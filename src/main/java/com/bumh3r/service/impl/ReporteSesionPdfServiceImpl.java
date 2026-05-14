package com.bumh3r.service.impl;

import com.bumh3r.entity.*;
import com.bumh3r.repository.*;
import com.bumh3r.service.ReporteSesionPdfService;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Primary
@Service
public class ReporteSesionPdfServiceImpl implements ReporteSesionPdfService {

    // ── Colores ────────────────────────────────────────────────────────────
    private static final Color NEGRO       = Color.BLACK;
    private static final Color AZUL_HEADER = new Color(26, 58, 92);
    private static final Color AZUL_VALOR  = new Color(0, 51, 153);
    private static final Color GRIS_TEXTO  = new Color(85, 85, 85);
    private static final Color ROJO_PIE    = new Color(139, 0, 0);
    private static final Color VERDE_PRES  = new Color(10, 92, 54);
    private static final Color ROJO_AUS    = new Color(132, 32, 41);
    private static final Color AZUL_REC    = new Color(0, 85, 165);
    private static final Color BLANCO      = Color.WHITE;
    private static final Color GRIS_FILA   = new Color(245, 248, 252);
    private static final Color AZUL_THEAD  = new Color(44, 62, 80);
    private static final Color VERDE_BG    = new Color(209, 231, 221);
    private static final Color AMARILLO_BG = new Color(255, 243, 205);
    private static final Color ROJO_BG     = new Color(248, 215, 218);
    private static final Color VERDE_FG    = new Color(10, 92, 54);
    private static final Color AMARILLO_FG = new Color(133, 100, 4);
    private static final Color ROJO_FG     = new Color(132, 32, 41);
    private static final Color GRIS_LABEL  = new Color(245, 248, 252);
    private static final Color GRIS_BORDER = new Color(150, 150, 150);

    // ── Fuentes ────────────────────────────────────────────────────────────
    private static final Font F_NORMAL_7 = new Font(Font.HELVETICA, 7,  Font.NORMAL, NEGRO);
    private static final Font F_NORMAL_8 = new Font(Font.HELVETICA, 8,  Font.NORMAL, NEGRO);
    private static final Font F_BOLD_7   = new Font(Font.HELVETICA, 7,  Font.BOLD,   NEGRO);
    private static final Font F_BOLD_8   = new Font(Font.HELVETICA, 8,  Font.BOLD,   NEGRO);
    private static final Font F_BOLD_13  = new Font(Font.HELVETICA, 13, Font.BOLD,   NEGRO);
    private static final Font F_ITALIC_7 = new Font(Font.HELVETICA, 7,  Font.ITALIC, GRIS_TEXTO);
    private static final Font F_VALOR    = new Font(Font.HELVETICA, 8,  Font.BOLD,   AZUL_VALOR);
    private static final Font F_BLANCO_8 = new Font(Font.HELVETICA, 8,  Font.BOLD,   BLANCO);
    private static final Font F_BLANCO_7 = new Font(Font.HELVETICA, 7,  Font.NORMAL, BLANCO);
    private static final Font F_VERDE    = new Font(Font.HELVETICA, 7,  Font.BOLD,   VERDE_PRES);
    private static final Font F_ROJO_AUS = new Font(Font.HELVETICA, 7,  Font.BOLD,   ROJO_AUS);
    private static final Font F_AZUL_REC = new Font(Font.HELVETICA, 7,  Font.BOLD,   AZUL_REC);
    private static final Font F_PIE      = new Font(Font.HELVETICA, 6,  Font.NORMAL, GRIS_TEXTO);

    @Autowired private IReporteSesionRepository reporteSesionRepository;
    @Autowired private IAsistenciaRepository     asistenciaRepository;
    @Autowired private IGrupoTutoradoRepository  grupoTutoradoRepository;

