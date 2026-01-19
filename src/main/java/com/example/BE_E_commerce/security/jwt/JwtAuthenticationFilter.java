package com.example.BE_E_commerce.security.jwt;

import com.example.BE_E_commerce.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.lang.NonNull;
import org. springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core. context.SecurityContextHolder;
import org.springframework.security.core. userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org. springframework.web.filter.OncePerRequestFilter;

import java. io.IOException;

import static com.example.BE_E_commerce.constant.RedisKeyConstants.tokenBlacklistKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Get JWT token from request
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // ← THÊM: Check if token is blacklisted
                if (isTokenBlacklisted(jwt)) {
                    log.warn("Token is blacklisted:  {}", jwt. substring(0, 20) + "...");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Validate token
                if (jwtTokenProvider.validateToken(jwt)) {
                    // Get username from token
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);

                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Create authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails. getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Set authentication for user: {} ({})",
                            ((UserDetailsImpl) userDetails).getUsername(),
                            ((UserDetailsImpl) userDetails).getEmail()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken. substring(7); // Remove "Bearer " prefix
        }

        return null;
    }

    /**
     * Check if token is blacklisted
     */
    private boolean isTokenBlacklisted(String token) {
        return redisService.hasKey(tokenBlacklistKey(token));
    }
}