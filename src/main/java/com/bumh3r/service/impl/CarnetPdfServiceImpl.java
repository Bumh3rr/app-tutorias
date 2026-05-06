package com.bumh3r.service.impl;

import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.IAsistenciaRepository;
import com.bumh3r.repository.IGrupoTutoradoRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.CarnetPdfService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Primary
@Service
public class CarnetPdfServiceImpl implements CarnetPdfService {

    private static final Color AZUL_TECNM     = new Color(0, 51, 102);
    private static final Color AMARILLO_PAT   = new Color(245, 200, 0);
    private static final Color GRIS_HEADER    = new Color(232, 232, 232);
    private static final Color VERDE_PRESENTE = new Color(46, 125, 50);
    private static final Color ROJO_AUSENTE   = new Color(198, 40, 40);
    private static final Color AZUL_RECUPERADA = new Color(21, 101, 192);
    private static final Color FONDO_VERDE    = new Color(232, 245, 233);
    private static final Color FONDO_ROJO     = new Color(255, 235, 238);
    private static final Color FONDO_AZUL     = new Color(227, 242, 253);
    private static final Color GRIS_TEXTO     = new Color(102, 102, 102);
    private static final Color BLANCO         = Color.WHITE;
    private static final Color NEGRO          = Color.BLACK;

    @Autowired private ITutoradoRepository tutoradoRepository;
    @Autowired private IGrupoTutoradoRepository grupoTutoradoRepository;
    @Autowired private ISesionRepository sesionRepository;
    @Autowired private IAsistenciaRepository asistenciaRepository;

