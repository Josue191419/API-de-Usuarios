package org.example.apis.Controller;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import org.example.apis.Model.PersonaRolModel;
import org.example.apis.dao.PersonaRolDAO;
import org.example.apis.dao.personaDAO;
import org.example.apis.dao.RolDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PersonaRolController {

    private final PersonaRolDAO asignaciones;
    private final personaDAO personas;
    private final RolDAO roles;

    public PersonaRolController(PersonaRolDAO asignaciones, personaDAO personas, RolDAO roles) {
        this.asignaciones = asignaciones;
        this.personas = personas;
        this.roles = roles;
    }

    @GetMapping("/persona-roles")
    public List<PersonaRolModel> listar() throws SQLException {
        return asignaciones.listar();
    }

    @GetMapping("/persona-roles/{id}")
    public PersonaRolModel obtener(@PathVariable Long id) throws SQLException {
        return asignaciones.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada"));
    }

    @PostMapping("/persona-roles")
    public ResponseEntity<PersonaRolModel> crear(@RequestBody PersonaRolModel asignacion) throws SQLException {
        validarReferencias(asignacion.getIdPersona(), asignacion.getIdRol());
        comprobarNoDuplicada(asignacion.getIdPersona(), asignacion.getIdRol());
        asignacion.setId(null);
        PersonaRolModel creada = asignaciones.crear(asignacion);
        return ResponseEntity.created(URI.create("/persona-roles/" + creada.getId())).body(creada);
    }

    @PutMapping("/persona-roles/{id}")
    public PersonaRolModel actualizar(@PathVariable Long id, @RequestBody PersonaRolModel cambios)
            throws SQLException {
        PersonaRolModel asignacion = obtener(id);
        validarReferencias(cambios.getIdPersona(), cambios.getIdRol());
        asignaciones.buscarPorPersonaYRol(cambios.getIdPersona(), cambios.getIdRol())
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "La persona ya tiene asignado ese rol");
                });
        asignacion.setIdPersona(cambios.getIdPersona());
        asignacion.setIdRol(cambios.getIdRol());
        asignaciones.actualizar(id, asignacion);
        return asignacion;
    }

    @DeleteMapping("/persona-roles/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) throws SQLException {
        if (!asignaciones.eliminar(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/personas/{personaId}/roles")
    public List<PersonaRolModel> listarPorPersona(@PathVariable Long personaId) throws SQLException {
        buscarPersona(personaId);
        return asignaciones.listarPorPersona(personaId);
    }

    @PostMapping("/personas/{personaId}/roles")
    public ResponseEntity<PersonaRolModel> asignarRol(
            @PathVariable Long personaId,
            @RequestBody AsignarRolRequest request) throws SQLException {
        if (request == null || request.idRol() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idRol es obligatorio");
        }
        validarReferencias(personaId, request.idRol());
        comprobarNoDuplicada(personaId, request.idRol());
        PersonaRolModel asignacion = asignaciones.crear(new PersonaRolModel(null, personaId, request.idRol()));
        return ResponseEntity.created(URI.create("/persona-roles/" + asignacion.getId())).body(asignacion);
    }

    @DeleteMapping("/personas/{personaId}/roles/{rolId}")
    public ResponseEntity<Void> quitarRol(@PathVariable Long personaId, @PathVariable Long rolId)
            throws SQLException {
        if (!asignaciones.eliminarPorPersonaYRol(personaId, rolId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada");
        }
        return ResponseEntity.noContent().build();
    }

    private void validarReferencias(Long personaId, Long rolId) throws SQLException {
        if (personaId == null || rolId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idPersona e idRol son obligatorios");
        }
        buscarPersona(personaId);
        if (roles.buscarPorId(rolId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado");
        }
    }

    private void buscarPersona(Long id) throws SQLException {
        if (personas.buscarPorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Persona no encontrada");
        }
    }

    private void comprobarNoDuplicada(Long personaId, Long rolId) throws SQLException {
        if (asignaciones.buscarPorPersonaYRol(personaId, rolId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La persona ya tiene asignado ese rol");
        }
    }

    public record AsignarRolRequest(Long idRol) {
    }
}
