import com.fasterxml.jackson.databind.JsonNode;

/**
 * Crossmint wallet integration demo
 * with EVM keypair generation, wallet creation, funding, and transaction signing + approval
 */
public class Main {
    private static final Logger logger = new Logger(Main.class);

    public static void main(String[] args) {
        try {
        logger.info("Starting Crossmint Wallet Java Demo");
        logger.info("============================================================");

            var keypairService = new KeypairService();
            var crossmintService = new CrossmintService();

            // Step 1: Generate keypair
            logger.info("Step 1/6: Generating EVM keypair using web3j");
            var keypair = keypairService.generateKeypair();
            keypairService.displayKeypairInfo();

            // Step 2: Create wallet
            logger.info("Step 2/6: Creating wallet with Crossmint API");
            var walletResp = crossmintService.createWallet(keypair.address);
            
            String walletLocator = extractWalletLocator(walletResp);

            var crossmintWalletAddress = walletResp.path("address").asText();
            logger.info("Crossmint Wallet Address: {}", crossmintWalletAddress);

            // Step 3: Fund wallet
            logger.info("Step 3/6: Funding wallet with test tokens");
            if (crossmintWalletAddress.isEmpty()) {
                throw new RuntimeException("Crossmint wallet address is empty - cannot proceed with funding");
            }
            crossmintService.fundWallet(crossmintWalletAddress, Config.FUND_AMOUNT);
            
            // Wait for funding to complete
            logger.info("Waiting for funding to complete...");
            Thread.sleep(2000);

            // Step 4: Create transaction
            logger.info("Step 4/6: Creating USDXM transfer transaction");
            var txResp = crossmintService.createTransaction(
                crossmintWalletAddress, 
                Config.DEMO_RECIPIENT_ADDRESS, 
                Config.DEMO_AMOUNT_USDXM
            );
            
            var txId = txResp.path("id").asText();
            logger.info("Transaction ID: {}", txId);

            // Extract the message to sign from the response
            var approvals = txResp.path("approvals").path("pending");
            if (approvals.isArray() && approvals.size() > 0) {
                var pendingApproval = approvals.get(0);
                var messageToSign = pendingApproval.path("message").asText();
                var signerLocator = pendingApproval.path("signer").path("locator").asText();

                // Step 5: Sign message and approve transaction
                logger.info("Step 5/6: Signing message and approving transaction");
                var signature = keypairService.signMessageHash(messageToSign);
                
                crossmintService.approveTransaction(
                    crossmintWalletAddress, 
                    txId, 
                    signerLocator, 
                    signature
                );

                // Step 6: Poll transaction result
                logger.info("Step 6/6: Polling for transaction result");
                var finalTx = crossmintService.pollTransaction(crossmintWalletAddress, txId, 5000, 60);
                var finalStatus = finalTx.path("status").asText();
                var txHash = finalTx.path("onChain").path("txId").asText();

                // Summary
                displaySummary(keypair.address, crossmintWalletAddress, txId, finalStatus, txHash);
                
            } else {
                throw new RuntimeException("No pending approvals found in transaction response");
            }

        } catch (Exception e) {
            logger.error("Demo failed: {}", e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Extracts wallet locator from wallet response, trying multiple possible fields.
     */
    private static String extractWalletLocator(JsonNode walletResp) {
        String walletLocator = walletResp.path("locator").asText();
        if (walletLocator.isEmpty()) {
            walletLocator = walletResp.path("id").asText();
        }
        if (walletLocator.isEmpty()) {
            walletLocator = walletResp.path("walletId").asText();
        }
        return walletLocator;
    }

    /**
     * Displays the final demo summary with transaction details.
     */
    private static void displaySummary(String address, String walletLocator, String txId, String finalStatus, String txHash) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Status: " + finalStatus);
        System.out.println("TxId: " + txId);
        System.out.println("TxHash: " + txHash);
        System.out.println("Smart Wallet: " + walletLocator);
        System.out.println("External Wallet Signer: " + address);
        System.out.println("Network: " + Config.NETWORK);
        System.out.println("Amount: " + Config.DEMO_AMOUNT_USDXM + " USDXM");
        System.out.println("Recipient: " + Config.DEMO_RECIPIENT_ADDRESS);
        System.out.println("=".repeat(60));
    }
}