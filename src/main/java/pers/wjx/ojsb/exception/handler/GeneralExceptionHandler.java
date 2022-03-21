package pers.wjx.ojsb.exception.handler;

import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pers.wjx.ojsb.exception.AlreadyExistedException;
import pers.wjx.ojsb.exception.UnauthorizedException;
import pers.wjx.ojsb.exception.UnknownServerException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
    }

    @ExceptionHandler(AlreadyExistedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleAlreadyExistedException(AlreadyExistedException ex) {
        return ex.getMessage();
    }


    @ExceptionHandler(UnknownServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnknownException(UnknownServerException ex) {
        return ex.getMessage() + "：服务器发生未知错误";
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedException(UnauthorizedException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleNotLoginException(NotLoginException ex) {
        return "请登录后查看";
    }
}
