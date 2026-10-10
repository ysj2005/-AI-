package com.southwind.aispringboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.southwind.aispringboot.config.JwtConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class JwtTokenUtil implements ApplicationContextAware {
    private static final String ISSUER = "mental_health_assistant";
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUtil.applicationContext = applicationContext;
    }

    private static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    }

    public static String generateToken(long userId, String username, Integer roleType) {
        try {
            JwtConfig jwtConfig = getJwtConfig();
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            Date expiration = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());
            String token = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expiration)
                    .withIssuedAt(new Date())
                    .withIssuer(ISSUER)
                    .sign(algorithm);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("生成token失败：" + e);
        }
    }

    public static String extraTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header)) {
            JwtConfig jwtConfig = getJwtConfig();
            String tokenPrefix = jwtConfig.getTokenPrefix();
            if (header.startsWith(tokenPrefix)) {
                return header.substring(tokenPrefix.length());
            }
            return header;
        }
        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            System.out.println("Using token from 'token' header");
            return tokenHeader;
        }
        return null;
    }
    //获取当前token
    public static String getCurrentToken() {
      ServletRequestAttributes attributes =  (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if(attributes!=null){
            HttpServletRequest request = attributes.getRequest();
            String token = (String) request.getAttribute("jwtToken");
            if (token!=null){
                return token;
            }
            //备用方案，从请求头直接获取
          String headerToken = extraTokenFromRequest(request);
            return headerToken;
      }
      return null;
    }

    public static DecodedJWT verifyToken(String token) {
        try {
            JwtConfig jwtConfig = getJwtConfig();
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token验证失败：" + e.getMessage());
        }
    }

    public static Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }

    public static String getUsernameFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("username").asString();
    }

    public static Integer getRoleTypeFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("roleType").asInt();
    }

    public static boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
