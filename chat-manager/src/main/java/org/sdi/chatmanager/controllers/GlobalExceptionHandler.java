package org.sdi.chatmanager.controllers;

import org.sdi.chatmanager.dtos.ErrorDto;
import org.sdi.chatmanager.exceptions.ExistingMemberException;
import org.sdi.chatmanager.exceptions.UserAlreadyInvitedException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.sdi.chatmanager.exceptions.NotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(String.valueOf(HttpStatus.BAD_REQUEST), message));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(String.valueOf(HttpStatus.NOT_FOUND), ex.getMessage()));
    }

    @ExceptionHandler(ExistingMemberException.class)
    public ResponseEntity<ErrorDto> handleExistingMemberException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(String.valueOf(HttpStatus.BAD_REQUEST), ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyInvitedException.class)
    public ResponseEntity<ErrorDto> handleUserAlreadyInvitedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(String.valueOf(HttpStatus.BAD_REQUEST), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        String message = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR), message));
    }
}
