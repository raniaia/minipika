package org.raniaia.poseidon.framework.exception;

/**
 * @author tiansheng
 * @version 1.0.0
 * @date 2019/11/12 11:44
 * @since 1.8
 */
public class PoseidonException extends Exception{

    public PoseidonException() {
    }

    public PoseidonException(String message) {
        super(message);
    }

    public PoseidonException(String message, Throwable cause) {
        super(message, cause);
    }

    public PoseidonException(Throwable cause) {
        super(cause);
    }

    public PoseidonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}