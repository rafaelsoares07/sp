package dev.rafael.cadastro.auth;

import dev.rafael.cadastro.users.User;
import dev.rafael.cadastro.users.UserRepository;
import dev.rafael.cadastro.exceptions.GenericException;
import dev.rafael.cadastro.config.ResponseSucessAPI;
import dev.rafael.cadastro.infra.authetication.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/login")
    public ResponseEntity<ResponseSucessAPI<LoginResponseDTO>> login (@RequestBody AuthticationDTO data){
        return new ResponseSucessAPI<>(authService.login(data),"Token criado com sucesso",HttpStatus.OK).toResponseEntity();
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/register")
    public ResponseEntity<ResponseSucessAPI<User>> register(@RequestBody AuthticationDTO data){
        return new ResponseSucessAPI<>(authService.register(data),"Usuário criado com sucesso!",HttpStatus.CREATED).toResponseEntity();
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/validate/token")
    public ResponseEntity<ResponseSucessAPI<String>> validToken(HttpServletRequest request){
        return new ResponseSucessAPI<>(authService.validate(request),"Usuário criado com sucesso!",HttpStatus.OK).toResponseEntity();
    }
}
