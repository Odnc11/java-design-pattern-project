package main.models;

/**
 * Abstract base class for all users in the e-commerce system.
 * 
 * Follows Open/Closed Principle - extend for new user types (Seller, Buyer)
 * without modifying this class.
 * 
 * @see Seller
 * @see Buyer
 */
public abstract class User {
    public enum Role { SELLER, BUYER }

    private int id;
    private String name;
    private String email;
    private String password;

    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * @return The role of this user (SELLER or BUYER)
     */
    public abstract Role getRole();

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }

    /**
     * Authenticate by checking email and password.
     */
    public boolean authenticate(String email, String password) {
        return this.email.equalsIgnoreCase(email) && this.password.equals(password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof User)) return false;
        User other = (User) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, name='%s', email='%s', role=%s}",
                getClass().getSimpleName(), id, name, email, getRole());
    }
}
