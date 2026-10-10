package com.southwind.aispringboot.util;

import com.southwind.aispringboot.common.ResultCode;
import com.southwind.aispringboot.config.SecurityConfig;
import com.southwind.aispringboot.entity.User;
import com.southwind.aispringboot.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthticationFilter extends OncePerRequestFilter {

    @Resource
    private UserMapper userMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUrl = request.getRequestURI();
        return SecurityConfig.isPublicPath(requestUrl);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();
        System.out.println("JWT Filter - URL: " + requestUrl + ", Method: " + requestMethod);

        String token = JwtTokenUtil.extraTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            SecurityContextHolder.clearContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }

        try {
            Long userId = JwtTokenUtil.getUserIdFromToken(token);
            String username = JwtTokenUtil.getUsernameFromToken(token);
            Integer roleType = JwtTokenUtil.getRoleTypeFromToken(token);
            System.out.println("JWT Token valid - userId: " + userId + ", username: " + username + ", roleType: " + roleType);

            User user = userMapper.selectById(userId);
            System.out.println("User from DB: " + user);

            if (user == null) {
                System.out.println("User not found, userId: " + userId);
                ResponseUtil.writeError(response, ResultCode.USER_NOT_EXIST);
                return;
            }

            if (!user.isActive()) {
                System.out.println("User is disabled, userId: " + userId);
                ResponseUtil.writeError(response, ResultCode.UNAUTHORIZED);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("jwtToken", token);
            System.out.println("User authenticated successfully: " + user.getUsername());

        } catch (Exception e) {
            System.out.println("JWT Token invalid: " + e.getMessage());
            SecurityContextHolder.clearContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
