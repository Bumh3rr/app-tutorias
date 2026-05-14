package com.bumh3r.service;

public interface NombramientoCoordinadorPdfService {
    byte[] generarNombramiento(Integer idCoordinador) throws Exception;
    String validar(Integer idCoordinador);
}
