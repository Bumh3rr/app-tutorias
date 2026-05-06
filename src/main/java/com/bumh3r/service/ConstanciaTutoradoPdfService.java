package com.bumh3r.service;

public interface ConstanciaTutoradoPdfService {
    byte[] generarConstanciaTutorado(Integer idTutorado, Integer idSemestre) throws Exception;
}
