package com.assettracker.model;

import java.sql.Timestamp;

public class AssetRequest {
    private int requestId;
    private int userId;
    private String userName;
    private int assetId;
    private String assetName;
    private String assetTag;
    private String requestType; // ASSIGNMENT, RETURN, MAINTENANCE
    private Timestamp requestDate;
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED
    private Integer approvedBy;
    private String approvedByName;
    private Timestamp approvedDate;
    private String notes;

    // Constructors
    public AssetRequest() {}

    public AssetRequest(int userId, int assetId, String requestType) {
        this.userId = userId;
        this.assetId = assetId;
        this.requestType = requestType;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public Timestamp getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Timestamp approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "AssetRequest{" +
                "requestId=" + requestId +
                ", userName='" + userName + '\'' +
                ", assetName='" + assetName + '\'' +
                ", requestType='" + requestType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
