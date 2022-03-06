import java.security.*;
import java.util.ArrayList;

public class Transaction {
    public String transactionId;
    public PublicKey sender;
    public PublicKey recipient;
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0;

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    /**
     * Checks if transactions is valid and gather
     * inputs and generating outputs
     *
     * @return if transaction is valid
     */
    public boolean processTransaction() {

        if(!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }


        for(TransactionInput i : inputs) {
            i.UTXO = GameOfBlockchain.UTXOs.get(i.transactionOutputId);
        }


        if(getInputsValue() < GameOfBlockchain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }


        float leftOver = getInputsValue() - value;
        transactionId = calculateHash();
        outputs.add(new TransactionOutput( this.recipient, value,transactionId));
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId));


        for(TransactionOutput o : outputs) {
            GameOfBlockchain.UTXOs.put(o.id , o);
        }


        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue;
            GameOfBlockchain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    /**
     * returns sum of inputs(UTXOs) values
     *
     * @return sum(UTXOs)
     */
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    /**
     * getter for the sum of outputs
     *
     * @return the sum of output transactions.
     */
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    /**
     * Signs the data that shall be immutable
     *
     * @param privateKey
     */
    public void generateSignature(PrivateKey privateKey){
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASignature(privateKey,data);
    }

    /**
     * Verifies that the data we signed has not changed
     *
     * @return true if the data wasn't changed
     */
    public boolean verifySignature(){
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    private String calculateHash(){
        sequence++;
        return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value) + sequence);

    }
}
