package tuf.webscaf.app.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    public UUID getAUID(String token) throws Exception {
        Claims claims = getAllClaimsFromToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public UUID getCompany(String token) throws Exception{
        Claims claims = getAllClaimsFromToken(token);
        return UUID.fromString(claims.get("companyUuid",String.class));
    }

    public UUID getBranch(String token) throws Exception{
        Claims claims = getAllClaimsFromToken(token);
        return UUID.fromString(claims.get("branchUuid",String.class));
    }

    public String getType(String token) throws Exception{
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("type",String.class);
    }


    public Claims getAllClaimsFromToken(String token) throws Exception {
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(this.secret.getBytes()))
                .parseClaimsJws(token)
                .getBody();
    }
}