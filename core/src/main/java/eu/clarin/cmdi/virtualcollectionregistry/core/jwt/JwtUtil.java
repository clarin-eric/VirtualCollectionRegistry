/*
 * Copyright (C) 2026 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 *
 * @author wilelb
 */
public class JwtUtil {
    
    private final SecretKey secret_key;
    
    public JwtUtil(String secret) {
        if(secret == null || secret.isEmpty() || secret.length() < 32) {
            throw new RuntimeException("Secret is required and must be at least 32 chracters long");
        }
        secret_key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Generates a JWT using the new v0.13.0 builder syntax.
     * @param username
     * @return 
     */
    public String createToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 60*1000); // 1 minute

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secret_key) // The library auto-detects HS256
                .compact();
    }

    /**
     * Verifies and parses a JWT using the new parserBuilder logic.
     * @param token
     * @return 
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secret_key) // Modern verification method
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
