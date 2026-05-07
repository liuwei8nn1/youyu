package com.youyu.auth.api;

import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.SecretKey;

import com.youyu.framework.context.UserInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Token提供者 - 负责Token的生成与验证
 */
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
	/**
	 *  获取 Access Token 有效期（秒）
	 */
	@Getter
	private final long accessTokenTtl;  // Access Token 有效期（秒）
	/**
	 *  获取 Refresh Token 有效期（秒）
	 */
	@Getter
	private final long refreshTokenTtl; // Refresh Token 有效期（秒）

    public JwtTokenProvider(String secret, long accessTokenTtl, long refreshTokenTtl) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    /**
     * 生成 Access Token
     *
     * @param userIdentityId 用户身份ID (sid)
     * @param userId   用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param roles 角色列表,号分隔
     * @param deviceId 设备ID
     * @return JWT Token字符串
     */
    public String generateAccessToken(Long userIdentityId, Long userId, String username, Integer userType, 
                                      String roles, Long deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(UserInfo.JWT_SID, userIdentityId);  // 新增 sid
        claims.put(UserInfo.JWT_USERID, userId);
        claims.put(UserInfo.JWT_USERNAME, username);
        claims.put(UserInfo.JWT_USERTYPE, userType);
        claims.put(UserInfo.JWT_ROLES, roles);
        claims.put(UserInfo.JWT_DEVICEID, deviceId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenTtl * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成 Refresh Token
     *
     * @param userIdentityId 用户身份ID (sid)
     * @param userId   用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param roles 角色列表,号分隔
     * @param deviceId 设备ID
     * @return JWT Token字符串
     */
    public String generateRefreshToken(Long userIdentityId, Long userId, String username, Integer userType,
                                       String roles, Long deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(UserInfo.JWT_SID, userIdentityId);  // 新增 sid
        claims.put(UserInfo.JWT_USERID, userId);
        claims.put(UserInfo.JWT_USERNAME, username);
        claims.put(UserInfo.JWT_USERTYPE, userType);
        claims.put(UserInfo.JWT_ROLES, roles);
        claims.put(UserInfo.JWT_DEVICEID, deviceId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenTtl * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证并解析Token
     *
     * @param token JWT Token字符串
     * @return Claims对象
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("JWT Token验证失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("无效的Token");
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(UserInfo.JWT_USERID, Long.class);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(UserInfo.JWT_USERNAME, String.class);
    }

    /**
     * 从Token中获取用户类型
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(UserInfo.JWT_USERTYPE, String.class);
    }

    /**
     * 从Token中获取角色列表
     */
    public String getRolesFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(UserInfo.JWT_ROLES, String.class);
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从Token中获取设备ID
     */
    public Long getDeviceIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(UserInfo.JWT_DEVICEID, Long.class);
    }

	public UserInfo getUserInfo(Claims claims) {
        UserInfo userInfo = new UserInfo();
        userInfo.setSid(claims.get(UserInfo.JWT_SID, Long.class));  // 新增 sid
        userInfo.setUserId(claims.get(UserInfo.JWT_USERID, Long.class));
        userInfo.setUserType(claims.get(UserInfo.JWT_USERTYPE, Integer.class));
        userInfo.setRoles(claims.get(UserInfo.JWT_ROLES, String.class));
        userInfo.setUsername(claims.get(UserInfo.JWT_USERNAME, String.class));
        userInfo.setDeviceId(claims.get(UserInfo.JWT_DEVICEID, Long.class));
        return userInfo;
    }

}
