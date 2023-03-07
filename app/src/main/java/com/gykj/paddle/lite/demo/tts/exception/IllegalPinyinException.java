package com.gykj.paddle.lite.demo.tts.exception;

/**
 *
 * @author Ricky Fung
 */
public class IllegalPinyinException extends Exception {

    private static final long serialVersionUID = 4447260855879734366L;

    public IllegalPinyinException() {
    }

    public IllegalPinyinException(String message) {
        super(message);
    }

    public IllegalPinyinException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalPinyinException(Throwable cause) {
        super(cause);
    }
}
