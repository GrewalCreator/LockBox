package com.manager.password.manager;

import java.util.Scanner;

public class PasswordManager {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CredentialStorage manager = new CredentialStorage();


        System.out.println("Enter A Username: ");
        String username = scanner.next();

        System.out.println("Enter A Password: ");
        String password = scanner.next();

        System.out.println(username);
        System.out.println(password);
        Credentials credential = new Credentials("www.google.ca", username, password);

        try {
            manager.add(credential);
        }catch(CredentialExistsException e){
            System.out.println("Would you like to update the password? [y/n]");
            String response = scanner.next().toLowerCase();
            if(response.equals("y")) {
                manager.update(credential);
            }
        }
    }
}