    // ══════════════════════════════════════════════════════════════════════
    @Override
    public byte[] generarReporteSesion(Integer idReporte) throws Exception {
        ReporteSesion reporte  = reporteSesionRepository.findById(idReporte).orElseThrow();
        Sesion   sesion    = reporte.getSesion();
        Grupo    grupo     = sesion   != null ? sesion.getGrupo()     : null;
        Tutor    tutor     = grupo    != null ? grupo.getTutor()      : null;
        Actividad actividad= sesion   != null ? sesion.getActividad() : null;
        Semestre semestre  = grupo    != null ? grupo.getSemestre()   : null;
        Carrera  carrera   = grupo    != null ? grupo.getCarrera()    : null;

        List<Asistencia> asistencias = sesion != null
                ? asistenciaRepository.findBySesion(sesion) : new ArrayList<>();

        Map<Integer, Asistencia> mapaAsistencia = asistencias.stream()
                .collect(Collectors.toMap(a -> a.getTutorado().getId(), a -> a, (a1, a2) -> a1));

        List<GrupoTutorado> grupoTutorados = grupo != null
                ? grupoTutoradoRepository.findByActivoAndGrupo(1, grupo) : new ArrayList<>();
        grupoTutorados.sort(Comparator.comparing(
                gt -> gt.getTutorado().getApellido() + gt.getTutorado().getNombre()));

        long totalAlumnos = grupoTutorados.size();
        long presentes    = asistencias.stream()
                .filter(a -> a.getPresente() != null && a.getPresente() == 1).count();
        long ausentes     = totalAlumnos - presentes;
        double pctAsist   = totalAlumnos > 0
                ? Math.round((presentes * 100.0 / totalAlumnos) * 10.0) / 10.0 : 0.0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String fechaImparticion = (sesion != null && sesion.getFechaImparticion() != null)
                ? sdf.format(sesion.getFechaImparticion()) : "—";
        String fechaEntrega = reporte.getFechaEntrega() != null
                ? sdf.format(reporte.getFechaEntrega()) : "—";
        String periodoMayus = semestre != null
                ? semestre.getPeriodo().toUpperCase() + " " + semestre.getAnio() : "—";
        String nombreTutor  = tutor != null
                ? tutor.getNombre() + " " + tutor.getApellido() : "Sin asignar";
        String nombreActividad = actividad != null ? actividad.getNombre() : "Sin actividad";
        String nombreCarrera   = carrera   != null ? carrera.getNombre()   : "—";

        // ── Documento ──────────────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 40, 40, 28, 24);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        doc.add(buildEncabezado());

        Paragraph pTitulo = new Paragraph("REPORTE DE SESIÓN DE TUTORÍA", F_BOLD_13);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        pTitulo.setSpacingAfter(2f);
        doc.add(pTitulo);

        Paragraph pPeriodo = new Paragraph();
        pPeriodo.setAlignment(Element.ALIGN_CENTER);
        pPeriodo.add(new Chunk("Programa Institucional de Tutorías (PIT) · ", F_NORMAL_7));
        pPeriodo.add(new Chunk(periodoMayus, F_VALOR));
        pPeriodo.setSpacingAfter(8f);
        doc.add(pPeriodo);

        // Secciones
        doc.add(buildSeccionI(sesion, nombreActividad, fechaImparticion, fechaEntrega, reporte));
        doc.add(buildSeccionII(tutor, grupo, nombreTutor, nombreCarrera, semestre));
        doc.add(buildSeccionIII(reporte));
        buildSeccionIV(doc, grupoTutorados, mapaAsistencia, totalAlumnos, presentes, ausentes, pctAsist);
        doc.add(buildSeccionV(reporte));
        doc.add(buildFirmas(nombreTutor.toUpperCase(), nombreCarrera));

        // Pie
        doc.add(new Chunk(new LineSeparator(1.5f, 100, ROJO_PIE, Element.ALIGN_CENTER, -2)));
        Paragraph pie = new Paragraph(
                "Av. José Francisco Ruiz Massieu No. 5, Colonia Villa Moderna, Chilpancingo de los Bravo,\n"
                + "Guerrero, México. Tel. (747) 45 4 1300, Ext. 1326 y 1327 · email: dda@chilpancingo.tecnm.mx"
                + " · http://chilpancingo.tecnm.mx", F_PIE);
        pie.setAlignment(Element.ALIGN_CENTER);
        doc.add(pie);

        doc.close();
        return baos.toByteArray();
    }

