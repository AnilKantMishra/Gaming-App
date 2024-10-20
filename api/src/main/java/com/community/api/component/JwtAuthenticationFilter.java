package com.community.api.component;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.SanitizerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.broadleafcommerce.profile.core.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.persistence.EntityManager;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    private static final Pattern UNSECURED_URI_PATTERN = Pattern.compile(
            "^/api/v1/(account|otp|test|files/avisoftdocument/[^/]+/[^/]+|files/[^/]+|avisoftdocument/[^/]+|swagger-ui.html|swagger-resources|v2/api-docs|images|webjars).*"
    );
    private String apiKey="IaJGL98yHnKjnlhKshiWiy1IhZ+uFsKnktaqFX3Dvfg=";

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private CustomerService CustomerService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private SanitizerService sanitizerService;

    @Autowired
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try {
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            String requestBodyStr = requestBody.toString();

            if (sanitizerService.isMalicious(requestBodyStr)) {
                handleException(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request: Malicious content detected.");
                return;
            }

            BufferedRequestWrapper wrappedRequest = new BufferedRequestWrapper(request, requestBodyStr);
            String requestURI = wrappedRequest.getRequestURI();

            if (isUnsecuredUri(requestURI) || bypassimages(requestURI)) {
                chain.doFilter(wrappedRequest, response);
                return;
            }

            if (isApiKeyRequiredUri(wrappedRequest) && validateApiKey(wrappedRequest)) {
                chain.doFilter(wrappedRequest, response);
                return;
            }

            boolean responseHandled = authenticateUser(wrappedRequest, response);
            if (!responseHandled) {
                chain.doFilter(wrappedRequest, response);
            }

        } catch (ExpiredJwtException e) {
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is expired");
            logger.error("ExpiredJwtException caught: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid JWT token");
            exceptionHandling.handleException(e);
            logger.error("MalformedJwtException caught: {}", e.getMessage());
        } catch (Exception e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            exceptionHandling.handleException(e);
            logger.error("Exception caught: {}", e.getMessage());
        }
    }


    private boolean bypassimages(String requestURI) {
        return UNSECURED_URI_PATTERN.matcher(requestURI).matches();

    }

    private boolean isApiKeyRequiredUri(HttpServletRequest request) {

        String requestURI = request.getRequestURI();
        String path = requestURI.split("\\?")[0].trim();

        List<Pattern> bypassPatterns = Arrays.asList(
                Pattern.compile("^/api/v1/category-custom/get-products-by-category-id/\\d+$"),
                Pattern.compile("^/api/v1/category-custom/get-all-categories$")
        );

        boolean isBypassed = bypassPatterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
        return isBypassed;
    }

    private boolean validateApiKey(HttpServletRequest request) {
        String requestApiKey = request.getHeader("x-api-key");
        return apiKey.equals(requestApiKey);
    }

    private boolean isUnsecuredUri(String requestURI) {
        return requestURI.startsWith("/api/v1/account")
                || requestURI.startsWith("/api/v1/otp")
                || requestURI.startsWith("/api/v1/test")
                || requestURI.startsWith("/api/v1/files/avisoftdocument/**")
                || requestURI.startsWith("/api/v1/files/**")
                || requestURI.startsWith("/api/v1/avisoftdocument/**")
                || requestURI.startsWith("/api/v1/swagger-ui.html")
                || requestURI.startsWith("/api/v1/swagger-resources")
                || requestURI.startsWith("/api/v1/v2/api-docs")
                || requestURI.startsWith("/api/v1/images")
                || requestURI.startsWith("/api/v1/webjars");
    }


    private boolean authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            respondWithUnauthorized(response, "JWT token cannot be empty");
            return true;
        }

        if (customCustomerService == null) {
            respondWithUnauthorized(response, "CustomCustomerService is null");
            return true;
        }

        String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
        Long id = jwtUtil.extractId(jwt);

        if (id == null) {
            respondWithUnauthorized(response, "Invalid details in token");
            return true;
        }
        String ipAdress = request.getRemoteAddr();
        String User_Agent = request.getHeader("User-Agent");

        try {
            if (!jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                respondWithUnauthorized(response, "Invalid JWT token");
                return true;
            }
        } catch (ExpiredJwtException e) {
            jwtUtil.logoutUser(jwt);
            respondWithUnauthorized(response, "Token is expired");
            return true;
        }
        Customer customCustomer = null;
        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            customCustomer = CustomerService.readCustomerById(id);
            if (customCustomer != null && jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        customCustomer.getId(), null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return false;
            } else {
                respondWithUnauthorized(response, "Invalid data provided for this customer");
                return true;
            }
        }
        return false;
    }

    private void respondWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"UNAUTHORIZED\",\"status_code\":401,\"message\":\"" + message + "\"}");
            response.getWriter().flush();
        }
    }

    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(statusCode);
            response.setContentType("application/json");

            String status;
            if (statusCode == HttpServletResponse.SC_BAD_REQUEST) {
                status = "BAD_REQUEST";
            } else if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
                status = "UNAUTHORIZED";
            } else {
                status = "ERROR";
            }

            String jsonResponse = String.format(
                    "{\"status\":\"%s\",\"status_code\":%d,\"message\":\"%s\"}",
                    status,
                    statusCode,
                    message
            );
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        }
    }

}
