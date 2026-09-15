package reginaldo.orbit.api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    public String gerarToken (String email) {
        return gerarToken(email, null);
    }

    public String gerarToken (String email, UUID userId) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        var builder = JWT.create()
                .withSubject(email)
                .withExpiresAt(Date.from(instant()));
        if (userId != null) {
            builder = builder.withClaim("userId", userId.toString());
        }
        return builder.sign(algorithm);
    }

    private Instant instant () {
        return Instant.now().plus(1, ChronoUnit.HOURS);
    }

    private DecodedJWT verifyToken (String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);

        return decodedJWT;
    }

    public DecodedJWT validarToken (String token) {
        return verifyToken(token);
    }
}
