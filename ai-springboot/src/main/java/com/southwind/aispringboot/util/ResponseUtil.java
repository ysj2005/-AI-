package com.southwind.aispringboot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.southwind.aispringboot.common.Result;
import com.southwind.aispringboot.common.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeError(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(resultCode.getHttpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<?> result = Result.error(resultCode.getCode(), resultCode.getMsg());
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
    }

    public static void writeError(HttpServletResponse response, int httpStatus, String code, String msg) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<?> result = Result.error(code, msg);
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
    }

    public static void writeSuccess(HttpServletResponse response, Object data) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<?> result = Result.ok(data);
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
    }
}
