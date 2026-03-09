package com.assettracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.assettracker.model.Asset;

public class AssetDAO {

    // Base SQL fragment for SELECT operations
    private static final String BASE_SELECT_SQL = 
            "SELECT a.*, u.full_name as assigned_to_name FROM assets a " +
            "LEFT JOIN users u ON a.assigned_to = u.user_id ";

    // --- READ OPERATIONS ---

    public List<Asset> getAllAssets() {
        List<Asset> assets = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY a.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                assets.add(extractAssetFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assets;
    }

    /**
     * Retrieves assets based on search, category, and status filters.
     * This method supports the dynamic filtering logic in app.js.
     */
    public List<Asset> getFilteredAssets(String search, String category, String status) {
        List<Asset> assets = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(BASE_SELECT_SQL);
        sqlBuilder.append("WHERE 1=1 "); 

        // Prepare list for dynamic parameters
        List<Object> params = new ArrayList<>();

        // 1. Search filter (applies to asset name or tag)
        if (search != null && !search.isEmpty()) {
            sqlBuilder.append("AND (a.asset_name LIKE ? OR a.asset_tag LIKE ?) ");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        // 2. Category filter
        if (category != null && !category.isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sqlBuilder.append("AND a.category = ? ");
            params.add(category);
        }

        // 3. Status filter
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sqlBuilder.append("AND a.status = ? ");
            params.add(status);
        }

        sqlBuilder.append("ORDER BY a.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            // Set dynamic parameters based on list
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                assets.add(extractAssetFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assets;
    }

    public List<Asset> getAssetsByStatus(String status) {
        List<Asset> assets = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE a.status = ? ORDER BY a.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                assets.add(extractAssetFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assets;
    }

    public List<Asset> getAssetsByUser(int userId) {
        List<Asset> assets = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE a.assigned_to = ? ORDER BY a.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                assets.add(extractAssetFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assets;
    }

    public Asset getAssetById(int assetId) {
        String sql = BASE_SELECT_SQL + "WHERE a.asset_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, assetId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractAssetFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- WRITE/UPDATE OPERATIONS ---

    public boolean createAsset(Asset asset) {
        String sql = "INSERT INTO assets (asset_name, asset_tag, category, description, " +
                     "purchase_date, purchase_cost, current_value, status, location) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, asset.getAssetName());
            stmt.setString(2, asset.getAssetTag());
            stmt.setString(3, asset.getCategory());
            stmt.setString(4, asset.getDescription());
            stmt.setDate(5, asset.getPurchaseDate());
            stmt.setBigDecimal(6, asset.getPurchaseCost());
            stmt.setBigDecimal(7, asset.getCurrentValue());
            stmt.setString(8, asset.getStatus());
            stmt.setString(9, asset.getLocation());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAsset(Asset asset) {
        String sql = "UPDATE assets SET asset_name = ?, category = ?, description = ?, " +
                     "purchase_date = ?, purchase_cost = ?, current_value = ?, " +
                     "status = ?, location = ?, assigned_to = ? WHERE asset_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, asset.getAssetName());
            stmt.setString(2, asset.getCategory());
            stmt.setString(3, asset.getDescription());
            stmt.setDate(4, asset.getPurchaseDate());
            stmt.setBigDecimal(5, asset.getPurchaseCost());
            stmt.setBigDecimal(6, asset.getCurrentValue());
            stmt.setString(7, asset.getStatus());
            stmt.setString(8, asset.getLocation());
            
            if (asset.getAssignedTo() != null) {
                // Set the assigned user ID
                stmt.setInt(9, asset.getAssignedTo());
            } else {
                // Set to NULL in the database if asset is unassigned
                stmt.setNull(9, Types.INTEGER);
            }
            stmt.setInt(10, asset.getAssetId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAsset(int assetId) {
        // Note: This performs a hard DELETE. Consider logical deletion (e.g., setting is_active=FALSE) 
        // if you need to keep historical records, but hard delete is used here as provided initially.
        String sql = "DELETE FROM assets WHERE asset_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, assetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Assigns an asset to a user and updates its status to 'ASSIGNED'.
     * Used in the Request approval transaction.
     */
    public boolean assignAsset(int assetId, int userId) {
        String sql = "UPDATE assets SET assigned_to = ?, status = 'ASSIGNED' WHERE asset_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, assetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Unassigns an asset and updates its status to 'AVAILABLE'.
     * Used in the Request return/approval transaction.
     */
    public boolean unassignAsset(int assetId) {
        String sql = "UPDATE assets SET assigned_to = NULL, status = 'AVAILABLE' WHERE asset_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, assetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- HELPER METHOD ---

    private Asset extractAssetFromResultSet(ResultSet rs) throws SQLException {
        Asset asset = new Asset();
        asset.setAssetId(rs.getInt("asset_id"));
        asset.setAssetName(rs.getString("asset_name"));
        asset.setAssetTag(rs.getString("asset_tag"));
        asset.setCategory(rs.getString("category"));
        asset.setDescription(rs.getString("description"));
        asset.setPurchaseDate(rs.getDate("purchase_date"));
        asset.setPurchaseCost(rs.getBigDecimal("purchase_cost"));
        asset.setCurrentValue(rs.getBigDecimal("current_value"));
        asset.setStatus(rs.getString("status"));
        asset.setLocation(rs.getString("location"));
        
        int assignedTo = rs.getInt("assigned_to");
        if (!rs.wasNull()) {
            asset.setAssignedTo(assignedTo);
            // Assumes the SQL includes the joined column 'assigned_to_name'
            asset.setAssignedToName(rs.getString("assigned_to_name")); 
        }
        
        asset.setCreatedAt(rs.getTimestamp("created_at"));
        asset.setUpdatedAt(rs.getTimestamp("updated_at"));
        return asset;
    }
}