package com.manager.password.manager;

import java.util.HashMap;

/**
 * This is a temporary method of storing passwords
 */


public class CredentialStorage {
    private final HashMap<String, Credentials> credentialsStorage;

    public CredentialStorage(){
        credentialsStorage = new HashMap<>();
    }

    public boolean add(Credentials credential){
        if(!credentialsStorage.containsKey(credential.getWebsite())){
            credentialsStorage.put(credential.getWebsite(), credential);
            return true;
        }

        throw new CredentialExistsException("Credentials For " + credential.getWebsite() + " Exists");
    }

    public void update(Credentials credential) {
        credentialsStorage.put(credential.getWebsite(), credential);
    }
}
