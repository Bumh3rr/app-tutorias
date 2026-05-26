package com.bumh3r.exception;

public class RegistroInactivoExistenteException extends RuntimeException {

    private final String tipoEntidad;
    private final String campoConflicto;
    private final String valorConflicto;
    private final Integer idRegistroInactivo;

    public RegistroInactivoExistenteException(String tipoEntidad, String campoConflicto,
            String valorConflicto, Integer idRegistroInactivo) {
        super("El campo \"" + campoConflicto + "\" con valor \"" + valorConflicto +
              "\" ya pertenece a un/a " + tipoEntidad + " dado/a de baja (ID: " + idRegistroInactivo +
              "). Reactívalo/a en lugar de crear un registro nuevo.");
        this.tipoEntidad = tipoEntidad;
        this.campoConflicto = campoConflicto;
        this.valorConflicto = valorConflicto;
        this.idRegistroInactivo = idRegistroInactivo;
    }

    public String getTipoEntidad() { return tipoEntidad; }
    public String getCampoConflicto() { return campoConflicto; }
    public String getValorConflicto() { return valorConflicto; }
    public Integer getIdRegistroInactivo() { return idRegistroInactivo; }
}
