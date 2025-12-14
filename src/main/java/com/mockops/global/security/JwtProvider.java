package com.mockops.global.security;

import com.mockops.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration().toMillis());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Access Token 생성 (약관 동의 정보 포함)
     * @param userId 사용자 ID
     * @param tosAgreedVersion 동의한 이용약관 버전 (null 가능)
     * @param ppAgreedVersion 동의한 개인정보처리방침 버전 (null 가능)
     * @return JWT Access Token
     */
    public String generateAccessTokenWithConsent(Long userId, String tosAgreedVersion, String ppAgreedVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration().toMillis());

        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate);

        // 약관 동의 정보 추가 (null이 아닐 경우에만)
        if (tosAgreedVersion != null) {
            builder.claim("tos_agreed_version", tosAgreedVersion);
        }
        if (ppAgreedVersion != null) {
            builder.claim("pp_agreed_version", ppAgreedVersion);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration().toMillis());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * JWT에서 이용약관 동의 버전 추출
     * @param token Access Token
     * @return 이용약관 동의 버전 (없으면 null)
     */
    public String getTosAgreedVersionFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("tos_agreed_version", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JWT에서 개인정보처리방침 동의 버전 추출
     * @param token Access Token
     * @return 개인정보처리방침 동의 버전 (없으면 null)
     */
    public String getPpAgreedVersionFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("pp_agreed_version", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            throw ErrorCode.EXPIRED_TOKEN.serviceException();
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰입니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (JwtException e) {
            log.error("JWT 검증에 실패했습니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        }
    }

    /**
     * 초대 토큰 생성 (7일 유효)
     * @param invitationId 초대장 ID
     * @param email 초대 대상 이메일
     * @param projectId 프로젝트 ID
     * @return JWT 토큰
     */
    public String generateInvitationToken(Long invitationId, String email, Long projectId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getInvitationTokenExpiration().toMillis());

        return Jwts.builder()
                .subject(String.valueOf(invitationId))
                .claim("email", email)
                .claim("projectId", projectId)
                .claim("type", "invitation")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 초대 토큰에서 초대장 ID 추출
     */
    public Long getInvitationIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 초대 토큰에서 이메일 추출
     */
    public String getEmailFromInvitationToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    /**
     * 초대 토큰에서 프로젝트 ID 추출
     */
    public Long getProjectIdFromInvitationToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("projectId", Long.class);
    }

    /**
     * 초대 토큰 유효성 검증
     */
    public boolean validateInvitationToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (!"invitation".equals(tokenType)) {
                log.error("초대 토큰이 아닙니다.");
                throw ErrorCode.INVALID_TOKEN.serviceException();
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 초대 토큰입니다.");
            throw ErrorCode.EXPIRED_TOKEN.serviceException();
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰입니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (JwtException e) {
            log.error("JWT 검증에 실패했습니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다.");
            throw ErrorCode.INVALID_TOKEN.serviceException();
        }
    }

    /**
     * Webhook JWT 토큰 생성 (30일 유효, 프로젝트별 Secret Key 사용)
     * @param projectId 프로젝트 ID
     * @param secretKey 프로젝트별 Webhook Secret Key (복호화된 원본)
     * @return JWT 토큰
     */
    public String generateWebhookToken(Long projectId, String secretKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getWebhookTokenExpiration().toMillis());

        SecretKey webhookKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(String.valueOf(projectId))
                .claim("type", "webhook")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(webhookKey)
                .compact();
    }

    /**
     * Webhook JWT 토큰 검증 (프로젝트별 Secret Key 사용)
     * @param token Webhook JWT 토큰
     * @param secretKey 프로젝트별 Webhook Secret Key (복호화된 원본)
     * @return 유효성 여부
     */
    public boolean validateWebhookToken(String token, String secretKey) {
        try {
            SecretKey webhookKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(webhookKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (!"webhook".equals(tokenType)) {
                log.error("Webhook 토큰이 아닙니다. type={}", tokenType);
                throw ErrorCode.INVALID_TOKEN.serviceException();
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 Webhook 토큰입니다. expiredAt={}", e.getClaims().getExpiration());
            throw ErrorCode.EXPIRED_TOKEN.serviceException();
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다. message={}", e.getMessage());
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰입니다. message={}", e.getMessage());
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (JwtException e) {
            log.error("Webhook JWT 검증에 실패했습니다. message={}", e.getMessage(), e);
            throw ErrorCode.INVALID_TOKEN.serviceException();
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다. message={}", e.getMessage());
            throw ErrorCode.INVALID_TOKEN.serviceException();
        }
    }

    /**
     * Webhook JWT 토큰에서 프로젝트 ID 추출
     * @param token Webhook JWT 토큰
     * @param secretKey 프로젝트별 Webhook Secret Key (복호화된 원본)
     * @return 프로젝트 ID
     */
    public Long getProjectIdFromWebhookToken(String token, String secretKey) {
        SecretKey webhookKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(webhookKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
}
