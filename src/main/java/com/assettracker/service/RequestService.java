package com.assettracker.service;

import java.util.List;
import java.util.stream.Collectors;

import com.assettracker.dao.AssetDAO;
import com.assettracker.dao.AssetRequestDAO;
import com.assettracker.model.Asset;
import com.assettracker.model.AssetRequest;
import com.assettracker.model.User;

public class RequestService {
    private AssetRequestDAO requestDAO;
    private AssetDAO assetDAO;
    private AuthService authService;

    public RequestService() {
        this.requestDAO = new AssetRequestDAO();
        this.assetDAO = new AssetDAO();
        this.authService = new AuthService();
    }

    public List<AssetRequest> getAllRequests(User requestingUser) {
        if (!authService.hasPermission(requestingUser, "APPROVE_REQUEST")) {
            return null;
        }
        
        return requestDAO.getAllRequests();
    }

    public List<AssetRequest> getRequestsByUser(int userId) {
        return requestDAO.getRequestsByUser(userId);
    }

    public List<AssetRequest> getPendingRequests(User requestingUser) {
        if (!authService.hasPermission(requestingUser, "APPROVE_REQUEST")) {
            return null;
        }
        
        return requestDAO.getPendingRequests();
    }

    public boolean createRequest(AssetRequest request, User requestingUser) {
        if (!authService.hasPermission(requestingUser, "REQUEST_ASSET")) {
            return false;
        }
        
        if (!validateRequest(request)) {
            return false;
        }
        
        // Check if asset is available for assignment requests
        if (request.getRequestType().equals("ASSIGNMENT")) {
            Asset asset = assetDAO.getAssetById(request.getAssetId());
            if (asset == null || !asset.getStatus().equals("AVAILABLE")) {
                return false;
            }
        }
        
        // Check if user already has an active asset for this asset
        if (request.getRequestType().equals("ASSIGNMENT")) {
            List<AssetRequest> userRequests = requestDAO.getRequestsByUser(request.getUserId());
            boolean hasPendingRequest = userRequests.stream()
                .anyMatch(r -> r.getAssetId() == request.getAssetId() && 
                              r.getStatus().equals("PENDING"));
            
            if (hasPendingRequest) {
                return false; // User already has a pending request for this asset
            }
        }
        
        return requestDAO.createRequest(request);
    }

    public boolean approveRequest(int requestId, User approver) {
        if (!authService.hasPermission(approver, "APPROVE_REQUEST")) {
            return false;
        }
        
        AssetRequest request = getRequestById(requestId);
        if (request == null || !request.getStatus().equals("PENDING")) {
            return false;
        }
        
        // Update request status
        boolean requestUpdated = requestDAO.updateRequestStatus(
            requestId, 
            "APPROVED", 
            approver.getUserId(), 
            "Approved by " + approver.getFullName()
        );
        
        if (!requestUpdated) {
            return false;
        }
        
        // Update asset assignment based on request type
        if (request.getRequestType().equals("ASSIGNMENT")) {
            return assetDAO.assignAsset(request.getAssetId(), request.getUserId());
        } else if (request.getRequestType().equals("RETURN")) {
            return assetDAO.unassignAsset(request.getAssetId());
        } else if (request.getRequestType().equals("MAINTENANCE")) {
            Asset asset = assetDAO.getAssetById(request.getAssetId());
            if (asset != null) {
                asset.setStatus("MAINTENANCE");
                return assetDAO.updateAsset(asset);
            }
        }
        
        return true;
    }

    public boolean rejectRequest(int requestId, User rejector, String reason) {
        if (!authService.hasPermission(rejector, "APPROVE_REQUEST")) {
            return false;
        }
        
        AssetRequest request = getRequestById(requestId);
        if (request == null || !request.getStatus().equals("PENDING")) {
            return false;
        }
        
        return requestDAO.updateRequestStatus(
            requestId, 
            "REJECTED", 
            rejector.getUserId(), 
            reason != null ? reason : "Rejected by " + rejector.getFullName()
        );
    }

    public boolean completeRequest(int requestId, User completedBy) {
        if (!authService.hasPermission(completedBy, "APPROVE_REQUEST")) {
            return false;
        }
        
        return requestDAO.updateRequestStatus(
            requestId, 
            "COMPLETED", 
            completedBy.getUserId(), 
            "Completed"
        );
    }

    public List<AssetRequest> getRequestsByStatus(String status) {
        List<AssetRequest> allRequests = requestDAO.getAllRequests();
        
        if (status == null || status.isEmpty()) {
            return allRequests;
        }
        
        return allRequests.stream()
            .filter(request -> request.getStatus().equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

    public List<AssetRequest> getRequestsByAsset(int assetId) {
        List<AssetRequest> allRequests = requestDAO.getAllRequests();
        
        return allRequests.stream()
            .filter(request -> request.getAssetId() == assetId)
            .collect(Collectors.toList());
    }

    private AssetRequest getRequestById(int requestId) {
        List<AssetRequest> allRequests = requestDAO.getAllRequests();
        
        return allRequests.stream()
            .filter(request -> request.getRequestId() == requestId)
            .findFirst()
            .orElse(null);
    }

    private boolean validateRequest(AssetRequest request) {
        if (request == null) {
            return false;
        }
        
        if (request.getUserId() <= 0 || request.getAssetId() <= 0) {
            return false;
        }
        
        if (request.getRequestType() == null || request.getRequestType().isEmpty()) {
            return false;
        }
        
        // Validate request type
        String type = request.getRequestType();
        if (!type.equals("ASSIGNMENT") && !type.equals("RETURN") && !type.equals("MAINTENANCE")) {
            return false;
        }
        
        return true;
    }

    public int getPendingRequestCount(User user) {
        if (user.getRole().equals("USER")) {
            List<AssetRequest> userRequests = requestDAO.getRequestsByUser(user.getUserId());
            return (int) userRequests.stream()
                .filter(r -> r.getStatus().equals("PENDING"))
                .count();
        } else {
            List<AssetRequest> pendingRequests = requestDAO.getPendingRequests();
            return pendingRequests.size();
        }
    }
}
