package org.sdi.chatmanager.exceptions;

public class ExistingMemberException extends RuntimeException {

    public ExistingMemberException(String message) {
        super(message);
    }
}
