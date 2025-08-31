package io.github.junhyoung.nearbuy.auth.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ResponseWriterUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseWriterUtil() {}

    public static void writeJson(HttpServletResponse response, Object value) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(value));
        response.getWriter().flush();
    }
}
