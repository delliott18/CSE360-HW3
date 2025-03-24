public class UserManager {
    private static final int MIN_USERNAME_LENGTH = 6;
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final String sessionToken;

        public AuthResult(boolean success, String message, String sessionToken) {
            this.success = success;
            this.message = message;
            this.sessionToken = sessionToken;
        }
    }

    public AuthResult registerUser(String username, String password) {
        // Username validation
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username is required", null);
        }

        username = username.trim();

        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            return new AuthResult(false, 
                "Username must be between " + MIN_USERNAME_LENGTH + 
                " and " + MAX_USERNAME_LENGTH + " characters", null);
        }

        if (!username.matches("^[a-zA-Z0-9._]+$")) {
            return new AuthResult(false, 
                "Username can only contain letters, numbers, dots, and underscores", null);
        }

        if (!username.matches(".*[a-zA-Z].*")) {
            return new AuthResult(false, "Username must contain at least one letter", null);
        }

        if (username.startsWith(".") || username.endsWith(".")) {
            return new AuthResult(false, "Username cannot start or end with a dot", null);
        }

        if (username.contains("..")) {
            return new AuthResult(false, "Username cannot contain consecutive dots", null);
        }

        // Password validation
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Password is required", null);
        }

        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return new AuthResult(false, 
                "Password must be between " + MIN_PASSWORD_LENGTH + 
                " and " + MAX_PASSWORD_LENGTH + " characters", null);
        }

        if (!password.matches(".*[A-Z].*")) {
            return new AuthResult(false, "Password must contain at least one uppercase letter", null);
        }

        if (!password.matches(".*[a-z].*")) {
            return new AuthResult(false, "Password must contain at least one lowercase letter", null);
        }

        if (!password.matches(".*\\d.*")) {
            return new AuthResult(false, "Password must contain at least one number", null);
        }

        if (!password.matches(".*[!@#$%^&*()\\[\\]{}\\-_=+;:,<.>/?].*")) {
            return new AuthResult(false, "Password must contain at least one special character", null);
        }

        // Check if username is taken
        if (MemoryStorage.userExists(username)) {
            return new AuthResult(false, "Username is already taken", null);
        }

        // Create user
        MemoryStorage.addUser(username, password);
        String sessionToken = MemoryStorage.createSession(username);
        return new AuthResult(true, "Registration successful", sessionToken);
    }

    public AuthResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username is required", null);
        }
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Password is required", null);
        }

        username = username.trim();

        if (!MemoryStorage.userExists(username)) {
            return new AuthResult(false, "Invalid username or password", null);
        }

        if (MemoryStorage.isLockedOut(username)) {
            return new AuthResult(false, 
                "Account is locked. Please try again in " + 
                MemoryStorage.getLockoutTimeRemaining(username) + " minutes.", null);
        }

        String storedPassword = MemoryStorage.getPassword(username);
        if (password.equals(storedPassword)) {
            MemoryStorage.resetLoginAttempts(username);
            String sessionToken = MemoryStorage.createSession(username);
            return new AuthResult(true, "Login successful", sessionToken);
        } else {
            MemoryStorage.incrementLoginAttempts(username);
            if (MemoryStorage.isLockedOut(username)) {
                return new AuthResult(false, "Too many failed attempts. Account has been locked.", null);
            }
            return new AuthResult(false, "Invalid username or password", null);
        }
    }
}