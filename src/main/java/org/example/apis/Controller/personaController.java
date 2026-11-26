package org.example.apis.Controller;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import org.example.apis.Model.personaModel;
import org.example.apis.dao.PersonaRolDAO;
import org.example.apis.dao.personaDAO;
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
@RequestMapping("/personas")
public class personaController {

    private final personaDAO personas;
    private final PersonaRolDAO asignaciones;

    public personaController(personaDAO personas, PersonaRolDAO asignaciones) {
        this.personas = personas;
        this.asignaciones = asignaciones;
    }

    @GetMapping
    public List<personaModel> listar() throws SQLException {
        return personas.listar();
    }

    @GetMapping("/{id}")
    public personaModel obtener(@PathVariable Long id) throws SQLException {
        return personas.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Persona no encontrada"));
    }

    @PostMapping
    public ResponseEntity<personaModel> crear(@RequestBody personaModel persona) throws SQLException {
        persona.setId(null);
        personaModel creada = personas.crear(persona);
        return ResponseEntity.created(URI.create("/personas/" + creada.getId())).body(creada);
    }

    @PutMapping("/{id}")
    public personaModel actualizar(@PathVariable Long id, @RequestBody personaModel cambios) throws SQLException {
        if (!personas.actualizar(id, cambios)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Persona no encontrada");
        }
        return obtener(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) throws SQLException {
        if (personas.buscarPorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Persona no encontrada");
        }
        if (asignaciones.tieneAsignacionesPersona(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Elimina primero los roles asignados a la persona");
        }
        personas.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
