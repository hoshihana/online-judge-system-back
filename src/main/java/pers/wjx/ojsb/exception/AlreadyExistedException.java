package pers.wjx.ojsb.exception;

public class AlreadyExistedException extends RuntimeException {
    public AlreadyExistedException() {
        super();
    }

    public AlreadyExistedException(String message) {
        super(message);
    }
}
