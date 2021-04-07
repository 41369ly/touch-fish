package cn.tybblog.touchfish.exception;

/**
 * @author ly
 */
public class FishException extends Exception {

    /** 异常信息 */
    private String message;

    public FishException(String message){
        super(message);
        this.message = message;
    }

    /**
     * 抛出异常
     * @param msg 异常信息
     * @throws FishException
     */
    public static void throwFishException(String msg) throws FishException {
        throw new FishException(msg);
    }
}
