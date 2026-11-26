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
import org.example.apis.Model.personaModel;
import org.springframework.stereotype.Repository;

@Repository
public class personaDAO {

    private final Conexion conexion;

    public personaDAO(Conexion conexion) {
        this.conexion = conexion;
    }

    public List<personaModel> listar() throws SQLException {
        String sql = "SELECT id, nombre, apellidos, correo, telefono FROM personas_josue";
        List<personaModel> personas = new ArrayList<>();
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                personas.add(mapear(results));
            }
        }
        return personas;
    }

    public Optional<personaModel> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, nombre, apellidos, correo, telefono FROM personas_josue WHERE id = ?";
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(mapear(results)) : Optional.empty();
            }
        }
    }

    public personaModel crear(personaModel persona) throws SQLException {
        String sql = "INSERT INTO personas_josue (nombre, apellidos, correo, telefono) VALUES (?, ?, ?, ?)";
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, persona.getNombre());
            statement.setString(2, persona.getApellidos());
            statement.setString(3, persona.getCorreo());
            statement.setString(4, persona.getTelefono());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se generó el id de la persona");
                }
                persona.setId(keys.getLong(1));
            }
        }
        return persona;
    }

    public boolean actualizar(Long id, personaModel persona) throws SQLException {
        String sql = "UPDATE personas_josue SET nombre = ?, apellidos = ?, correo = ?, telefono = ? WHERE id = ?";
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, persona.getNombre());
            statement.setString(2, persona.getApellidos());
            statement.setString(3, persona.getCorreo());
            statement.setString(4, persona.getTelefono());
            statement.setLong(5, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM personas_josue WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private personaModel mapear(ResultSet results) throws SQLException {
        personaModel persona = new personaModel();
        persona.setId(results.getLong("id"));
        persona.setNombre(results.getString("nombre"));
        persona.setApellidos(results.getString("apellidos"));
        persona.setCorreo(results.getString("correo"));
        persona.setTelefono(results.getString("telefono"));
        return persona;
    }
}
