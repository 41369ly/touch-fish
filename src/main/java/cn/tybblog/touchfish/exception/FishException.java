package cn.tybblog.touchfish.exception;

/**
 * @author ly
 */
public class FishException extends Exception {

    /** �쳣��Ϣ */
    private String message;

    public FishException(String message){
        super(message);
        this.message = message;
    }

    /**
     * �׳��쳣
     * @param msg �쳣��Ϣ
     * @throws FishException
     */
    public static void throwFishException(String msg) throws FishException {
        throw new FishException(msg);
    }
}
