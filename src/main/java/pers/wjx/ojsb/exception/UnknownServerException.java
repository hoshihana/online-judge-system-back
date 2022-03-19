package pers.wjx.ojsb.exception;

public class UnknownServerException extends RuntimeException {
    public UnknownServerException() {
    }

    public UnknownServerException(String message) {
        super(message);
    }
}
