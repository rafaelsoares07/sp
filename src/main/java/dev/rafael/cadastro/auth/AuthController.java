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

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseSucessAPI<String>> login (@RequestBody AuthticationDTO data){
        return new ResponseSucessAPI<>(authService.login(data),"Token criado com sucesso",HttpStatus.OK).toResponseEntity();
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseSucessAPI<User>> register(@RequestBody AuthticationDTO data){
        return new ResponseSucessAPI<>(authService.register(data),"Usuário criado com sucesso!",HttpStatus.CREATED).toResponseEntity();
    }
}
