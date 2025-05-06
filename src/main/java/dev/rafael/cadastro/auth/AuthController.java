package dev.rafael.cadastro.auth;

import dev.rafael.cadastro.users.User;
import dev.rafael.cadastro.users.UserRepository;
import dev.rafael.cadastro.exceptions.GenericException;
import dev.rafael.cadastro.config.ResponseSucessAPI;
import dev.rafael.cadastro.infra.authetication.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login (@RequestBody AuthticationDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());
        return new ResponseSucessAPI<>(token,"Token criado com sucesso",HttpStatus.OK).toResponseEntity();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody AuthticationDTO data){

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

        return new ResponseSucessAPI<>(usr,"Usuário criado com sucesso!",HttpStatus.CREATED).toResponseEntity();
    }
}
