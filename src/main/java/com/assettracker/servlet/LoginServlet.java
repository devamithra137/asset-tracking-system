package com.assettracker.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.assettracker.dao.UserDAO;
import com.assettracker.model.User;
import com.google.gson.Gson;

@WebServlet("/api/auth/*")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo(); // /login or /logout

        if (pathInfo == null) {
            sendError(response, "Invalid auth request");
            return;
        }

        if (pathInfo.equals("/login")) {
            handleLogin(request, response, out);
        } 
        else if (pathInfo.equals("/logout")) {
            handleLogout(request, response, out);
        }
        else {
            sendError(response, "Unknown auth path: " + pathInfo);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, Object> result = new HashMap<>();
        User user = userDAO.authenticate(username, password);

        if (user != null) {

            // ✅ Create session and store user details for authorization
            HttpSession session = request.getSession(true);

            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("username", user.getUsername());

            result.put("success", true);
            result.put("message", "Login successful");
            result.put("user", user);

            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            result.put("success", false);
            result.put("message", "Invalid username or password");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        out.print(gson.toJson(result));
        out.flush();
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Logged out successfully");

        out.print(gson.toJson(result));
        out.flush();
    }

    private void sendError(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
    }
}
