import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Service for generating EVM keypairs and signing messages for Crossmint transactions.
 */
public class KeypairService {
    private static final Logger logger = new Logger(KeypairService.class);

    private ECKeyPair keyPair;
    private String address;

    /**
     * Represents a generated EVM keypair with address and private key.
     */
    public static class Keypair {
        public final String privateKeyHex;
        public final String address;
        public final ECKeyPair keyPair;

        public Keypair(String privateKeyHex, String address, ECKeyPair keyPair) {
            this.privateKeyHex = privateKeyHex;
            this.address = address;
            this.keyPair = keyPair;
        }
    }

    /**
     * Generates a new EVM keypair using secure random.
     */
    public Keypair generateKeypair() {
        try {
            logger.info("Generating new EVM keypair...");
            SecureRandom random = new SecureRandom();
            byte[] privateKeyBytes = new byte[32];
            random.nextBytes(privateKeyBytes);
            BigInteger privateKey = new BigInteger(1, privateKeyBytes);

            keyPair = ECKeyPair.create(privateKey);
            address = "0x" + Keys.getAddress(keyPair.getPublicKey());

            String privateKeyHex = Numeric.toHexStringNoPrefixZeroPadded(keyPair.getPrivateKey(), 64);
            logger.success("Generated keypair for address: {}", address);

            return new Keypair("0x" + privateKeyHex, address, keyPair);
        } catch (Exception e) {
            logger.error("Failed to generate keypair: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getAddress() {
        return address;
    }

    public String getPrivateKey() {
        if (keyPair == null) return null;
        return "0x" + Numeric.toHexStringNoPrefixZeroPadded(keyPair.getPrivateKey(), 64);
    }

    public ECKeyPair getKeyPair() {
        return keyPair;
    }

    /**
     * Signs a message hash for Crossmint transaction approval.
     * Uses signPrefixedMessage to match viem's behavior with raw message signing.
     */
    public String signMessageHash(String messageHash) {
        if (keyPair == null) {
            throw new IllegalStateException("No keypair available. Generate a keypair first.");
        }

        logger.info("Signing message hash: {}", messageHash);

        try {
            String privateKeyHex = getPrivateKey();
            String signature = signMessage(messageHash, privateKeyHex);
            logger.success("Message hash signed successfully");
            
            return signature;
        } catch (Exception e) {
            logger.error("Failed to sign message hash: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Signs a message hash for EVM smart wallet transaction approval.
     * Uses signPrefixedMessage to match viem's behavior with raw message signing.
     * 
     * @param messageHex The message hash from approvals.pending[0].message
     * @param privateKeyHex The EOA private key
     * @return Hex-encoded signature in format: 0x[r][s][v] (132 chars total)
     */
    public static String signMessage(String messageHex, String privateKeyHex) {
        // Remove 0x prefix if present
        String cleanMessage = messageHex.startsWith("0x") ? messageHex.substring(2) : messageHex;
        String cleanPrivateKey = privateKeyHex.startsWith("0x") ? privateKeyHex.substring(2) : privateKeyHex;
          
        // Convert hex strings to byte arrays
        byte[] messageBytes = Numeric.hexStringToByteArray(cleanMessage);
        byte[] privateKeyBytes = Numeric.hexStringToByteArray(cleanPrivateKey);
          
        // Create key pair from private key
        ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);
          
        // Use signPrefixedMessage to match viem's behavior with raw message signing
        Sign.SignatureData signature = Sign.signPrefixedMessage(messageBytes, keyPair);
          
        // Extract r, s, v components
        byte[] r = signature.getR();
        byte[] s = signature.getS();
        byte[] v = signature.getV();
          
        // Convert to hex and ensure proper padding
        String rHex = padLeft(Numeric.toHexStringNoPrefix(r), 64);
        String sHex = padLeft(Numeric.toHexStringNoPrefix(s), 64);
        String vHex = Numeric.toHexStringNoPrefix(v);
          
        // Combine into final signature: 0x + r(64) + s(64) + v(2)
        return "0x" + rHex + sHex + vHex;
    }
      
    /**
     * Pads a hex string with leading zeros to reach target length.
     */
    private static String padLeft(String str, int length) {
        while (str.length() < length) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * Displays keypair information to console.
     */
    public void displayKeypairInfo() {
        if (keyPair == null) {
            logger.warn("No keypair available");
            return;
        }

        System.out.println("\nKeypair Information:");
        System.out.println("=".repeat(50));
        System.out.println("Address: " + address);
        System.out.println("Private Key: " + getPrivateKey());
        System.out.println("=".repeat(50));
    }
}