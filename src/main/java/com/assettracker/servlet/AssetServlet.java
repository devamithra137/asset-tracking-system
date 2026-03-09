package com.assettracker.servlet;

import com.assettracker.dao.AssetDAO;
import com.assettracker.model.Asset;
import com.assettracker.model.User;
import com.assettracker.util.ServletUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@WebServlet("/api/assets/*")
public class AssetServlet extends HttpServlet {

    private AssetDAO assetDAO;
    private Gson gson;

    @Override
    public void init() {
        assetDAO = new AssetDAO();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    }

    // ----------------- GET -----------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        User u = (User) request.getSession().getAttribute("user");
        if (u == null) {
            ServletUtil.sendError(response, 401, "Login required");
            return;
        }

        String path = request.getPathInfo();

        try {
            if (path == null || path.equals("/")) {
                String search = request.getParameter("search");
                String category = request.getParameter("category");
                String status = request.getParameter("status");

                List<Asset> list = assetDAO.getFilteredAssets(search, category, status);
                ServletUtil.sendJson(response, 200, gson.toJson(list));
                return;
            }

            String[] parts = path.split("/");
            if (parts.length >= 3 && parts[1].equalsIgnoreCase("user")) {
                int userId = Integer.parseInt(parts[2]);
                ServletUtil.sendJson(response, 200, gson.toJson(assetDAO.getAssetsByUser(userId)));
                return;
            }

            if (parts.length >= 3 && parts[1].equalsIgnoreCase("status")) {
                String statusVal = parts[2].toUpperCase();
                ServletUtil.sendJson(response, 200, gson.toJson(assetDAO.getAssetsByStatus(statusVal)));
                return;
            }

            if (parts.length >= 2) {
                int id = Integer.parseInt(parts[1]);
                Asset a = assetDAO.getAssetById(id);
                if (a != null)
                    ServletUtil.sendJson(response, 200, gson.toJson(a));
                else
                    ServletUtil.sendError(response, 404, "Asset not found");
            }

        } catch (Exception e) {
            ServletUtil.sendError(response, 500, e.getMessage());
        }
    }

    // ----------------- POST (CREATE) -----------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        User u = (User) request.getSession().getAttribute("user");
        if (u == null ||
                !(u.getRole().equals("ADMINISTRATOR") || u.getRole().equals("INVENTORY_MANAGER"))) {

            ServletUtil.sendError(response, 403, "Access denied");
            return;
        }

        try {
            Asset a;

            if (request.getContentType() != null && request.getContentType().contains("application/json")) {
                a = gson.fromJson(readBody(request), Asset.class);
            } else {
                a = new Asset();
                a.setAssetName(request.getParameter("assetName"));
                a.setAssetTag(request.getParameter("assetTag"));
                a.setCategory(request.getParameter("category"));
                a.setDescription(request.getParameter("description"));
                a.setLocation(request.getParameter("location"));
                a.setStatus(request.getParameter("status"));

                String d = request.getParameter("purchaseDate");
                if (d != null && !d.isEmpty()) a.setPurchaseDate(Date.valueOf(d));

                String pc = request.getParameter("purchaseCost");
                if (pc != null && !pc.isEmpty()) a.setPurchaseCost(new BigDecimal(pc));

                String cv = request.getParameter("currentValue");
                if (cv != null && !cv.isEmpty()) a.setCurrentValue(new BigDecimal(cv));
            }

            boolean ok = assetDAO.createAsset(a);

            if (ok)
                ServletUtil.sendJson(response, 201, gson.toJson(ServletUtil.createMessage("Asset created successfully")));
            else
                ServletUtil.sendError(response, 400, "Failed to create asset");

        } catch (Exception e) {
            ServletUtil.sendError(response, 500, e.getMessage());
        }
    }

    // ----------------- PUT (UPDATE) -----------------
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        User u = (User) request.getSession().getAttribute("user");
        if (u == null ||
                !(u.getRole().equals("ADMINISTRATOR") || u.getRole().equals("INVENTORY_MANAGER"))) {

            ServletUtil.sendError(response, 403, "Access denied");
            return;
        }

        String path = request.getPathInfo();
        if (path == null || path.split("/").length < 2) {
            ServletUtil.sendError(response, 400, "Asset ID required");
            return;
        }

        try {
            int id = Integer.parseInt(path.split("/")[1]);
            Asset a = gson.fromJson(readBody(request), Asset.class);
            a.setAssetId(id);

            if (a.getPurchaseCost() == null) a.setPurchaseCost(BigDecimal.ZERO);
            if (a.getCurrentValue() == null) a.setCurrentValue(BigDecimal.ZERO);

            boolean ok = assetDAO.updateAsset(a);

            if (ok)
                ServletUtil.sendJson(response, 200, gson.toJson(ServletUtil.createMessage("Asset updated")));
            else
                ServletUtil.sendError(response, 404, "Asset not found");

        } catch (Exception e) {
            ServletUtil.sendError(response, 500, e.getMessage());
        }
    }

    // ----------------- DELETE -----------------
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        User u = (User) request.getSession().getAttribute("user");
        if (u == null ||
                !(u.getRole().equals("ADMINISTRATOR") || u.getRole().equals("INVENTORY_MANAGER"))) {

            ServletUtil.sendError(response, 403, "Access denied");
            return;
        }

        String path = request.getPathInfo();
        if (path == null || path.split("/").length < 2) {
            ServletUtil.sendError(response, 400, "Asset ID required");
            return;
        }

        try {
            int id = Integer.parseInt(path.split("/")[1]);
            boolean ok = assetDAO.deleteAsset(id);

            if (ok)
                ServletUtil.sendJson(response, 200, gson.toJson(ServletUtil.createMessage("Asset deleted")));
            else
                ServletUtil.sendError(response, 404, "Asset not found");

        } catch (Exception e) {
            ServletUtil.sendError(response, 500, e.getMessage());
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        BufferedReader br = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }
}
