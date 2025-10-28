import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;

/**
 * Service for interacting with Crossmint API for wallet operations.
 */
public class CrossmintService {
    private static final Logger logger = new Logger(CrossmintService.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String apiKey;

    public CrossmintService() {
        baseUrl = Config.CROSSMINT_BASE_URL;
        apiKey = Config.CROSSMINT_API_KEY;
        mapper = new ObjectMapper();
        client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .build();
    }

    private Request.Builder requestBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("X-API-KEY", apiKey)
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Creates a new smart wallet with external signer.
     */
    public JsonNode createWallet(String address) throws IOException {
        logger.info("Creating wallet for address: {}", address);
        
        ObjectNode config = mapper.createObjectNode();
        ObjectNode adminSigner = mapper.createObjectNode();
        adminSigner.put("type", "external-wallet");
        adminSigner.put("address", address);
        config.set("adminSigner", adminSigner);
        
        ObjectNode payload = mapper.createObjectNode();
        payload.set("config", config);
        payload.put("type", "smart");
        payload.put("chainType", "evm");

        Request request = requestBuilder(baseUrl + "/api/2025-06-09/wallets")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("Unexpected code " + response.code() + ": " + errorBody);
            }
            JsonNode body = mapper.readTree(response.body().string());
            logger.success("Wallet created successfully");
            return body;
        }
    }

    /**
     * Funds a wallet with test tokens.
     */
    public JsonNode fundWallet(String walletLocator, String amount) throws IOException {
        logger.info("Funding wallet {} with {} USDXM", walletLocator, amount);
        
        ObjectNode payload = mapper.createObjectNode();
        payload.put("amount", Integer.parseInt(amount));
        payload.put("token", "usdxm");
        payload.put("chain", Config.NETWORK);

        Request request = requestBuilder(baseUrl + "/api/v1-alpha2/wallets/" + walletLocator + "/balances")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("Unexpected code " + response.code() + ": " + errorBody);
            }
            JsonNode body = mapper.readTree(response.body().string());
            logger.success("Wallet funded successfully");
            return body;
        }
    }

    /**
     * Creates a USDXM transfer transaction.
     */
    public JsonNode createTransaction(String walletLocator, String recipientAddress, String amount) throws IOException {
        logger.info("Creating transaction: {} USDXM to {}", amount, recipientAddress);
        
        ObjectNode payload = mapper.createObjectNode();
        payload.put("recipient", recipientAddress);
        payload.put("amount", amount);

        String url = baseUrl + "/api/2025-06-09/wallets/" + walletLocator + "/tokens/" + Config.NETWORK + ":usdxm/transfers";
        Request request = requestBuilder(url)
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("Unexpected code " + response.code() + ": " + errorBody);
            }
            JsonNode body = mapper.readTree(response.body().string());
            logger.success("Transaction created successfully");
            return body;
        }
    }

    /**
     * Approves a transaction with the provided signature.
     */
    public JsonNode approveTransaction(String walletLocator, String transactionId, String signerLocator, String signature) throws IOException {
        logger.info("Approving transaction {} with signature", transactionId);
        
        ObjectNode approval = mapper.createObjectNode();
        approval.put("signer", signerLocator);
        approval.put("signature", signature);
        
        ObjectNode payload = mapper.createObjectNode();
        payload.set("approvals", mapper.createArrayNode().add(approval));

        String url = baseUrl + "/api/2025-06-09/wallets/" + walletLocator + "/transactions/" + transactionId + "/approvals";
        
        Request request = requestBuilder(url)
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("Unexpected code " + response.code() + ": " + errorBody);
            }
            JsonNode body = mapper.readTree(response.body().string());
            logger.success("Transaction approved successfully");
            return body;
        }
    }

    /**
     * Gets the current status of a transaction.
     */
    public JsonNode getTransaction(String walletLocator, String transactionId) throws IOException {
        logger.debug("Getting transaction status for {}", transactionId);
        
        String url = baseUrl + "/api/2025-06-09/wallets/" + walletLocator + "/transactions/" + transactionId;
        Request request = requestBuilder(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("Unexpected code " + response.code() + ": " + errorBody);
            }
            return mapper.readTree(response.body().string());
        }
    }

    /**
     * Polls a transaction until completion or timeout.
     */
    public JsonNode pollTransaction(String walletLocator, String transactionId, long intervalMs, int maxAttempts) throws IOException, InterruptedException {
        logger.info("Starting to poll transaction {}...", transactionId);
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            try {
                JsonNode tx = getTransaction(walletLocator, transactionId);
                String status = tx.path("status").asText();
                logger.info("Transaction status: {} (attempt {}/{})", status, attempts + 1, maxAttempts);
                
                if ("completed".equals(status) || "success".equals(status) || "failed".equals(status) || "rejected".equals(status)) {
                    // Extract and log transaction hash if available
                    String txHash = tx.path("txHash").asText();
                    if (!txHash.isEmpty()) {
                        logger.info("Transaction Hash: {}", txHash);
                    }
                    
                    return tx;
                }
                
                Thread.sleep(intervalMs);
                attempts++;
                
            } catch (Exception e) {
                logger.error("Error polling transaction (attempt {}): {}", attempts + 1, e.getMessage());
                attempts++;
                
                if (attempts >= maxAttempts) {
                    throw new IOException("Failed to poll transaction after " + maxAttempts + " attempts");
                }
                
                Thread.sleep(intervalMs);
            }
        }
        
        throw new IOException("Transaction polling timed out after " + maxAttempts + " attempts");
    }
}