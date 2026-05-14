package com.bumh3r.service;

public interface ConstanciaTutorPdfService {
    byte[] generarConstanciaTutor(Integer idTutor, Integer idSemestre) throws Exception;
    String validar(Integer idTutor, Integer idSemestre);
}
