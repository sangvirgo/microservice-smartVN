package com.smartvn.admin_service.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Lớp tiện ích để xử lý các hoạt động liên quan đến JWT (JSON Web Token).
 * Bao gồm xác thực token và trích xuất thông tin từ token.
 */
@Component
@Slf4j
public class JwtUtils {

    // Lấy secret key từ file cấu hình
    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    /**
     * Lấy SecretKey dùng để ký và xác thực JWT.
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Trích xuất tất cả các claims từ một token JWT.
     * @param token Chuỗi JWT
     * @return Claims object chứa thông tin payload.
     * @throws ExpiredJwtException Nếu token đã hết hạn.
     * @throws UnsupportedJwtException Nếu token không được hỗ trợ.
     * @throws MalformedJwtException Nếu token bị lỗi định dạng.
     * @throws SignatureException Nếu chữ ký token không hợp lệ.
     * @throws IllegalArgumentException Nếu token là null hoặc rỗng.
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Xác thực chữ ký bằng secret key
                .build()
                .parseSignedClaims(token)     // Parse token
                .getPayload();                // Lấy phần payload (claims)
    }

    /**
     * Xác thực một token JWT.
     * Kiểm tra chữ ký, thời gian hết hạn và định dạng.
     * @param authToken Chuỗi JWT cần xác thực.
     * @return true nếu token hợp lệ, false nếu không.
     */
    public boolean validateToken(String authToken) {
        try {
            getClaimsFromToken(authToken); // Chỉ cần gọi parse là đủ để xác thực
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Lấy email (subject) từ token JWT.
     * @param token Chuỗi JWT.
     * @return Email của người dùng.
     */
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Lấy danh sách roles từ token JWT.
     * Giả định roles được lưu trong claim "roles" dưới dạng List<String>.
     * @param token Chuỗi JWT.
     * @return List các tên role (không có tiền tố ROLE_).
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        // Cần cast vì get trả về Object
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return roles;
    }

    /**
     * Lấy User ID từ token JWT.
     * Giả định User ID được lưu trong claim "id".
     * @param token Chuỗi JWT.
     * @return User ID dạng Long.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        // Claim "id" có thể là Integer hoặc Long tùy lúc tạo token
        Object idClaim = claims.get("id");
        if (idClaim == null) {
            throw new IllegalArgumentException("User ID claim not found in JWT");
        }
        if (idClaim instanceof Integer) {
            return ((Integer) idClaim).longValue();
        } else if (idClaim instanceof Long) {
            return (Long) idClaim;
        }
        // Xử lý trường hợp khác hoặc ném lỗi nếu cần
        throw new IllegalArgumentException("Invalid user ID claim type in JWT");
    }
}
