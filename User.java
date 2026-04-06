package com.brewbite.model.user;

import java.util.Objects;

/**
 * Data class representing a user of the Brew & Bite system.
 * Customers have no credentials; Baristas and Managers use hardcoded login.
 *
 * Responsibility: Hold identity and role information for one user.
 */
public class User {

    private String username;
    private String password; // null for customers
    private UserRole role;

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public User() {}

    /**
     * Full constructor for staff accounts (Barista / Manager).
     *
     * @param username the login name
     * @param password the plaintext password (hardcoded for this assignment)
     * @param role     the role granted to this account
     */
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    /**
     * Convenience constructor for a walk-in customer (no credentials).
     *
     * @param displayName the name entered at the kiosk
     */
    public User(String displayName) {
        this.username = displayName;
        this.password = null;
        this.role     = UserRole.CUSTOMER;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getUsername()              { return username; }
    public void   setUsername(String u)      { this.username = u; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }

    public UserRole getRole()                { return role; }
    public void     setRole(UserRole r)      { this.role = r; }

    // ── Business helpers ─────────────────────────────────────────────────────

    /**
     * Validates login credentials.
     * Customers are never validated this way — they skip login entirely.
     *
     * @param inputPassword the password provided at the login screen
     * @return true if the password matches
     */
    public boolean authenticate(String inputPassword) {
        if (password == null) return false;
        return password.equals(inputPassword);
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(username, other.username) &&
               Objects.equals(role,     other.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, role);
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', role=" + role + "}";
    }
}
