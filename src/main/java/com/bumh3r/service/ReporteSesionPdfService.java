package com.bumh3r.service;

public interface ReporteSesionPdfService {
    byte[] generarReporteSesion(Integer idReporte) throws Exception;
    String validar(Integer idReporte);
}
