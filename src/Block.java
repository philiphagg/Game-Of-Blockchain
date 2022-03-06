import java.util.ArrayList;
import java.util.Date;

/**
 * This class represents instances of blocks on the
 * blockchain.
 */
public class Block {

    public String hash;
    public String prevHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    private long timeStamp;
    private int nonce;

    public Block(String prevHash) {
        this.prevHash = prevHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    /**
     * calculates the hash thats stored in the block
     *
     * @return the hash
     */
    public String calculateHash(){
        return StringUtil.applySha256(prevHash + Long.toString(timeStamp) + nonce + merkleRoot);
    }

    /**
     * Mining blocks on the blockchain
     *
     * @param difficulty    difficulty of mining blocks.
     */
    public void mineBlock(int difficulty){
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0, difficulty).equals(target)){
            nonce++;
            hash = calculateHash();
        }
        System.out.println("This block was mined " + hash);
        System.out.println("At Nounce: "+nonce);
    }

    public boolean addTransaction(Transaction transaction) {
        if(transaction == null) return false;
        if((!prevHash.equals("0"))) {
            if((!transaction.processTransaction())) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}