    @Override
    public String validar(Integer idReporte) {
        ReporteSesion reporte = reporteSesionRepository.findById(idReporte).orElse(null);
        if (reporte == null || !Integer.valueOf(1).equals(reporte.getActivo()))
            return "El reporte no existe o ha sido dado de baja del sistema.";

        Sesion sesion = reporte.getSesion();
        if (sesion == null)
            return "El reporte no tiene una sesión asociada.";

        if (!"REALIZADA".equals(sesion.getEstatusRegistro()))
            return "La sesión asociada al reporte no ha sido marcada como realizada.";

        List<Asistencia> asistencias = asistenciaRepository.findBySesion(sesion);
        if (asistencias.isEmpty())
            return "No se han registrado asistencias para esta sesión.";

        return null;
    }

    // ── Encabezado — tabla plana 5 col (evita bug de tablas anidadas OpenPDF) ─
    private PdfPTable buildEncabezado() {
        PdfPTable t = new PdfPTable(new float[]{22f, 16f, 14f, 21f, 40f});
        t.setWidthPercentage(100f);
        t.setSpacingAfter(4f);

        t.addCell(imgCell("/static/images/tecnm/sep_logo.png", 33f, Element.ALIGN_LEFT));

        PdfPCell gap = new PdfPCell(new Phrase(" "));
        gap.setBorder(Rectangle.NO_BORDER);
        t.addCell(gap);

        t.addCell(imgCell("/static/images/tecnm/tecnologico_nacional.png", 30f, Element.ALIGN_LEFT));

        PdfPCell spacer = new PdfPCell(new Phrase(" "));
        spacer.setBorder(Rectangle.NO_BORDER);
        t.addCell(spacer);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        rightCell.setPadding(2f);
        try (InputStream is = getClass().getResourceAsStream("/static/images/tecnm/emblema.png")) {
            if (is != null) {
                Image img = Image.getInstance(is.readAllBytes());
                img.scaleToFit(999f, 44f);
                img.setAlignment(Image.ALIGN_RIGHT);
                rightCell.addElement(img);
            }
        } catch (Exception ignored) {}
        Paragraph instPar = new Paragraph();
        instPar.setAlignment(Element.ALIGN_RIGHT);
        instPar.add(new Chunk("Instituto Tecnológico de Chilpancingo\n",
                new Font(Font.HELVETICA, 8, Font.BOLD, NEGRO)));
        instPar.add(new Chunk("Departamento de Desarrollo Académico",
                new Font(Font.HELVETICA, 7, Font.NORMAL, GRIS_TEXTO)));
        rightCell.addElement(instPar);
        t.addCell(rightCell);
        return t;
    }

