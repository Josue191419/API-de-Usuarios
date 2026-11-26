package org.example.apis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.example.apis.Config.Conexion;
import org.example.apis.Model.PersonaRolModel;
import org.springframework.stereotype.Repository;

@Repository
public class PersonaRolDAO {

    private final Conexion conexion;

    public PersonaRolDAO(Conexion conexion) {

        this.conexion = conexion;
    }

    public List<PersonaRolModel> listar() throws SQLException {
        List<PersonaRolModel> asignaciones = new ArrayList<>();
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, id_persona, id_rol FROM persona_rol_josue");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                asignaciones.add(mapear(results));
            }
        }
        return asignaciones;
    }

    public List<PersonaRolModel> listarPorPersona(Long personaId) throws SQLException {
        List<PersonaRolModel> asignaciones = new ArrayList<>();
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, id_persona, id_rol FROM persona_rol_josue WHERE id_persona = ?")) {
            statement.setLong(1, personaId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    asignaciones.add(mapear(results));
                }
            }
        }
        return asignaciones;
    }

    public Optional<PersonaRolModel> buscarPorId(Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, id_persona, id_rol FROM persona_rol_josue WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(mapear(results)) : Optional.empty();
            }
        }
    }

    public Optional<PersonaRolModel> buscarPorPersonaYRol(Long personaId, Long rolId) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, id_persona, id_rol FROM persona_rol_josue WHERE id_persona = ? AND id_rol = ?")) {
            statement.setLong(1, personaId);
            statement.setLong(2, rolId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(mapear(results)) : Optional.empty();
            }
        }
    }

    public PersonaRolModel crear(PersonaRolModel asignacion) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO persona_rol_josue (id_persona, id_rol) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, asignacion.getIdPersona());
            statement.setLong(2, asignacion.getIdRol());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se generó el id de la asignación");
                }
                asignacion.setId(keys.getLong(1));
            }
        }
        return asignacion;
    }

    public boolean actualizar(Long id, PersonaRolModel asignacion) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE persona_rol_josue SET id_persona = ?, id_rol = ? WHERE id = ?")) {
            statement.setLong(1, asignacion.getIdPersona());
            statement.setLong(2, asignacion.getIdRol());
            statement.setLong(3, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM persona_rol_josue WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorPersonaYRol(Long personaId, Long rolId) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM persona_rol_josue WHERE id_persona = ? AND id_rol = ?")) {
            statement.setLong(1, personaId);
            statement.setLong(2, rolId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean tieneAsignacionesPersona(Long personaId) throws SQLException {
        return existe("SELECT 1 FROM persona_rol_josue WHERE id_persona = ?", personaId);
    }

    public boolean tieneAsignacionesRol(Long rolId) throws SQLException {
        return existe("SELECT 1 FROM persona_rol_josue WHERE id_rol = ?", rolId);
    }

    private boolean existe(String sql, Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private PersonaRolModel mapear(ResultSet results) throws SQLException {
        return new PersonaRolModel(
                results.getLong("id"),
                results.getLong("id_persona"),
                results.getLong("id_rol"));
    }
}
