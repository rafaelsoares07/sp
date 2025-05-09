package dev.rafael.cadastro.infra.authetication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.rafael.cadastro.users.User;
import dev.rafael.cadastro.exceptions.GenericException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    public String generateToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256("SECRET");
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withExpiresAt(genExpirationToken())
                    .sign(algorithm);

            return token.isEmpty()?"":token;
        }catch (JWTCreationException exception){
            throw new GenericException("Erro ao gerar token", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public String validateToken(String token){
        try {
            token = token.trim().replace("\n", "").replace("\r", "");

            Algorithm algorithm = Algorithm.HMAC256("SECRET");
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        }catch (JWTVerificationException exception){
            System.out.println(exception.toString());
            return "";
        }
    }

    private Instant genExpirationToken(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}