    @Override
    public byte[] generarCarnetTutorado(Integer idTutorado) throws Exception {
        Tutorado tutorado = tutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));

        List<GrupoTutorado> gruposTutorado = grupoTutoradoRepository.findByActivoAndTutorado(1, tutorado);
        Grupo grupo = gruposTutorado.isEmpty() ? null : gruposTutorado.get(0).getGrupo();

        List<Sesion> sesiones = grupo != null
                ? sesionRepository.findByActivoAndGrupo(1, grupo).stream()
                        .sorted(Comparator.comparing(Sesion::getSemana))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        List<Asistencia> asistencias = asistenciaRepository.findByTutorado(tutorado);
        Map<Integer, Asistencia> asistenciaMap = new HashMap<>();
        for (Asistencia a : asistencias) {
            asistenciaMap.putIfAbsent(a.getSesion().getId(), a);
        }

        long presentes   = asistencias.stream().filter(a -> Integer.valueOf(1).equals(a.getPresente())).count();
        long recuperadas = asistencias.stream().filter(a -> Integer.valueOf(1).equals(a.getRecuperada())).count();
        long acreditadas = presentes + recuperadas;
        double porcentaje = (acreditadas / 10.0) * 100.0;

        String carreraClv = (tutorado.getCarrera() != null) ? tutorado.getCarrera().getClave().toLowerCase() : "xxx";
        int anioSem = (grupo != null && grupo.getSemestre() != null) ? grupo.getSemestre().getAnio() : 2026;
        int grupoId = (grupo != null) ? grupo.getId() : 0;
        String codigo = "dda-" + carreraClv + "-ej" + (anioSem - 2000) + "-" + grupoId
                + " E-" + tutorado.getNumeroControl();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        PdfPTable mainTable = new PdfPTable(3);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{25f, 45f, 30f});
        mainTable.setExtendLastRow(true);

        mainTable.addCell(buildLeftColumn(tutorado, codigo));
        mainTable.addCell(buildCenterColumn(tutorado, grupo, sesiones, asistenciaMap));
        mainTable.addCell(buildRightColumn(sesiones, asistenciaMap, acreditadas, porcentaje));

        doc.add(mainTable);
        doc.close();

        return baos.toByteArray();
    }

    // ── Left Column ───────────────────────────────────────────────────────────

    private PdfPCell buildLeftColumn(Tutorado tutorado, String codigo) {
        PdfPTable nested = new PdfPTable(1);
        nested.setWidthPercentage(100);
        nested.setExtendLastRow(true);

        // Logo — TecNM image
        PdfPCell logoCel = new PdfPCell();
        logoCel.setBackgroundColor(AZUL_TECNM);
        logoCel.setPaddingTop(8);
        logoCel.setPaddingBottom(8);
        logoCel.setPaddingLeft(8);
        logoCel.setPaddingRight(8);
        logoCel.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCel.setBorder(Rectangle.NO_BORDER);
        try {
            byte[] imgBytes;
            try (java.io.InputStream is = CarnetPdfServiceImpl.class.getResourceAsStream(
                    "/static/images/tecnm/logoTecnm.png")) {
                imgBytes = (is != null) ? is.readAllBytes() : null;
            }
            if (imgBytes != null) {
                Image logo = Image.getInstance(imgBytes);
                logo.scaleToFit(160f, 70f);
                logo.setAlignment(Element.ALIGN_CENTER);
                logoCel.addElement(logo);
            } else {
                throw new Exception("logo not found");
            }
        } catch (Exception e) {
            Paragraph fallback = new Paragraph(11f);
            fallback.setAlignment(Element.ALIGN_CENTER);
            fallback.add(new Chunk("INSTITUTO TECNOLÓGICO\nDE CHILPANCINGO\n1984", f(7f, Font.BOLD, BLANCO)));
            logoCel.addElement(fallback);
        }
        nested.addCell(logoCel);

        // "INSTRUCCIONES" header
        PdfPCell hdrCel = new PdfPCell();
        hdrCel.setBackgroundColor(AMARILLO_PAT);
        hdrCel.setPadding(4);
        hdrCel.setBorder(Rectangle.BOTTOM);
        hdrCel.setBorderWidthBottom(1f);
        hdrCel.setBorderColorBottom(NEGRO);
        Paragraph hdrP = new Paragraph("INSTRUCCIONES", f(11f, Font.BOLD, NEGRO));
        hdrP.setAlignment(Element.ALIGN_CENTER);
        hdrCel.addElement(hdrP);
        nested.addCell(hdrCel);

        // Instructions body
        PdfPCell instrCel = new PdfPCell();
        instrCel.setPadding(6);
        instrCel.setBorder(Rectangle.NO_BORDER);
        instrCel.addElement(buildInstructionsText());
        nested.addCell(instrCel);

        // Signature area — last row, extends to fill remaining column height
        PdfPCell sigCel = new PdfPCell();
        sigCel.setBorder(Rectangle.TOP);
        sigCel.setBorderWidthTop(1f);
        sigCel.setBorderColorTop(NEGRO);
        sigCel.setPadding(5);
        sigCel.setVerticalAlignment(Element.ALIGN_BOTTOM);
        Paragraph sigP = new Paragraph(10f);
        sigP.setAlignment(Element.ALIGN_CENTER);
        sigP.add(new Chunk("Nombre y firma de Conformidad\ndel/la Tutorado/a\n\n", f(7f, Font.NORMAL, GRIS_TEXTO)));
        sigP.add(new Chunk(codigo, f(6f, Font.NORMAL, GRIS_TEXTO)));
        sigCel.addElement(sigP);
        nested.addCell(sigCel);

        PdfPCell outer = new PdfPCell(nested);
        outer.setPadding(0);
        outer.setBorder(Rectangle.BOX);
        return outer;
    }

    private Paragraph buildInstructionsText() {
        Font fR = f(7f, Font.NORMAL, NEGRO);
        Font fB = f(7f, Font.BOLD, NEGRO);
        Paragraph p = new Paragraph(10f);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Como es de tu conocimiento las ", fR));
        p.add(new Chunk("TUTORÍAS", fB));
        p.add(new Chunk(" son una actividad complementaria que te permite obtener ", fR));
        p.add(new Chunk("2 CRÉDITOS", fB));
        p.add(new Chunk(" (1 por semestre).\n\n", fR));
        p.add(new Chunk("Este ", fR));
        p.add(new Chunk("CARNET", fB));
        p.add(new Chunk(" es para tu uso exclusivamente, en él llevarás tu registro de asistencias" +
                " a las 10 actividades planeadas para ti, en el marco del Programa Institucional" +
                " de Tutorías, es de suma importancia que cubras un ", fR));
        p.add(new Chunk("80% (8 firmas)", fB));
        p.add(new Chunk(" de asistencias en la tabla \"", fR));
        p.add(new Chunk("ACTIVIDAD TUTORIAL", fB));
        p.add(new Chunk("\" y seas acreedor a una constancia.\n\n", fR));
        p.add(new Chunk("Finalmente, es necesario entregar tu carnet en la última sesión a tu" +
                " tutor/a, quedándote con una copia donde tu Tutor/a firmará de recibido, misma" +
                " que deberás conservar para futuras aclaraciones.\n\n", fR));
        p.add(new Chunk("De no cumplir con lo estipulado no obtendrás tus créditos.", fR));
        return p;
    }

    // ── Center Column ─────────────────────────────────────────────────────────

    private PdfPCell buildCenterColumn(Tutorado tutorado, Grupo grupo,
                                       List<Sesion> sesiones, Map<Integer, Asistencia> asistenciaMap) {
        PdfPTable nested = new PdfPTable(1);
        nested.setWidthPercentage(100);
        nested.setExtendLastRow(true);

        // Header
        PdfPCell hdrCel = new PdfPCell();
        hdrCel.setBackgroundColor(AMARILLO_PAT);
        hdrCel.setPadding(5);
        hdrCel.setBorder(Rectangle.BOTTOM);
        hdrCel.setBorderWidthBottom(1f);
        hdrCel.setBorderColorBottom(NEGRO);
        Paragraph hdrP = new Paragraph("REGISTRO DE ASISTENCIAS", f(13f, Font.BOLD, NEGRO));
        hdrP.setAlignment(Element.ALIGN_CENTER);
        hdrCel.addElement(hdrP);
        nested.addCell(hdrCel);

        // Data strip
        nested.addCell(buildTutoradoDataStrip(tutorado, grupo));

        // Activity table
        PdfPCell actCel = new PdfPCell(buildActivityTable(sesiones, asistenciaMap));
        actCel.setPadding(0);
        actCel.setBorder(Rectangle.NO_BORDER);
        nested.addCell(actCel);

        // Footer — last row, extends to fill remaining column height
        PdfPCell footerCel = new PdfPCell();
        footerCel.setBackgroundColor(new Color(240, 240, 240));
        footerCel.setPadding(3);
        footerCel.setBorder(Rectangle.TOP);
        footerCel.setBorderWidthTop(1f);
        footerCel.setBorderColorTop(NEGRO);
        footerCel.setVerticalAlignment(Element.ALIGN_BOTTOM);
        Paragraph footerP = new Paragraph("INSTITUTO TECNOLÓGICO DE CHILPANCINGO", f(7f, Font.NORMAL, NEGRO));
        footerP.setAlignment(Element.ALIGN_CENTER);
        footerCel.addElement(footerP);
        nested.addCell(footerCel);

        PdfPCell outer = new PdfPCell(nested);
        outer.setPadding(0);
        outer.setBorder(Rectangle.BOX);
        return outer;
    }

    private PdfPCell buildTutoradoDataStrip(Tutorado tutorado, Grupo grupo) {
        Font fLabel = f(6f, Font.NORMAL, new Color(180, 180, 180));
        Font fValue = f(8f, Font.BOLD, BLANCO);

        PdfPTable grid = new PdfPTable(4);
        try { grid.setWidths(new float[]{25f, 25f, 25f, 25f}); } catch (Exception ignored) {}
        grid.setWidthPercentage(100);

        addDataCell(grid, "TUTORADO", fLabel, true);
        addDataCell(grid, "NO. CONTROL", fLabel, true);
        addDataCell(grid, "TUTOR", fLabel, true);
        addDataCell(grid, "CARRERA / SEMESTRE", fLabel, true);

        String nombreTutorado = tutorado.getNombre() + " " + tutorado.getApellido();
        String numControl = tutorado.getNumeroControl();
        String tutor = (grupo != null && grupo.getTutor() != null)
                ? grupo.getTutor().getNombre() + " " + grupo.getTutor().getApellido()
                : "—";
        String carreraSem = "";
        if (tutorado.getCarrera() != null) carreraSem += tutorado.getCarrera().getClave();
        if (grupo != null && grupo.getSemestre() != null)
            carreraSem += " " + grupo.getSemestre().getPeriodo() + " " + grupo.getSemestre().getAnio();

        addDataCell(grid, nombreTutorado, fValue, false);
        addDataCell(grid, numControl, fValue, false);
        addDataCell(grid, tutor, fValue, false);
        addDataCell(grid, carreraSem.trim(), fValue, false);

        PdfPCell outer = new PdfPCell(grid);
        outer.setBackgroundColor(AZUL_TECNM);
        outer.setPadding(0);
        outer.setBorder(Rectangle.NO_BORDER);
        return outer;
    }

    private void addDataCell(PdfPTable t, String text, Font font, boolean isLabel) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(AZUL_TECNM);
        c.setPaddingLeft(4);
        c.setPaddingRight(4);
        c.setPaddingTop(isLabel ? 4 : 1);
        c.setPaddingBottom(isLabel ? 1 : 5);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);
    }

    private PdfPTable buildActivityTable(List<Sesion> sesiones, Map<Integer, Asistencia> asistenciaMap) {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{60f, 40f}); } catch (Exception ignored) {}

        // Header row
        PdfPCell h1 = new PdfPCell();
        h1.setBackgroundColor(GRIS_HEADER);
        h1.setPadding(4);
        h1.setBorder(Rectangle.BOTTOM);
        h1.setBorderWidthBottom(1f);
        h1.setBorderColorBottom(NEGRO);
        h1.addElement(new Paragraph("ACTIVIDAD TUTORIAL", f(8f, Font.BOLD, NEGRO)));
        t.addCell(h1);

        PdfPCell h2 = new PdfPCell();
        h2.setBackgroundColor(GRIS_HEADER);
        h2.setPadding(4);
        h2.setBorder(Rectangle.BOTTOM | Rectangle.LEFT);
        h2.setBorderWidthBottom(1f);
        h2.setBorderColorBottom(NEGRO);
        h2.setBorderWidthLeft(1f);
        h2.setBorderColorLeft(NEGRO);
        h2.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph h2P = new Paragraph(10f);
        h2P.setAlignment(Element.ALIGN_CENTER);
        h2P.add(new Chunk("FIRMA DE ASISTENCIA\n", f(8f, Font.BOLD, NEGRO)));
        h2P.add(new Chunk("DE TUTOR/A O COORDINACIÓN", f(7.5f, Font.BOLD, NEGRO)));
        h2.addElement(h2P);
        t.addCell(h2);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM", new Locale("es", "MX"));
        Font fActN = f(7.5f, Font.BOLD, NEGRO);
        Font fActG = f(7f, Font.NORMAL, GRIS_TEXTO);
        Font fPres = f(7f, Font.BOLD, VERDE_PRESENTE);
        Font fAus  = f(7f, Font.BOLD, ROJO_AUSENTE);
        Font fRec  = f(7f, Font.BOLD, AZUL_RECUPERADA);
        Font fFirmaV = f(14f, Font.BOLD, VERDE_PRESENTE);
        Font fFirmaA = f(14f, Font.BOLD, AZUL_RECUPERADA);

        if (sesiones.isEmpty()) {
            for (int i = 1; i <= 10; i++) addEmptyActivityRow(t, i, fActN);
        } else {
            int count = 0;
            for (Sesion s : sesiones) {
                if (count >= 10) break;
                count++;
                Asistencia asist = asistenciaMap.get(s.getId());

                String actNombre = s.getSemana() + ". " +
                        (s.getActividad() != null ? s.getActividad().getNombre().toUpperCase() : "(SIN ACTIVIDAD)");
                String fechaStr = "";
                if (s.getFechaImparticion() != null) {
                    LocalDate ld = s.getFechaImparticion().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    fechaStr = ld.format(fmt).toUpperCase();
                }

                PdfPCell c1 = new PdfPCell();
                c1.setPaddingLeft(4);
                c1.setPaddingRight(4);
                c1.setPaddingTop(3);
                c1.setPaddingBottom(3);
                c1.setBorder(Rectangle.BOTTOM);
                c1.setBorderWidthBottom(0.5f);
                c1.setBorderColorBottom(GRIS_HEADER);
                c1.setMinimumHeight(22f);

                Paragraph p = new Paragraph(10f);
                if (asist != null && Integer.valueOf(1).equals(asist.getPresente())) {
                    c1.setBackgroundColor(FONDO_VERDE);
                    p.add(new Chunk(actNombre + "\n", fActN));
                    p.add(new Chunk(fechaStr + "\n", fActG));
                    p.add(new Chunk("PRESENTE", fPres));
                } else if (asist != null && Integer.valueOf(1).equals(asist.getRecuperada())) {
                    c1.setBackgroundColor(FONDO_AZUL);
                    p.add(new Chunk(actNombre + "\n", fActN));
                    p.add(new Chunk(fechaStr + "\n", fActG));
                    p.add(new Chunk("RECUPERADA", fRec));
                } else if (asist != null) {
                    c1.setBackgroundColor(FONDO_ROJO);
                    p.add(new Chunk(actNombre + "\n", fActN));
                    p.add(new Chunk(fechaStr + "\n", fActG));
                    p.add(new Chunk("AUSENTE", fAus));
                } else {
                    p.add(new Chunk(actNombre + "\n", fActN));
                    p.add(new Chunk(fechaStr, fActG));
                }
                c1.addElement(p);
                t.addCell(c1);

                PdfPCell c2 = new PdfPCell();
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c2.setPadding(2);
                c2.setBorder(Rectangle.BOTTOM | Rectangle.LEFT);
                c2.setBorderWidthBottom(0.5f);
                c2.setBorderColorBottom(GRIS_HEADER);
                c2.setBorderWidthLeft(0.5f);
                c2.setBorderColorLeft(GRIS_HEADER);
                c2.setMinimumHeight(22f);
                if (asist != null && Integer.valueOf(1).equals(asist.getPresente())) {
                    Paragraph sp = new Paragraph("OK", fFirmaV);
                    sp.setAlignment(Element.ALIGN_CENTER);
                    c2.addElement(sp);
                } else if (asist != null && Integer.valueOf(1).equals(asist.getRecuperada())) {
                    Paragraph sp = new Paragraph("REC", fFirmaA);
                    sp.setAlignment(Element.ALIGN_CENTER);
                    c2.addElement(sp);
                }
                t.addCell(c2);
            }
            for (int i = count + 1; i <= 10; i++) addEmptyActivityRow(t, i, fActN);
        }
        return t;
    }

    private void addEmptyActivityRow(PdfPTable t, int num, Font fActN) {
        PdfPCell c1 = new PdfPCell();
        c1.setPaddingLeft(4);
        c1.setPaddingRight(4);
        c1.setPaddingTop(3);
        c1.setPaddingBottom(3);
        c1.setBorder(Rectangle.BOTTOM);
        c1.setBorderWidthBottom(0.5f);
        c1.setBorderColorBottom(GRIS_HEADER);
        c1.setMinimumHeight(22f);
        c1.addElement(new Paragraph(num + ".", fActN));
        t.addCell(c1);

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(Rectangle.BOTTOM | Rectangle.LEFT);
        c2.setBorderWidthBottom(0.5f);
        c2.setBorderColorBottom(GRIS_HEADER);
        c2.setBorderWidthLeft(0.5f);
        c2.setBorderColorLeft(GRIS_HEADER);
        c2.setMinimumHeight(22f);
        t.addCell(c2);
    }

    // ── Right Column ──────────────────────────────────────────────────────────

    private PdfPCell buildRightColumn(List<Sesion> sesiones, Map<Integer, Asistencia> asistenciaMap,
                                      long acreditadas, double porcentaje) {
        PdfPTable nested = new PdfPTable(1);
        nested.setWidthPercentage(100);
        nested.setExtendLastRow(true);

        Font fR7  = f(7f, Font.NORMAL, NEGRO);
        Font fB7  = f(7f, Font.BOLD, NEGRO);
        Font fB75 = f(7.5f, Font.BOLD, AZUL_TECNM);
        Font fRed7 = f(7f, Font.BOLD, ROJO_AUSENTE);
        Font fBlue65 = f(6.5f, Font.NORMAL, new Color(0, 102, 204));

        // Header
        PdfPCell hdrCel = new PdfPCell();
        hdrCel.setBackgroundColor(GRIS_HEADER);
        hdrCel.setPadding(4);
        hdrCel.setBorder(Rectangle.BOTTOM);
        hdrCel.setBorderWidthBottom(1f);
        hdrCel.setBorderColorBottom(NEGRO);
        Paragraph hdrP = new Paragraph("ASESORÍAS / TALLERES", f(9f, Font.BOLD, NEGRO));
        hdrP.setAlignment(Element.ALIGN_CENTER);
        hdrCel.addElement(hdrP);
        nested.addCell(hdrCel);

        // Course block
        PdfPCell courseCel = new PdfPCell();
        courseCel.setPadding(5);
        courseCel.setBorder(Rectangle.BOTTOM);
        courseCel.setBorderWidthBottom(0.5f);
        courseCel.setBorderColorBottom(GRIS_HEADER);
        Paragraph courseP = new Paragraph(10f);
        courseP.add(new Chunk("Curso \"Sanamente Libremente: Jóvenes por la paz y contra las adicciones\"\n", fB75));
        courseP.add(new Chunk("Es de carácter obligatorio para las y los tutorados.\n", fRed7));
        courseP.add(new Chunk("https://saberes.gob.mx/catalog/course/941902", fBlue65));
        courseCel.addElement(courseP);
        nested.addCell(courseCel);

        // Note block
        PdfPCell noteCel = new PdfPCell();
        noteCel.setBackgroundColor(new Color(255, 254, 240));
        noteCel.setPadding(5);
        noteCel.setBorder(Rectangle.BOTTOM);
        noteCel.setBorderWidthBottom(0.5f);
        noteCel.setBorderColorBottom(GRIS_HEADER);
        Paragraph noteP = new Paragraph(
                "Si ya lo aprobaste, trae la constancia al Depto. Desarrollo Académico. "
                        + "Es indispensable traer tu carnet para el registro de la firma.", fR7);
        noteP.setAlignment(Element.ALIGN_JUSTIFIED);
        noteCel.addElement(noteP);
        nested.addCell(noteCel);

        // Recuperaciones header
        PdfPCell recHdrCel = new PdfPCell();
        recHdrCel.setBackgroundColor(new Color(245, 245, 245));
        recHdrCel.setPadding(4);
        recHdrCel.setBorder(Rectangle.BOTTOM);
        recHdrCel.setBorderWidthBottom(0.5f);
        recHdrCel.setBorderColorBottom(GRIS_HEADER);
        Paragraph recHdrP = new Paragraph("RECUPERACIONES DE INASISTENCIA", fB7);
        recHdrP.setAlignment(Element.ALIGN_CENTER);
        recHdrCel.addElement(recHdrP);
        nested.addCell(recHdrCel);

        // Recuperaciones rows (5 total)
        List<Asistencia> recList = sesiones.stream()
                .map(s -> asistenciaMap.get(s.getId()))
                .filter(a -> a != null && Integer.valueOf(1).equals(a.getRecuperada()))
                .collect(Collectors.toList());

        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < 5; i++) {
            String fechaVal = "";
            if (i < recList.size() && recList.get(i).getSesion() != null
                    && recList.get(i).getSesion().getFechaImparticion() != null) {
                LocalDate ld = recList.get(i).getSesion().getFechaImparticion()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                fechaVal = ld.format(fmtDate);
            }

            PdfPTable recRow = new PdfPTable(4);
            try { recRow.setWidths(new float[]{18f, 24f, 22f, 36f}); } catch (Exception ignored) {}
            recRow.setWidthPercentage(100);
            addRecCell(recRow, "FECHA:", fB7);
            addRecCell(recRow, fechaVal, fR7);
            addRecCell(recRow, "ASESOR/A:", fB7);
            addRecCell(recRow, "", fR7);

            PdfPCell rowWrap = new PdfPCell(recRow);
            rowWrap.setPadding(0);
            rowWrap.setBorder(Rectangle.BOTTOM);
            rowWrap.setBorderWidthBottom(0.5f);
            rowWrap.setBorderColorBottom(GRIS_HEADER);
            nested.addCell(rowWrap);
        }

        // Summary
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);

        Font fLabel6 = f(6f, Font.NORMAL, GRIS_TEXTO);
        Font fBig18  = f(18f, Font.BOLD, AZUL_TECNM);
        Font fBigOk  = f(18f, Font.BOLD, VERDE_PRESENTE);
        Font fBigFail = f(18f, Font.BOLD, ROJO_AUSENTE);

        PdfPCell asistCel = new PdfPCell();
        asistCel.setPadding(6);
        asistCel.setBorder(Rectangle.TOP | Rectangle.RIGHT);
        asistCel.setBorderWidthTop(1f);
        asistCel.setBorderColorTop(NEGRO);
        asistCel.setBorderWidthRight(0.5f);
        asistCel.setBorderColorRight(GRIS_HEADER);
        asistCel.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph asistP = new Paragraph();
        asistP.setAlignment(Element.ALIGN_CENTER);
        asistP.add(new Chunk("Asistencias\n", fLabel6));
        asistP.add(new Chunk(acreditadas + "/10", fBig18));
        asistCel.addElement(asistP);
        summaryTable.addCell(asistCel);

        Font fBigPct = (porcentaje >= 80.0) ? fBigOk : fBigFail;
        String pctStr = (porcentaje == Math.floor(porcentaje))
                ? String.valueOf((long) porcentaje) + "%"
                : String.format("%.1f%%", porcentaje);
        PdfPCell pctCel = new PdfPCell();
        pctCel.setPadding(6);
        pctCel.setBorder(Rectangle.TOP);
        pctCel.setBorderWidthTop(1f);
        pctCel.setBorderColorTop(NEGRO);
        pctCel.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph pctP = new Paragraph();
        pctP.setAlignment(Element.ALIGN_CENTER);
        pctP.add(new Chunk("Porcentaje\n", fLabel6));
        pctP.add(new Chunk(pctStr, fBigPct));
        pctCel.addElement(pctP);
        summaryTable.addCell(pctCel);

        PdfPCell minCel = new PdfPCell();
        minCel.setColspan(2);
        minCel.setPadding(4);
        minCel.setBorder(Rectangle.NO_BORDER);
        Paragraph minP = new Paragraph("Mínimo requerido: 80% (8 firmas)", f(7f, Font.NORMAL, GRIS_TEXTO));
        minP.setAlignment(Element.ALIGN_CENTER);
        minCel.addElement(minP);
        summaryTable.addCell(minCel);

        PdfPCell summaryCel = new PdfPCell(summaryTable);
        summaryCel.setPadding(0);
        summaryCel.setBorder(Rectangle.NO_BORDER);
        summaryCel.setVerticalAlignment(Element.ALIGN_BOTTOM);
        nested.addCell(summaryCel);

        PdfPCell outer = new PdfPCell(nested);
        outer.setPadding(0);
        outer.setBorder(Rectangle.BOX);
        return outer;
    }

    private void addRecCell(PdfPTable t, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPaddingTop(3);
        c.setPaddingBottom(5);
        c.setPaddingLeft(3);
        c.setPaddingRight(3);
        c.setMinimumHeight(16f);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);
    }

    // ── Font helper ───────────────────────────────────────────────────────────

    private Font f(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }
}
