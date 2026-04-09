package com.example.cardtransferrest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LogFilter extends OncePerRequestFilter {
    private static final String REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(REQUEST_ID, requestId);

        try {
            log.info("Started process request with {} : {}", REQUEST_ID, requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
