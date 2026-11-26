package org.example.apis.Controller;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import org.example.apis.Model.RolModel;
import org.example.apis.dao.PersonaRolDAO;
import org.example.apis.dao.RolDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/roles")
public class RolController {

    private final RolDAO roles;
    private final PersonaRolDAO asignaciones;

    public RolController(RolDAO roles, PersonaRolDAO asignaciones) {
        this.roles = roles;
        this.asignaciones = asignaciones;
    }

    @GetMapping
    public List<RolModel> listar() throws SQLException {
        return roles.listar();
    }

    @GetMapping("/{id}")
    public RolModel obtener(@PathVariable Long id) throws SQLException {
        return roles.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    @PostMapping
    public ResponseEntity<RolModel> crear(@RequestBody RolModel rol) throws SQLException {
        rol.setId(null);
        RolModel creado = roles.crear(rol);
        return ResponseEntity.created(URI.create("/roles/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public RolModel actualizar(@PathVariable Long id, @RequestBody RolModel cambios) throws SQLException {
        if (!roles.actualizar(id, cambios)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado");
        }
        return obtener(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) throws SQLException {
        if (roles.buscarPorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado");
        }
        if (asignaciones.tieneAsignacionesRol(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Elimina primero las asignaciones del rol");
        }
        roles.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
