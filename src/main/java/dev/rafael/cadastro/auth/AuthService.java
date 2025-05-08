package dev.rafael.cadastro.auth;

import dev.rafael.cadastro.exceptions.GenericException;
import dev.rafael.cadastro.infra.authetication.TokenService;
import dev.rafael.cadastro.users.User;
import dev.rafael.cadastro.users.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    TokenService tokenService;

    public User register(AuthticationDTO data){

        if(userRepository.findByUsername(data.username())!=null) {
            throw new GenericException("Usuário já existe", HttpStatus.BAD_REQUEST);
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());

        User user = User.builder()
                .username(data.username())
                .password(encryptedPassword)
                .roles(new HashSet<>())
                .build();

        User usr = userRepository.save(user);

        return usr;
    }

    public LoginResponseDTO login(AuthticationDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var user = (User) auth.getPrincipal();
        String token = tokenService.generateToken(user);

        return new LoginResponseDTO(token, user);
    }

    public String validate(HttpServletRequest request){

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new GenericException("token invalido", HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);

        return tokenService.validateToken(token);

    }

}
