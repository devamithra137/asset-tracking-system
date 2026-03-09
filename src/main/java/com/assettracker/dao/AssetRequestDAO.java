package com.assettracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.assettracker.model.AssetRequest;

public class AssetRequestDAO {

    private AssetRequest extractRequestFromResultSet(ResultSet rs) throws SQLException {
        AssetRequest request = new AssetRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setUserId(rs.getInt("user_id"));
        request.setUserName(rs.getString("user_name"));
        request.setAssetId(rs.getInt("asset_id"));
        request.setAssetName(rs.getString("asset_name"));
        request.setAssetTag(rs.getString("asset_tag"));
        request.setRequestType(rs.getString("request_type"));
        request.setRequestDate(rs.getTimestamp("request_date"));
        request.setStatus(rs.getString("status"));
        
        int approvedBy = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            request.setApprovedBy(approvedBy);
            request.setApprovedByName(rs.getString("approved_by_name"));
        }
        
        request.setApprovedDate(rs.getTimestamp("approved_date"));
        request.setNotes(rs.getString("notes"));
        return request;
    }
    
    // Base SQL for selecting all request details
    private static final String BASE_SELECT_SQL = 
            "SELECT ar.*, u.full_name as user_name, a.asset_name, a.asset_tag, " +
            "approver.full_name as approved_by_name " +
            "FROM asset_requests ar " +
            "JOIN users u ON ar.user_id = u.user_id " +
            "JOIN assets a ON ar.asset_id = a.asset_id " +
            "LEFT JOIN users approver ON ar.approved_by = approver.user_id ";
    
    
    // --- READ OPERATIONS ---

    public List<AssetRequest> getAllRequests() {
        List<AssetRequest> requests = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY ar.request_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public List<AssetRequest> getRequestsByUser(int userId) {
        List<AssetRequest> requests = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE ar.user_id = ? ORDER BY ar.request_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public List<AssetRequest> getPendingRequests() {
        List<AssetRequest> requests = new ArrayList<>();
        // Changed to PreparedStatement for consistency, although a simple Statement was fine
        String sql = BASE_SELECT_SQL + "WHERE ar.status = 'PENDING' ORDER BY ar.request_date ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement(); // Statement is fine since no parameters are set
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    // --- CREATE OPERATION ---

    public boolean createRequest(AssetRequest request) {
        // FIX: Explicitly include 'status' column and set it to 'PENDING'
        String sql = "INSERT INTO asset_requests (user_id, asset_id, request_type, notes, status) " +
                     "VALUES (?, ?, ?, ?, 'PENDING')"; // <-- Fixed SQL statement
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, request.getUserId());
            stmt.setInt(2, request.getAssetId());
            stmt.setString(3, request.getRequestType());
            stmt.setString(4, request.getNotes());
            // Parameter 5 is the static 'PENDING' value in the SQL string, so no 5th setString needed
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // --- UPDATE OPERATION ---

    public boolean updateRequestStatus(int requestId, String status, int approvedBy, String notes) {
        String sql = "UPDATE asset_requests SET status = ?, approved_by = ?, " +
                     "approved_date = CURRENT_TIMESTAMP, notes = ? WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            // Allow approved_by to be null if request is REJECTED or CANCELLED, though INT 
            // column usually requires a value. Assuming approvedBy is a non-nullable int here:
            stmt.setInt(2, approvedBy); 
            stmt.setString(3, notes);
            stmt.setInt(4, requestId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // You might also need a getRequestById for the servlet to load full details for approval
    public AssetRequest getRequestById(int requestId) {
        String sql = BASE_SELECT_SQL + "WHERE ar.request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractRequestFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}