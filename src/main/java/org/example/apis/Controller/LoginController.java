package org.example.apis.Controller;

import java.sql.SQLException;
import org.example.apis.dao.UsuarioDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final UsuarioDAO usuarios;
    private final PasswordEncoder passwordEncoder;

    public LoginController(UsuarioDAO usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws SQLException {
        if (request == null || request.username() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(new LoginResponse("username y password son obligatorios"));
        }

        boolean valido = usuarios.buscarPorUsername(request.username())
                .filter(usuario -> "activo".equalsIgnoreCase(usuario.getEstado()))
                .filter(usuario -> passwordEncoder.matches(request.password(), usuario.getPassword()))
                .isPresent();
        if (!valido) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Usuario o contraseña incorrectos, o usuario inactivo"));
        }
        return ResponseEntity.ok(new LoginResponse("Autenticación exitosa"));
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String mensaje) {
    }
}
