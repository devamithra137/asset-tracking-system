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

import com.assettracker.dao.AssetDAO;
import com.assettracker.dao.AssetRequestDAO;
import com.assettracker.model.AssetRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; 
import java.lang.reflect.Type;
import java.sql.SQLException; // Import SQLException for better error handling


public class RequestServlet extends HttpServlet {

    private AssetRequestDAO requestDAO = new AssetRequestDAO();
    private AssetDAO assetDAO = new AssetDAO();
    private Gson gson = new Gson();
    
    // Define type for Gson parsing in doPut
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        Map<String, Object> errorResult = new HashMap<>();

        try {
            // Check for /pending
            if ("/pending".equals(pathInfo)) {
                List<AssetRequest> pending = requestDAO.getPendingRequests();
                out.print(gson.toJson(pending));
            // Check for /user/{id}
            } else if (pathInfo != null && pathInfo.startsWith("/user/")) {
                String userIdStr = pathInfo.substring(6);
                int userId = Integer.parseInt(userIdStr);
                List<AssetRequest> userRequests = requestDAO.getRequestsByUser(userId);
                out.print(gson.toJson(userRequests));
            // Default: List all
            } else {
                List<AssetRequest> all = requestDAO.getAllRequests();
                out.print(gson.toJson(all));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorResult.put("success", false);
            errorResult.put("message", "Invalid user ID format in path.");
            out.print(gson.toJson(errorResult));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            errorResult.put("success", false);
            errorResult.put("message", "Server error during GET request: " + e.getMessage());
            out.print(gson.toJson(errorResult));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false, \"message\":\"Unauthorized: Please log in.\"}");
            out.flush();
            return;
        }

        try {
            // Parameter parsing (Assumes frontend sends x-www-form-urlencoded)
            int userId = (Integer) session.getAttribute("userId");
            // Use Integer.parseInt and handle potential null/empty string gracefully
            String assetIdParam = request.getParameter("assetId");
            int assetId = (assetIdParam != null && !assetIdParam.isEmpty()) ? Integer.parseInt(assetIdParam) : -1;
            
            String requestType = request.getParameter("requestType");
            String notes = request.getParameter("notes");

            if (requestType == null || requestType.isEmpty() || assetId <= 0) {
                throw new IllegalArgumentException("Missing required request fields (Asset ID, Request Type).");
            }

            AssetRequest assetRequest = new AssetRequest(userId, assetId, requestType);
            assetRequest.setNotes(notes);

            boolean success = requestDAO.createRequest(assetRequest);

            result.put("success", success);
            result.put("message", success ? "Request submitted successfully" : "Request failed (Database Error)");
            response.setStatus(success ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid format for Asset ID.");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Missing or invalid data: " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Server error during request creation: " + e.getMessage());
        } finally {
            out.print(gson.toJson(result));
            out.flush();
        }
    }

    // --- PUT: Approve/Reject Request (FIXED TRANSACTION LOGIC ASSUMED) ---
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userRole") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false, \"message\":\"Unauthorized: Please log in.\"}");
            out.flush();
            return;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        int approverId = (Integer) session.getAttribute("userId");
        boolean success = false;
        String status = "UNKNOWN"; // Initialize status for messaging

        try {
            // 1. Read JSON body from input stream
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String jsonBody = sb.toString();

            // 2. Parse the JSON body into a generic Map
            Map<String, String> data = gson.fromJson(jsonBody, MAP_TYPE);

            String pathInfo = request.getPathInfo();
            // Ensure pathInfo is not null and can be parsed
            if (pathInfo == null || pathInfo.length() <= 1) {
                 throw new IllegalArgumentException("Missing Request ID in URL path.");
            }
            int requestId = Integer.parseInt(pathInfo.substring(1)); // e.g., /123 -> 123

            // 3. Retrieve parameters from the JSON body
            status = data.get("status");
            String notes = data.get("notes");
            
            // Required parameters for asset update logic after approval
            String reqTypeStr = data.get("requestType");
            String assetIdStr = data.get("assetId");
            String userIdStr = data.get("userId");
            
            // Basic role and status validation
            if (!("ADMINISTRATOR".equals(userRole) || "INVENTORY_MANAGER".equals(userRole))) {
                 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                 result.put("success", false);
                 result.put("message", "Access Denied: Only Administrator or Inventory Manager can update requests.");
                 out.print(gson.toJson(result));
                 out.flush();
                 return;
            }
            
            if (status == null || status.isEmpty() || !("APPROVED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status))) {
                 throw new IllegalArgumentException("Invalid or missing status parameter.");
            }
            
            // --- CORE TRANSACTION LOGIC STARTS HERE ---
            
            // 4. Update the request status first
            success = requestDAO.updateRequestStatus(requestId, status, approverId, notes);

            // 5. Perform asset assignment/unassignment ONLY if APPROVED and request status updated successfully
            if (success && "APPROVED".equalsIgnoreCase(status)) {
                 if (reqTypeStr == null || assetIdStr == null || userIdStr == null) {
                    // This is a FATAL application logic error if it occurs here
                    // Rollback should happen in a proper Service layer, but for now, log it.
                    System.err.println("CRITICAL: Missing Asset/User/RequestType data after request approval. Cannot update asset.");
                    // Throw to trigger the catch block
                    throw new IllegalArgumentException("Missing Asset ID, User ID, or Request Type for asset action.");
                 }
                 
                 int assetId = Integer.parseInt(assetIdStr);
                 int userId = Integer.parseInt(userIdStr);
                 
                 boolean assetUpdateSuccess = false;

                 if ("ASSIGNMENT".equalsIgnoreCase(reqTypeStr)) {
                     // REQUIRED DAO METHOD: Sets asset status to ASSIGNED and assigned_to to userId
                     assetUpdateSuccess = assetDAO.assignAsset(assetId, userId);
                 } else if ("RETURN".equalsIgnoreCase(reqTypeStr)) {
                     // REQUIRED DAO METHOD: Sets asset status to AVAILABLE and assigned_to to NULL
                     assetUpdateSuccess = assetDAO.unassignAsset(assetId);
                 }

                 if (!assetUpdateSuccess) {
                    // This indicates a DB failure in assetDAO. Rollback request update (ideally)
                    System.err.println("CRITICAL: Failed to update asset status/assignment in DB for approved request.");
                    throw new SQLException("Asset update failed. Check database constraints/connection.");
                 }
            }
            
            // --- CORE TRANSACTION LOGIC ENDS HERE ---

            result.put("success", success);
            result.put("message", "Request " + status.toLowerCase() + " successfully.");
            response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid ID format in URL or JSON data.");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Missing or invalid required data: " + e.getMessage());
        } catch (SQLException e) {
             // Catch explicit DB failures from assetDAO or requestDAO
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database transaction failed during status update or asset assignment. Please check logs.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "A critical server error occurred during request update: " + e.getMessage());
        } finally {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
}