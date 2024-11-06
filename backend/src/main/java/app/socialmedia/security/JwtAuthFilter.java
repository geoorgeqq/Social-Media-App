package app.socialmedia.security;

import app.socialmedia.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private TokenService tokenService;
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (request.getRequestURI().equals("/api/users/register") || request.getRequestURI().equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token == null || !token.startsWith("Bearer")) {
            unauthorizedResponse(response);
            return;
        }
        token = token.substring(7);

        if (request.getRequestURI().equals("/api/auth/refresh")) {
            if (!validateRefreshToken(token)) {
                unauthorizedResponse(response);
                return;
            }
            String email = tokenService.getClaims(token).get("user_id").toString();
            String sessionToken = tokenService.generateToken(email, "session");
            String refreshToken = tokenService.generateToken(email, "refresh");
            tokenService.addTokensToCookies(sessionToken, refreshToken, response);
            log.info("Refresh token sent");
            filterChain.doFilter(request, response);
            return;
        }

        if (!validateSessionToken(token)) {
            unauthorizedResponse(response);
            log.error(" Invalid session token");
            return;
        }
        String email = tokenService.getClaims(token).get("user_id").toString();
        UserDetails userDetails = userService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, null);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private void unauthorizedResponse(HttpServletResponse response) {
        ResponseCookie invalidSessionTokenCookie = ResponseCookie.from("sessionToken", "").httpOnly(true).secure(false).path("/").maxAge(0) // Expire immediately
                .build();
        ResponseCookie invalidRefreshTokenCookie = ResponseCookie.from("refreshToken", "").httpOnly(true).secure(false).path("/").maxAge(0) // Expire immediately
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, invalidSessionTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, invalidRefreshTokenCookie.toString());
        response.setStatus(401);
    }

    private boolean validateToken(String token) {
        try {
            Map<String, Object> validatedSessionToken = tokenService.validateToken(token);
            if (!Objects.equals(validatedSessionToken.get("valid").toString(), "true")) {
                log.error(validatedSessionToken.get("error").toString());
                return false;
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean validateSessionToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        Claims claims = tokenService.getClaims(token);
        return claims.get("type").equals("session");
    }

    private boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        Claims claims = tokenService.getClaims(token);
        return claims.get("type").equals("refresh");
    }
}
