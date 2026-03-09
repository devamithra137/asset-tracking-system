package com.assettracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.assettracker.model.User;
// NOTE: You need a utility class for password hashing, e.g., PasswordUtil
// For this example, we'll assume a dummy static method `hashPassword(String)` exists.
// Replace with actual Argon2/Bcrypt/Scrypt implementation in production!

public class UserDAO {
    
    // Placeholder for password utility - MUST BE REPLACED WITH REAL HASHING!
    private String hashPassword(String password) {
        // In a real application, use a library like jBCrypt or Argon2 (e.g., return BCrypt.hashpw(password, BCrypt.gensalt());)
        // For now, we assume this method exists and returns a hashed string.
        return password; // DANGER: PLAINTEXT PASSWORD! FIX THIS IN YOUR PRODUCTION CODE.
    }
    
    // Placeholder for password verification
    private boolean checkPassword(String plainPassword, String hashedPassword) {
        // In a real application, use a library (e.g., return BCrypt.checkpw(plainPassword, hashedPassword);)
        return plainPassword.equals(hashedPassword);
    }
    
    // --- AUTHENTICATE (FIXED: Uses Hashed Password Check) ---
    public User authenticate(String username, String password) {
        // SQL only retrieves user by username. Password check is done in Java.
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                // Security Fix: Check hashed password against the stored password
                if (checkPassword(password, user.getPassword())) {
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // --- GET USER BY ID (Already Correct) ---
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- CREATE USER (FIXED: Hashing password before insert) ---
    public boolean createUser(User user) {
        // Note: Added is_active to the insert statement for clarity, though it might default correctly
        String sql = "INSERT INTO users (username, password, full_name, email, role, is_active) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            // Security Fix: Hash the password before storing it
            stmt.setString(2, hashPassword(user.getPassword())); 
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- UPDATE USER (FIXED: Conditional password update) ---
    public boolean updateUser(User user) {
        // Check if a new password was provided by the frontend/servlet
        String newPassword = user.getPassword();
        boolean updatePassword = (newPassword != null && !newPassword.isEmpty());
        
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, is_active = ?" + 
                     (updatePassword ? ", password = ?" : "") + // Conditionally add password column
                     " WHERE user_id = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int index = 1;
            stmt.setString(index++, user.getFullName());
            stmt.setString(index++, user.getEmail());
            stmt.setString(index++, user.getRole());
            stmt.setBoolean(index++, user.isActive());
            
            if (updatePassword) {
                // Security Fix: Hash the new password before updating
                stmt.setString(index++, hashPassword(newPassword)); 
            }
            
            stmt.setInt(index++, user.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
 // Inside com.assettracker.dao.UserDAO

 // ... (other methods)

 /**
  * Counts the number of active users in the database.
  */
 public int getUserCount() throws SQLException {
     String sql = "SELECT COUNT(*) FROM users WHERE is_active = TRUE";
     try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {
         
         if (rs.next()) {
             return rs.getInt(1);
         }
     }
     return 0;
 }
    // --- DELETE/DEACTIVATE USER (Already Correct for Logical Deletion) ---
    public boolean deleteUser(int userId) {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- HELPER METHOD (Already Correct) ---
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}