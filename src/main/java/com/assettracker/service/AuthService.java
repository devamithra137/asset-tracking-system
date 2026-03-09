package com.assettracker.service;

import com.assettracker.dao.UserDAO;
import com.assettracker.model.User;

public class AuthService {
    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User authenticateUser(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return null;
        }
        
        User user = userDAO.authenticate(username, password);
        
        if (user != null && user.isActive()) {
            return user;
        }
        
        return null;
    }

    public boolean validateUserRole(User user, String requiredRole) {
        if (user == null || requiredRole == null) {
            return false;
        }
        
        return user.getRole().equals(requiredRole);
    }

    public boolean hasPermission(User user, String permission) {
        if (user == null) {
            return false;
        }
        
        switch (permission) {
            case "CREATE_ASSET":
            case "EDIT_ASSET":
            case "APPROVE_REQUEST":
                return user.getRole().equals("ADMINISTRATOR") || 
                       user.getRole().equals("INVENTORY_MANAGER");
            
            case "DELETE_ASSET":
            case "MANAGE_USERS":
            case "VIEW_REPORTS":
                return user.getRole().equals("ADMINISTRATOR");
            
            case "REQUEST_ASSET":
            case "RETURN_ASSET":
            case "VIEW_MY_ASSETS":
                return true; // All authenticated users
            
            default:
                return false;
        }
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public boolean createUser(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        
        // Add password validation logic here if needed
        if (user.getPassword().length() < 6) {
            return false;
        }
        
        return userDAO.createUser(user);
    }

    public boolean updateUser(User user) {
        if (user == null || user.getUserId() <= 0) {
            return false;
        }
        
        return userDAO.updateUser(user);
    }

    public boolean deactivateUser(int userId) {
        return userDAO.deleteUser(userId);
    }
}
