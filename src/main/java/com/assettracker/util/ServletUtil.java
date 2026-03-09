package com.assettracker.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public class ServletUtil {

    /**
     * Sends JSON response with status code.
     */
    public static void sendJson(HttpServletResponse response, int status, String json)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(json);
    }

    /**
     * Sends an error in JSON format.
     */
    public static void sendError(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        String json = "{ \"success\": false, \"message\": \"" + message + "\" }";
        response.getWriter().write(json);
    }

    /**
     * Returns a simple success message JSON.
     */
    public static Map<String, String> createMessage(String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("success", "true");
        map.put("message", msg);
        return map;
    }
}
