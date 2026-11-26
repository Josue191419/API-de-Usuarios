package org.example.apis.Model;

public class PersonaRolModel {

    private Long id;
    private Long idPersona;
    private Long idRol;

    public PersonaRolModel() {
    }

    public PersonaRolModel(Long id, Long idPersona, Long idRol) {
        this.id = id;
        this.idPersona = idPersona;
        this.idRol = idRol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }
}
