package com.lock.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site")
    private String site;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private byte[] password;

    @Column(name = "date_created")
    private LocalDate dateCreated;

    // Default constructor
    public Accounts() {
    }

    // Parameterized constructor
    public Accounts(String site, String username, String password) {
        this.site = site;
        this.username = username;
        this.password = encode(password);
        this.dateCreated = LocalDate.now();  // Set the current date as the creation date
    }

    // Method to encode password (for demonstration; replace with actual logic)
    private byte[] encode(String rawPassword) {
        // Implement your actual password encoding logic here
        return rawPassword.getBytes();  // Placeholder implementation
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }
}
