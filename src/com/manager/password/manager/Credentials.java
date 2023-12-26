package com.manager.password.manager;

public class Credentials {

    private String website;
    private String username;
    private Password password;

    public Credentials(String website, String username, String password){
        this.website = website;
        this.username = username;
        this.password = new Password(password);
    }

    public boolean changePassword(String oldPassword, String newPassword){
        if(password.equals(oldPassword)){
            this.password.changePassword(newPassword);
            return true;
        }
        return false;
    }

    public String getWebsite() {
        return website;
    }

    public String getUsername(){
        return username;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
