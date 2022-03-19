package pers.wjx.ojsb.exception;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException() {}

    public UnauthorizedException(String message) {
        super(message);
    }
}
