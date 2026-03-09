package com.assettracker.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.assettracker.dao.UserDAO;
import com.assettracker.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();
    // Define type for Gson parsing in doPut/doDelete (if needed)
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    // Helper method to check role authorization
    private boolean isAuthorized(HttpSession session) {
        if (session == null || session.getAttribute("userRole") == null) {
            return false;
        }
        String role = (String) session.getAttribute("userRole");
        return "ADMINISTRATOR".equals(role);
    }
    
    // --- GET: List All Users OR Get Single User by ID (FIXED) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Security Check
        if (!isAuthorized(request.getSession(false))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\":false, \"message\":\"Access Denied: Only ADMINISTRATOR can view users.\"}");
            out.flush();
            return;
        }

        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.length() > 1) {
                // Case 1: GET /api/users/{id}
                try {
                    // pathInfo will be like "/123", so we trim the leading slash
                    int userId = Integer.parseInt(pathInfo.substring(1));
                    User user = userDAO.getUserById(userId); // REQUIRED DAO METHOD
                    
                    if (user != null) {
                        out.print(gson.toJson(user));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"success\":false, \"message\":\"User not found.\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false, \"message\":\"Invalid user ID format.\"}");
                }
            } else {
                // Case 2: GET /api/users/ (List all users)
                List<User> users = userDAO.getAllUsers();
                out.print(gson.toJson(users));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            out.print("{\"success\":false, \"message\":\"Server error loading users: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    // --- POST: Create New User ---
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        
        // Security Check
        if (!isAuthorized(request.getSession(false))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\":false, \"message\":\"Access Denied: Only ADMINISTRATOR can create users.\"}");
            out.flush();
            return;
        }

        try {
            // Retrieve parameters from form data (Assumes frontend uses x-www-form-urlencoded for POST)
            User newUser = new User();
            newUser.setUsername(request.getParameter("username"));
            newUser.setPassword(request.getParameter("password")); 
            newUser.setFullName(request.getParameter("fullName"));
            newUser.setEmail(request.getParameter("email"));
            newUser.setRole(request.getParameter("role"));
            
            // Default new user to active
            newUser.setActive(true); 

            // Basic validation
            if (newUser.getUsername() == null || newUser.getUsername().isEmpty() || 
                newUser.getPassword() == null || newUser.getPassword().isEmpty() ||
                newUser.getRole() == null || newUser.getRole().isEmpty()) {
                throw new IllegalArgumentException("Missing required fields: username, password, or role.");
            }

            boolean success = userDAO.createUser(newUser);

            result.put("success", success);
            result.put("message", success ? "User created successfully" : "Failed to create user (possible duplicate username)");
            response.setStatus(success ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid input: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "A server error occurred during user creation. Check logs for DB error.");
        } finally {
            out.print(gson.toJson(result));
            out.flush();
        }
    }

    // --- PUT: Update User Details (Correct Path Extraction) ---
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        if (!isAuthorized(request.getSession(false))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\":false, \"message\":\"Access Denied: Only ADMINISTRATOR can update users.\"}");
            out.flush();
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false, \"message\":\"Missing user ID in URL path for update.\"}");
            out.flush();
            return;
        }

        try {
            // Read JSON body (required for PUT)
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String jsonBody = sb.toString();

            // Deserialize the JSON body into a User object
            User updatedUser = gson.fromJson(jsonBody, User.class);
            
            int userId = Integer.parseInt(pathInfo.substring(1)); // e.g., /123 -> 123
            
            updatedUser.setUserId(userId); 

            // Execute DAO update (The DAO handles updating based on User object properties)
            boolean success = userDAO.updateUser(updatedUser); // REQUIRED DAO METHOD

            result.put("success", success);
            result.put("message", success ? "User updated successfully" : "Failed to update user");
            response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid user ID in URL path.");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Server error during update: " + e.getMessage());
        } finally {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    // --- DELETE: Deactivate User (Correct Path Extraction) ---
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        
        if (!isAuthorized(request.getSession(false))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\":false, \"message\":\"Access Denied: Only ADMINISTRATOR can delete/deactivate users.\"}");
            out.flush();
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false, \"message\":\"Missing user ID in URL path for deactivation.\"}");
            out.flush();
            return;
        }

        try {
            int userId = Integer.parseInt(pathInfo.substring(1));

            // The UserDAO.deleteUser method performs a logical delete (sets is_active = FALSE)
            boolean success = userDAO.deleteUser(userId); // REQUIRED DAO METHOD

            result.put("success", success);
            result.put("message", success ? "User successfully deactivated." : "Failed to deactivate user.");
            response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid user ID in URL path.");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "A server error occurred during user deactivation.");
        } finally {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
}