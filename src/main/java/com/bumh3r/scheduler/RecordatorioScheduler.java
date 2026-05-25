package com.bumh3r.scheduler;

import com.bumh3r.service.NotificacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RecordatorioScheduler {

    @Autowired
    private NotificacionService notificacionService;

    @Value("${notificacion.recordatorio.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${notificacion.recordatorio.cron}", zone = "America/Mexico_City")
    public void ejecutarRecordatoriosSemanales() {
        if (!enabled) {
            log.info("Recordatorios semanales deshabilitados por configuracion");
            return;
        }
        log.info("=== Iniciando envio de recordatorios semanales ===");
        Map<String, Integer> resultado = notificacionService.enviarRecordatoriosSemanales();
        log.info("=== Recordatorios completados: {} ===", resultado);
    }
}
