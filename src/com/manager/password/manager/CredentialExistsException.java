package com.manager.password.manager;

public class CredentialExistsException extends RuntimeException{
    public CredentialExistsException(String errorMessage){
        super(errorMessage);
    }
}
