import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registration, login and password management. */
public class AuthService {
    private final Map<String, User> users = new LinkedHashMap<>();

    public AuthService() {
        users.put("admin", defaultAdmin());
    }

    private static User defaultAdmin() {
        // Default admin account (change the password after logging in)
        return new User("admin", hash("admin", "admin123"),
                "System Administrator", "-", Role.ADMIN);
    }

    /** Replaces all users with the ones loaded from file (admin is always kept). */
    public void replaceAll(List<User> loaded) {
        users.clear();
        for (User u : loaded) {
            users.put(u.getUsername().toLowerCase(), u);
        }
        if (!users.containsKey("admin")) {
            users.put("admin", defaultAdmin());
        }
    }

    /** Registers a customer. Throws IllegalArgumentException if the data is invalid. */
    public User register(String username, String password, String fullName, String address) {
        if (username == null || !username.matches("[A-Za-z0-9_]{4,20}")) {
            throw new IllegalArgumentException("Username must be 4-20 characters: letters, digits or _");
        }
        validatePassword(password);
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        String key = username.toLowerCase();
        if (users.containsKey(key)) {
            throw new IllegalArgumentException("This username is already taken");
        }
        User u = new User(key, hash(key, password), fullName.trim(),
                address == null || address.trim().isEmpty() ? "No address" : address.trim(),
                Role.CUSTOMER);
        users.put(key, u);
        return u;
    }

    /** Logs in. Throws IllegalArgumentException if it fails. */
    public User login(String username, String password) {
        User u = username == null ? null : users.get(username.trim().toLowerCase());
        if (u == null || !verify(u, password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!u.isActive()) {
            throw new IllegalArgumentException("This account has been suspended");
        }
        return u;
    }

    public boolean verify(User u, String password) {
        return password != null && u.getPasswordHash().equals(hash(u.getUsername(), password));
    }

    public void changePassword(User u, String newPassword) {
        validatePassword(newPassword);
        u.setPasswordHash(hash(u.getUsername(), newPassword));
    }

    public User find(String username) {
        return username == null ? null : users.get(username.trim().toLowerCase());
    }

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    private static String hash(String username, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((username.toLowerCase() + ":" + password)
                    .getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}