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
import org.example.apis.Model.UsuarioModel;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAO {

    private final Conexion conexion;

    public UsuarioDAO(Conexion conexion) {
        this.conexion = conexion;
    }

    public List<UsuarioModel> listar() throws SQLException {
        List<UsuarioModel> usuarios = new ArrayList<>();
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, username, password, estado FROM usuarios_josue");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                usuarios.add(mapear(results));
            }
        }
        return usuarios;
    }

    public Optional<UsuarioModel> buscarPorId(Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, username, password, estado FROM usuarios_josue WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(mapear(results)) : Optional.empty();
            }
        }
    }

    public Optional<UsuarioModel> buscarPorUsername(String username) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, username, password, estado FROM usuarios_josue WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(mapear(results)) : Optional.empty();
            }
        }
    }

    public boolean existeUsername(String username) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM usuarios_josue WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    public UsuarioModel crear(UsuarioModel usuario) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO usuarios_josue (username, password, estado) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, usuario.getUsername());
            statement.setString(2, usuario.getPassword());
            statement.setString(3, usuario.getEstado() == null || usuario.getEstado().isBlank()
                    ? "activo" : usuario.getEstado());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se generó el id del usuario");
                }
                usuario.setId(keys.getLong(1));
            }
        }
        return usuario;
    }

    public boolean actualizar(Long id, UsuarioModel usuario) throws SQLException {
        String sql = usuario.getPassword() == null
                ? "UPDATE usuarios_josue SET username = ?, estado = ? WHERE id = ?"
                : "UPDATE usuarios_josue SET username = ?, password = ?, estado = ? WHERE id = ?";
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usuario.getUsername());
            if (usuario.getPassword() == null) {
                statement.setString(2, usuario.getEstado());
                statement.setLong(3, id);
            } else {
                statement.setString(2, usuario.getPassword());
                statement.setString(3, usuario.getEstado());
                statement.setLong(4, id);
            }
            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        try (Connection connection = conexion.getConecion();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM usuarios_josue WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private UsuarioModel mapear(ResultSet results) throws SQLException {
        return new UsuarioModel(
                results.getLong("id"),
                results.getString("username"),
                results.getString("password"),
                results.getString("estado"));
    }
}
