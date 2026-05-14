package com.bumh3r.service;

public interface CarnetPdfService {
    byte[] generarCarnetTutorado(Integer idTutorado) throws Exception;
    String validar(Integer idTutorado);
}
