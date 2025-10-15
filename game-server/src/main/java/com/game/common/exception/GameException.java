package com.game.common.exception;

/**
 * 游戏业务异常基类
 */
public class GameException extends RuntimeException {
    
    private final int errorCode;
    private final String errorMessage;
    
    public GameException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public GameException(int errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        return String.format("GameException{errorCode=%d, errorMessage='%s'}", 
                errorCode, errorMessage);
    }
}