    private PdfPCell imgCell(String resource, float maxHeight, int hAlign) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(hAlign);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2f);
        try (InputStream is = getClass().getResourceAsStream(resource)) {
            if (is != null) {
                Image img = Image.getInstance(is.readAllBytes());
                img.scaleToFit(999f, maxHeight);
                img.setAlignment(hAlign);
                cell.addElement(img);
            }
        } catch (Exception ignored) {}
        return cell;
    }

    // ── Sección I ──────────────────────────────────────────────────────────
    private PdfPTable buildSeccionI(Sesion sesion, String nombreActividad,
            String fechaImparticion, String fechaEntrega, ReporteSesion reporte) {
        PdfPTable t = new PdfPTable(new float[]{20f, 30f, 20f, 30f});
        t.setWidthPercentage(100);
        t.setSpacingAfter(5f);
        addSeccionHeader(t, "I. DATOS DE LA SESIÓN", 4);

        t.addCell(celdaLabel("Semana"));
        t.addCell(celdaValor(sesion != null ? "Semana " + sesion.getSemana() : "—"));
        t.addCell(celdaLabel("Fecha de impartición"));
        t.addCell(celdaValor(fechaImparticion));

        t.addCell(celdaLabel("Actividad PAT"));
        PdfPCell actCell = celdaValor(nombreActividad);
        actCell.setColspan(3);
        t.addCell(actCell);

        t.addCell(celdaLabel("Estatus sesión"));
        t.addCell(crearCeldaBadge(sesion != null ? sesion.getEstatusRegistro() : null));
        t.addCell(celdaLabel("Fecha de entrega"));
        t.addCell(celdaValor(fechaEntrega));

        t.addCell(celdaLabel("Estatus revisión"));
        t.addCell(crearCeldaBadge(reporte.getEstatusRevision()));
        t.addCell(celdaLabel("ID Reporte"));
        t.addCell(celdaValor("#" + reporte.getId()));

        return t;
    }

    // ── Sección II ─────────────────────────────────────────────────────────
    private PdfPTable buildSeccionII(Tutor tutor, Grupo grupo,
            String nombreTutor, String nombreCarrera, Semestre semestre) {
        PdfPTable t = new PdfPTable(new float[]{20f, 30f, 20f, 30f});
        t.setWidthPercentage(100);
        t.setSpacingAfter(5f);
        addSeccionHeader(t, "II. DATOS DEL TUTOR Y GRUPO", 4);

        t.addCell(celdaLabel("Tutor/a"));
        PdfPCell tutorCell = celdaValor(nombreTutor);
        tutorCell.setColspan(3);
        t.addCell(tutorCell);

        t.addCell(celdaLabel("No. Control"));
        t.addCell(celdaValor(tutor != null && tutor.getNumeroControl() != null
                ? tutor.getNumeroControl() : "—"));
        t.addCell(celdaLabel("Email"));
        PdfPCell emailCell = new PdfPCell(new Phrase(
                tutor != null && tutor.getEmail() != null ? tutor.getEmail() : "—",
                new Font(Font.HELVETICA, 7, Font.NORMAL, NEGRO)));
        emailCell.setPadding(4f);
        emailCell.setBorderColor(GRIS_BORDER);
        emailCell.setBorderWidth(0.3f);
        t.addCell(emailCell);

        t.addCell(celdaLabel("Grupo"));
        t.addCell(celdaValor(grupo != null ? grupo.getNombre() : "—"));
        t.addCell(celdaLabel("Aula / Horario"));
        String aulaHorario = "—";
        if (grupo != null) {
            String a = grupo.getAula()      != null ? grupo.getAula()      : "—";
            String d = grupo.getDiaSemana() != null ? grupo.getDiaSemana() : "";
            String h = grupo.getHorario()   != null ? grupo.getHorario()   : "";
            aulaHorario = a + (d.isBlank() && h.isBlank() ? "" : " · " + (d + " " + h).trim());
        }
        t.addCell(celdaValor(aulaHorario));

        t.addCell(celdaLabel("Carrera"));
        t.addCell(celdaValor(nombreCarrera));
        t.addCell(celdaLabel("Semestre"));
        t.addCell(celdaValor(semestre != null
                ? semestre.getPeriodo() + " " + semestre.getAnio() : "—"));

        return t;
    }

    // ── Sección III ────────────────────────────────────────────────────────
    private PdfPTable buildSeccionIII(ReporteSesion reporte) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingAfter(5f);
        addSeccionHeader(t, "III. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA", 1);

        String desc = reporte.getDescripcionActividad() != null
                ? reporte.getDescripcionActividad() : "Sin descripción registrada.";
        PdfPCell content = new PdfPCell(new Phrase(desc, F_NORMAL_8));
        content.setPadding(6f);
        content.setMinimumHeight(52f);
        content.setBackgroundColor(new Color(250, 252, 255));
        content.setBorderColor(GRIS_BORDER);
        content.setBorderWidth(0.3f);
        t.addCell(content);
        return t;
    }

    // ── Sección IV ─────────────────────────────────────────────────────────
    private void buildSeccionIV(Document doc, List<GrupoTutorado> grupoTutorados,
            Map<Integer, Asistencia> mapaAsistencia,
            long totalAlumnos, long presentes, long ausentes, double pctAsist) throws Exception {

        PdfPTable headerT = new PdfPTable(1);
        headerT.setWidthPercentage(100);
        headerT.setSpacingAfter(0);
        addSeccionHeader(headerT, "IV. REGISTRO DE ASISTENCIA", 1);
        doc.add(headerT);

        PdfPTable statsT = new PdfPTable(4);
        statsT.setWidthPercentage(100);
        statsT.setSpacingAfter(0);
        statsT.addCell(buildStatCell(String.valueOf(totalAlumnos), "TOTAL ALUMNOS", AZUL_VALOR));
        statsT.addCell(buildStatCell(String.valueOf(presentes),    "PRESENTES",     VERDE_PRES));
        statsT.addCell(buildStatCell(String.valueOf(ausentes),     "AUSENTES",      ROJO_AUS));
        Color colorPct = pctAsist >= 80.0 ? VERDE_PRES : ROJO_AUS;
        statsT.addCell(buildStatCell(pctAsist + "%", "% ASISTENCIA", colorPct));
        doc.add(statsT);

        PdfPTable asistT = new PdfPTable(new float[]{5f, 12f, 35f, 18f, 15f, 15f});
        asistT.setWidthPercentage(100);
        asistT.setSpacingAfter(2f);

        for (String h : new String[]{"#", "No. Control", "Nombre del Tutorado",
                "Carrera", "Asistencia", "Recuperada"}) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, F_BLANCO_7));
            hCell.setBackgroundColor(AZUL_THEAD);
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            hCell.setPadding(3f);
            asistT.addCell(hCell);
        }

        if (grupoTutorados.isEmpty()) {
            PdfPCell empty = new PdfPCell(
                    new Phrase("Sin tutorados asignados a este grupo", F_ITALIC_7));
            empty.setColspan(6);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(6f);
            asistT.addCell(empty);
        } else {
            int num = 1;
            for (GrupoTutorado gt : grupoTutorados) {
                Tutorado t2  = gt.getTutorado();
                Asistencia a = mapaAsistencia.get(t2.getId());

                boolean presente   = a != null && a.getPresente()   != null && a.getPresente()   == 1;
                boolean recuperada = a != null && a.getRecuperada() != null && a.getRecuperada() == 1;
                boolean sinReg     = a == null;
                boolean ausente    = a != null && !presente && !recuperada;

                Color fondo = (num % 2 == 0) ? GRIS_FILA : BLANCO;

                String textoAsist; Font fAsist;
                if (presente)       { textoAsist = "✓ Presente";   fAsist = F_VERDE; }
                else if (ausente)   { textoAsist = "✗ Ausente";    fAsist = F_ROJO_AUS; }
                else if (sinReg)    { textoAsist = "Sin registro"; fAsist = F_ITALIC_7; }
                else                { textoAsist = "—";             fAsist = F_NORMAL_7; }

                String textoRec; Font fRec;
                if (recuperada) { textoRec = "↩ Sí"; fRec = F_AZUL_REC; }
                else            { textoRec = "—";     fRec = F_NORMAL_7; }

                String carreraClave = t2.getCarrera() != null ? t2.getCarrera().getClave() : "—";

                asistT.addCell(asistCell(String.valueOf(num), F_NORMAL_7, fondo, Element.ALIGN_CENTER));
                asistT.addCell(asistCell(t2.getNumeroControl(), F_NORMAL_7, fondo, Element.ALIGN_LEFT));
                asistT.addCell(asistCell(t2.getNombre() + " " + t2.getApellido(),
                        F_NORMAL_7, fondo, Element.ALIGN_LEFT));
                asistT.addCell(asistCell(carreraClave, F_NORMAL_7, fondo, Element.ALIGN_CENTER));
                asistT.addCell(asistCell(textoAsist, fAsist, fondo, Element.ALIGN_LEFT));
                asistT.addCell(asistCell(textoRec, fRec, fondo, Element.ALIGN_CENTER));
                num++;
            }
        }
        doc.add(asistT);

        Paragraph nota = new Paragraph(
                "* Los alumnos sin registro no tuvieron asistencia capturada en el sistema para esta sesión.",
                F_ITALIC_7);
        nota.setSpacingBefore(2f);
        nota.setSpacingAfter(5f);
        doc.add(nota);
    }

    // ── Sección V ──────────────────────────────────────────────────────────
    private PdfPTable buildSeccionV(ReporteSesion reporte) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingAfter(10f);
        addSeccionHeader(t, "V. OBSERVACIONES DEL TUTOR", 1);

        boolean tieneObs = reporte.getObservaciones() != null
                && !reporte.getObservaciones().isBlank();
        PdfPCell content = new PdfPCell(new Phrase(
                tieneObs ? reporte.getObservaciones()
                         : "Sin observaciones adicionales para esta sesión.",
                tieneObs ? F_NORMAL_8 : F_ITALIC_7));
        content.setPadding(6f);
        content.setMinimumHeight(30f);
        content.setBorderColor(GRIS_BORDER);
        content.setBorderWidth(0.3f);
        t.addCell(content);
        return t;
    }

    // ── Firmas ─────────────────────────────────────────────────────────────
    private PdfPTable buildFirmas(String nombreTutorMayus, String nombreCarrera) {
        PdfPTable outer = new PdfPTable(2);
        outer.setWidthPercentage(100);
        outer.setSpacingAfter(8f);
        outer.addCell(buildCeldaFirma("ATENTAMENTE", true,  nombreTutorMayus,
                "Tutor/a del Programa Institucional de Tutorías", NEGRO));
        outer.addCell(buildCeldaFirma("Vo.Bo.", false, "COORDINADOR/A DE CARRERA",
                "Coordinador/a de Tutoría · " + nombreCarrera, AZUL_VALOR));
        return outer;
    }

    private PdfPCell buildCeldaFirma(String titulo, boolean conLema,
            String nombre, String cargo, Color cargoColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);

        cell.addElement(new Paragraph(titulo, F_BOLD_8));
        if (conLema) {
            cell.addElement(new Paragraph("Excelencia en Educación Tecnológica®", F_ITALIC_7));
            cell.addElement(new Paragraph("Crear Tecnología es Forjar Libertad", F_ITALIC_7));
        } else {
            cell.addElement(new Paragraph(" ", F_ITALIC_7));
            cell.addElement(new Paragraph(" ", F_ITALIC_7));
        }

        Paragraph sello = new Paragraph(" ");
        sello.setSpacingBefore(50f);
        cell.addElement(sello);

        Paragraph linea = new Paragraph(
                new Chunk(new LineSeparator(0.5f, 100f, NEGRO, Element.ALIGN_CENTER, 0f)));
        linea.setSpacingAfter(2f);
        cell.addElement(linea);

        Paragraph pNombre = new Paragraph(nombre, F_BOLD_8);
        pNombre.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pNombre);

        Paragraph pCargo = new Paragraph(cargo,
                new Font(Font.HELVETICA, 7, Font.NORMAL, cargoColor));
        pCargo.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pCargo);

        return cell;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void addSeccionHeader(PdfPTable t, String texto, int colspan) {
        PdfPCell h = new PdfPCell(new Phrase(texto, F_BLANCO_8));
        h.setColspan(colspan);
        h.setBackgroundColor(AZUL_HEADER);
        h.setHorizontalAlignment(Element.ALIGN_CENTER);
        h.setPadding(5f);
        t.addCell(h);
    }

    private PdfPCell celdaLabel(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_BOLD_7));
        c.setBackgroundColor(GRIS_LABEL);
        c.setPadding(4f);
        c.setBorderColor(GRIS_BORDER);
        c.setBorderWidth(0.3f);
        return c;
    }

    private PdfPCell celdaValor(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "—", F_VALOR));
        c.setPadding(4f);
        c.setBorderColor(GRIS_BORDER);
        c.setBorderWidth(0.3f);
        return c;
    }

    private PdfPCell crearCeldaBadge(String estatus) {
        Color bg, fg;
        switch (estatus != null ? estatus.toUpperCase() : "") {
            case "REALIZADA", "REVISADO", "APROBADO" -> { bg = VERDE_BG;    fg = VERDE_FG; }
            case "CANCELADA", "RECHAZADA"             -> { bg = ROJO_BG;    fg = ROJO_FG;  }
            default                                   -> { bg = AMARILLO_BG; fg = AMARILLO_FG; }
        }
        Font fBadge = new Font(Font.HELVETICA, 7, Font.BOLD, fg);
        PdfPCell cell = new PdfPCell(new Phrase(estatus != null ? estatus : "PENDIENTE", fBadge));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(fg);
        cell.setBorderWidth(0.5f);
        cell.setPadding(3f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell buildStatCell(String numero, String label, Color color) {
        Font fNum = new Font(Font.HELVETICA, 14, Font.BOLD, color);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(248, 250, 252));
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(new Color(200, 200, 200));
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setMinimumHeight(32f);
        Paragraph numP = new Paragraph(numero, fNum);
        numP.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(numP);
        Paragraph lblP = new Paragraph(label, new Font(Font.HELVETICA, 7, Font.NORMAL, GRIS_TEXTO));
        lblP.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(lblP);
        return cell;
    }

    private PdfPCell asistCell(String texto, Font font, Color bg, int hAlign) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(3f);
        cell.setMinimumHeight(14f);
        cell.setBorderWidth(0.3f);
        cell.setBorderColor(new Color(180, 180, 180));
        cell.setHorizontalAlignment(hAlign);
        return cell;
    }
}
