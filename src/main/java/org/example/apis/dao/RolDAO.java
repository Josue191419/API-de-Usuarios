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
import org.example.apis.Model.RolModel;
import org.springframework.stereotype.Repository;

@Repository
public class RolDAO {

    private final Conexion conexion;

    public RolDAO(Conexion conexion) {
        this.conexion = conexion;
    }

    public List<RolModel> listar() throws SQLException {
        List<RolModel> roles = new ArrayList<>();
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, nombre, descripcion FROM roles_josue");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                roles.add(mapear(results));
            }
        }
        return roles;
    }

    public Optional<RolModel> buscarPorId(Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, nombre, descripcion FROM roles_josue WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(mapear(results)) : Optional.empty();
            }
        }
    }

    public RolModel crear(RolModel rol) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO roles_josue (nombre, descripcion) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, rol.getNombre());
            statement.setString(2, rol.getDescripcion());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se generó el id del rol");
                }
                rol.setId(keys.getLong(1));
            }
        }
        return rol;
    }

    public boolean actualizar(Long id, RolModel rol) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE roles_josue SET nombre = ?, descripcion = ? WHERE id = ?")) {
            statement.setString(1, rol.getNombre());
            statement.setString(2, rol.getDescripcion());
            statement.setLong(3, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM roles_josue WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private RolModel mapear(ResultSet results) throws SQLException {
        return new RolModel(results.getLong("id"), results.getString("nombre"), results.getString("descripcion"));
    }
}
