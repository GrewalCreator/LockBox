package com.manager.password;

import java.security.*;

public class KeysGenerator {
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    KeyPairGenerator keyPairGenerator;
    public KeysGenerator() throws NoSuchAlgorithmException {
        keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(2048);
        KeyPair pair = keyPairGenerator.generateKeyPair();

        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
    }

    public PublicKey getPublicKey(){return publicKey;}

    public boolean verifyPrivateKey(PrivateKey privateKey){return privateKey == this.privateKey;}
}
