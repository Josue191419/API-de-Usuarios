package org.example.apis.Controller;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import org.example.apis.Model.UsuarioModel;
import org.example.apis.dao.UsuarioDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioDAO usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioDAO usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UsuarioModel> listar() throws SQLException {
        return usuarios.listar();
    }

    @GetMapping("/{id}")
    public UsuarioModel obtener(@PathVariable Long id) throws SQLException {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @PostMapping
    public ResponseEntity<UsuarioModel> crear(@RequestBody UsuarioModel usuario) throws SQLException {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()
                || usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username y password son obligatorios");
        }
        if (usuarios.existeUsername(usuario.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe");
        }
        usuario.setId(null);
        if (usuario.getEstado() == null || usuario.getEstado().isBlank()) {
            usuario.setEstado("activo");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        UsuarioModel creado = usuarios.crear(usuario);
        return ResponseEntity.created(URI.create("/usuarios/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public UsuarioModel actualizar(@PathVariable Long id, @RequestBody UsuarioModel cambios) throws SQLException {
        UsuarioModel usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (cambios.getUsername() == null || cambios.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username es obligatorio");
        }
        if (!usuario.getUsername().equals(cambios.getUsername())
                && usuarios.existeUsername(cambios.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe");
        }
        usuario.setUsername(cambios.getUsername());
        if (cambios.getPassword() != null && !cambios.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(cambios.getPassword()));
        } else {
            usuario.setPassword(null);
        }
        if (cambios.getEstado() != null && !cambios.getEstado().isBlank()) {
            usuario.setEstado(cambios.getEstado());
        }
        if (!usuarios.actualizar(id, usuario)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return obtener(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) throws SQLException {
        if (!usuarios.eliminar(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return ResponseEntity.noContent().build();
    }
}
