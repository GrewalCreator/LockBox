package com.manager.password.manager;

public class Password {
    private String password;



    public Password(String password){
        this.password = password;
    }

    public void changePassword(String newPassword){
        this.password = newPassword;
    }

    public boolean equals(String pass){
        return pass.equals(password);
    }
}



