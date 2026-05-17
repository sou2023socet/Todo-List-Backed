package com.example.todolist.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Wrap the request to cache the body
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Store wrappers in request attributes for later use
        request.setAttribute("requestWrapper", requestWrapper);
        request.setAttribute("responseWrapper", responseWrapper);

        // Log incoming request
        logIncomingRequest(requestWrapper);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Get the wrappers from request attributes
        ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request.getAttribute("requestWrapper");
        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) request.getAttribute("responseWrapper");

        if (requestWrapper != null && responseWrapper != null) {
            // Log outgoing response
            logOutgoingResponse(requestWrapper, responseWrapper, ex);

            // Copy the cached response back to the original response
            try {
                responseWrapper.copyBodyToResponse();
            } catch (IOException e) {
                logger.error("Error copying response body", e);
            }
        }
    }

    private void logIncomingRequest(ContentCachingRequestWrapper request) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("REQUEST: ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURI());

        // Add query parameters if present
        if (request.getQueryString() != null) {
            logMessage.append("?").append(request.getQueryString());
        }

        logMessage.append(" | Remote: ").append(request.getRemoteAddr())
                .append(" | User-Agent: ").append(request.getHeader("User-Agent"));

        // Log headers (excluding sensitive ones)
        logMessage.append(" | Headers: {");
        Enumeration<String> headerNames = request.getHeaderNames();
        boolean first = true;
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip sensitive headers
            if (!headerName.equalsIgnoreCase("authorization") &&
                !headerName.equalsIgnoreCase("cookie") &&
                !headerName.toLowerCase().contains("password")) {
                if (!first) logMessage.append(", ");
                logMessage.append(headerName).append("=").append(request.getHeader(headerName));
                first = false;
            }
        }
        logMessage.append("}");

        // Log request body (excluding sensitive content)
        String requestBody = getRequestBody(request);
        if (requestBody != null && !requestBody.isEmpty()) {
            // Mask sensitive data in request body
            String maskedBody = maskSensitiveData(requestBody);
            logMessage.append(" | Body: ").append(maskedBody);
        }

        logger.info(logMessage.toString());
    }

    private void logOutgoingResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, Exception ex) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("RESPONSE: ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURI())
                .append(" | Status: ")
                .append(response.getStatus());

        // Log response headers
        logMessage.append(" | Response-Headers: {");
        boolean first = true;
        for (String headerName : response.getHeaderNames()) {
            if (!first) logMessage.append(", ");
            logMessage.append(headerName).append("=").append(response.getHeader(headerName));
            first = false;
        }
        logMessage.append("}");

        // Log response body
        String responseBody = getResponseBody(response);
        if (responseBody != null && !responseBody.isEmpty()) {
            logMessage.append(" | Body: ").append(responseBody);
        }

        // Log exception if present
        if (ex != null) {
            logMessage.append(" | Exception: ").append(ex.getClass().getSimpleName())
                    .append(" - ").append(ex.getMessage());
            logger.error(logMessage.toString(), ex);
        } else {
            // Determine log level based on status code
            int statusCode = response.getStatus();
            if (statusCode >= 400) {
                logMessage.append(" | Reason: ");
                if (statusCode == 400) logMessage.append("Bad Request - Invalid input data");
                else if (statusCode == 401) logMessage.append("Unauthorized - Authentication required");
                else if (statusCode == 403) logMessage.append("Forbidden - Insufficient permissions");
                else if (statusCode == 404) logMessage.append("Not Found - Resource does not exist");
                else if (statusCode == 409) logMessage.append("Conflict - Resource already exists");
                else if (statusCode == 422) logMessage.append("Unprocessable Entity - Validation failed");
                else if (statusCode >= 500) logMessage.append("Internal Server Error - Server-side error");
                else logMessage.append("Client Error");

                logger.warn(logMessage.toString());
            } else if (statusCode >= 300) {
                logMessage.append(" | Reason: Redirection");
                logger.info(logMessage.toString());
            } else {
                logger.info(logMessage.toString());
            }
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String maskSensitiveData(String data) {
        if (data == null) return null;

        // Mask password fields
        data = data.replaceAll("(\"password\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
        data = data.replaceAll("(\"password\"\\s*:\\s*')[^']*'", "$1***'");

        // Mask authorization tokens
        data = data.replaceAll("(\"authorization\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
        data = data.replaceAll("(\"token\"\\s*:\\s*\")[^\"]*\"", "$1***\"");

        return data;
    }
}