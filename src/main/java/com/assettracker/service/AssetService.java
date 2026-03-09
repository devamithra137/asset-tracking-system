package com.assettracker.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.assettracker.dao.AssetDAO;
import com.assettracker.model.Asset;
import com.assettracker.model.User;

public class AssetService {
    private AssetDAO assetDAO;
    private AuthService authService;

    public AssetService() {
        this.assetDAO = new AssetDAO();
        this.authService = new AuthService();
    }

    public List<Asset> getAllAssets(User requestingUser) {
        if (requestingUser == null) {
            return null;
        }
        
        if (authService.hasPermission(requestingUser, "VIEW_REPORTS")) {
            return assetDAO.getAllAssets();
        }
        
        return null;
    }

    public List<Asset> getAvailableAssets() {
        return assetDAO.getAssetsByStatus("AVAILABLE");
    }

    public List<Asset> getAssetsByUser(int userId) {
        return assetDAO.getAssetsByUser(userId);
    }

    public Asset getAssetById(int assetId) {
        return assetDAO.getAssetById(assetId);
    }

    public boolean createAsset(Asset asset, User createdBy) {
        if (!authService.hasPermission(createdBy, "CREATE_ASSET")) {
            return false;
        }
        
        if (!validateAsset(asset)) {
            return false;
        }
        
        return assetDAO.createAsset(asset);
    }

    public boolean updateAsset(Asset asset, User updatedBy) {
        if (!authService.hasPermission(updatedBy, "EDIT_ASSET")) {
            return false;
        }
        
        if (!validateAsset(asset)) {
            return false;
        }
        
        return assetDAO.updateAsset(asset);
    }

    public boolean deleteAsset(int assetId, User deletedBy) {
        if (!authService.hasPermission(deletedBy, "DELETE_ASSET")) {
            return false;
        }
        
        Asset asset = assetDAO.getAssetById(assetId);
        if (asset != null && asset.getStatus().equals("ASSIGNED")) {
            // Cannot delete assigned assets
            return false;
        }
        
        return assetDAO.deleteAsset(assetId);
    }

    public boolean assignAssetToUser(int assetId, int userId, User assignedBy) {
        if (!authService.hasPermission(assignedBy, "APPROVE_REQUEST")) {
            return false;
        }
        
        Asset asset = assetDAO.getAssetById(assetId);
        if (asset == null || !asset.getStatus().equals("AVAILABLE")) {
            return false;
        }
        
        return assetDAO.assignAsset(assetId, userId);
    }

    public boolean unassignAsset(int assetId, User unassignedBy) {
        if (!authService.hasPermission(unassignedBy, "APPROVE_REQUEST")) {
            return false;
        }
        
        return assetDAO.unassignAsset(assetId);
    }

    public List<Asset> searchAssets(String searchTerm) {
        List<Asset> allAssets = assetDAO.getAllAssets();
        
        if (searchTerm == null || searchTerm.isEmpty()) {
            return allAssets;
        }
        
        String lowerSearch = searchTerm.toLowerCase();
        
        return allAssets.stream()
            .filter(asset -> 
                asset.getAssetName().toLowerCase().contains(lowerSearch) ||
                asset.getAssetTag().toLowerCase().contains(lowerSearch) ||
                asset.getCategory().toLowerCase().contains(lowerSearch)
            )
            .collect(Collectors.toList());
    }

    public List<Asset> filterByCategory(String category) {
        List<Asset> allAssets = assetDAO.getAllAssets();
        
        if (category == null || category.isEmpty()) {
            return allAssets;
        }
        
        return allAssets.stream()
            .filter(asset -> asset.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }

    public boolean updateAssetStatus(int assetId, String newStatus, User updatedBy) {
        if (!authService.hasPermission(updatedBy, "EDIT_ASSET")) {
            return false;
        }
        
        Asset asset = assetDAO.getAssetById(assetId);
        if (asset == null) {
            return false;
        }
        
        asset.setStatus(newStatus);
        return assetDAO.updateAsset(asset);
    }

    public BigDecimal calculateTotalAssetValue() {
        List<Asset> assets = assetDAO.getAllAssets();
        BigDecimal total = BigDecimal.ZERO;
        
        for (Asset asset : assets) {
            if (asset.getCurrentValue() != null) {
                total = total.add(asset.getCurrentValue());
            }
        }
        
        return total;
    }

    private boolean validateAsset(Asset asset) {
        if (asset == null) {
            return false;
        }
        
        if (asset.getAssetName() == null || asset.getAssetName().isEmpty()) {
            return false;
        }
        
        if (asset.getAssetTag() == null || asset.getAssetTag().isEmpty()) {
            return false;
        }
        
        if (asset.getCategory() == null || asset.getCategory().isEmpty()) {
            return false;
        }
        
        if (asset.getPurchaseCost() != null && asset.getPurchaseCost().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        if (asset.getCurrentValue() != null && asset.getCurrentValue().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        return true;
    }
}
