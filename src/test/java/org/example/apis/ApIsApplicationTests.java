package org.example.apis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApIsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limpiarDatos() {
        jdbc.update("DELETE FROM persona_rol_josue");
        jdbc.update("DELETE FROM usuarios_josue");
        jdbc.update("DELETE FROM roles_josue");
        jdbc.update("DELETE FROM personas_josue");
    }

    @Test
    void permiteCrearConsultarYEliminarUnaAsignacionDeRol() throws Exception {
        String persona = mockMvc.perform(post("/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","apellidos":"Lopez","correo":"ana@example.com","telefono":"5551234"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long personaId = extraerId(persona);

        String rol = mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Administrador","descripcion":"Gestiona el sistema"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long rolId = extraerId(rol);

        mockMvc.perform(post("/personas/{personaId}/roles", personaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idRol\":" + rolId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPersona").value(personaId))
                .andExpect(jsonPath("$.idRol").value(rolId));

        mockMvc.perform(get("/personas/{personaId}/roles", personaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRol").value(rolId));

        mockMvc.perform(delete("/personas/{personaId}/roles/{rolId}", personaId, rolId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/roles/{id}", rolId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/personas/{id}", personaId)).andExpect(status().isNoContent());
    }

    @Test
    void permiteConsultarActualizarYEliminarPersonasYRoles() throws Exception {
        String persona = mockMvc.perform(post("/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","apellidos":"Lopez","correo":"ana@example.com","telefono":"5551234"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long personaId = extraerId(persona);

        mockMvc.perform(get("/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
        mockMvc.perform(put("/personas/{id}", personaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Maria","apellidos":"Lopez","correo":"ana@example.com","telefono":"5551234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Maria"));
        mockMvc.perform(get("/personas/{id}", personaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Maria"));

        String rol = mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Editor","descripcion":"Edita contenido"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long rolId = extraerId(rol);

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Editor"));
        mockMvc.perform(put("/roles/{id}", rolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Editor principal","descripcion":"Edita contenido"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Editor principal"));
        mockMvc.perform(get("/roles/{id}", rolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Editor principal"));
        mockMvc.perform(delete("/roles/{id}", rolId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/personas/{id}", personaId)).andExpect(status().isNoContent());
    }

    @Test
    void almacenaPasswordCifradoYValidaElLoginYEstado() throws Exception {
        String usuario = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"12345"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long usuarioId = extraerId(usuario);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"12345"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Autenticación exitosa"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/usuarios/{id}", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","estado":"inactivo"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"12345"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private long extraerId(String json) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(json)
                .get("id")
                .asLong();
    }
}
