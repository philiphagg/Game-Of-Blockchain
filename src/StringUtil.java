import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;


/**
 * Helper class responsible for converting input to sha-256 encoded string
 */
public class StringUtil {

    /**
     * Generates the merkle root of an array of transactions
     *
     * @param transactions The array of transactions
     * @return  the merkle root of the array
     */
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }

    /**
     * Method responsible for converting <code>input</code> to sha-256
     * encoded string
     *
     * @param input That shall be oncoded
     * @return      The encoded signature as string
     */
    public static String applySha256(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies ECVSA Signature
     *
     * @param privateKey the private key
     * @param input      input
     * @return           a signed byte array
     */
    public static byte[] applyECDSASignature(PrivateKey privateKey, String input){
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSignature = dsa.sign();
            output = realSignature;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return output;
    }

    /**
     * Verifies a String signature
     *
     * @param publicKey the key
     * @param data      the data
     * @param signature the signature
     * @return if signature matches
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getStringFromKey(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static String getStringFromKey(PrivateKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
