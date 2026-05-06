package com.bumh3r.service.impl;

import com.bumh3r.entity.*;
import com.bumh3r.repository.IDeteccionNecesidadesRepository;
import com.bumh3r.service.DeteccionPdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

@Primary
@Service
public class DeteccionPdfServiceImpl implements DeteccionPdfService {

    // Colors
    private static final Color AZUL_TECNM  = new Color(0, 51, 102);
    private static final Color ROJO_SEP    = new Color(180, 0, 0);
    private static final Color GRIS_HEADER = new Color(224, 224, 224);
    private static final Color GRIS_CLARO  = new Color(248, 248, 248);
    private static final Color AZUL_NOMBRE = new Color(0, 70, 140);
    private static final Color NEGRO       = Color.BLACK;
    private static final Color BLANCO      = Color.WHITE;
    private static final Color GRIS_TEXT   = new Color(80, 80, 80);

    @Autowired
    private IDeteccionNecesidadesRepository deteccionRepository;

    private Font f(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    @Override
    public byte[] generarPdfDeteccion(Integer idDeteccion) throws Exception {
        DeteccionNecesidades d = deteccionRepository.findById(idDeteccion)
                .orElseThrow(() -> new NoSuchElementException("Detección no encontrada"));

        Tutorado t = d.getTutorado();
        Sesion sesion = d.getSesion();
        Grupo grupo = sesion != null ? sesion.getGrupo() : null;
        Tutor tutor = grupo != null ? grupo.getTutor() : null;
        Semestre semestre = grupo != null ? grupo.getSemestre() : null;
        Carrera carrera = t != null ? t.getCarrera() : null;

        String nombreTutorado = t != null ? t.getNombre() + " " + t.getApellido() : "";
        String control        = t != null ? safeStr(t.getNumeroControl()) : "";
        String sexo           = t != null ? safeStr(t.getSexo()) : "";
        String nombreTutor    = tutor != null ? tutor.getNombre() + " " + tutor.getApellido() : "No asignado";
        String periodo        = semestre != null ? semestre.getPeriodo().toUpperCase() + " " + semestre.getAnio() : "";
        String fecha          = d.getFechaRegistro() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(d.getFechaRegistro()) : "";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 36f, 36f, 28f, 36f);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        HeaderFooterEvent hf = new HeaderFooterEvent(periodo);
        writer.setPageEvent(hf);
        doc.open();

        // ── Encabezado institucional ──────────────────────────────────────────
        doc.add(buildEncabezadoInstitucional(periodo));
        doc.add(buildTitulosInstitucionales(periodo));
        doc.add(spacer(4f));

        // ── Título "Detección de Necesidades" ────────────────────────────────
        Paragraph titulo = new Paragraph("Detección de Necesidades", f(14f, Font.BOLD, NEGRO));
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(10f);
        doc.add(titulo);

        // ── Tabla DATOS DEL TUTORADO/TUTORADA ────────────────────────────────
        doc.add(buildTablaAlumno(nombreTutorado, control, sexo, nombreTutor, fecha, carrera));
        doc.add(spacer(8f));

        // Instrucción
        Paragraph instruccion = new Paragraph("Contesta las siguientes preguntas:", f(9f, Font.BOLD, NEGRO));
        instruccion.setSpacingAfter(6f);
        doc.add(instruccion);

        // ── Preguntas P1 y P2 ────────────────────────────────────────────────
        doc.add(buildTablaPreguntas1y2(d));

        // ── Nueva página ─────────────────────────────────────────────────────
        doc.newPage();

        // ── Preguntas P3 a P7 ────────────────────────────────────────────────
        doc.add(buildTablaPreguntas3a7(d));

        doc.close();
        return baos.toByteArray();
    }

    // ── Encabezado institucional (logos + textos centrados) ──────────────────

    private PdfPTable buildEncabezadoInstitucional(String periodo) throws Exception {
        // Outer table: 3 columns
        PdfPTable outer = new PdfPTable(new float[]{18f, 64f, 18f});
        outer.setWidthPercentage(100f);
        outer.setSpacingAfter(4f);

        // Left: SEP + TecNM logo
        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(2f);
        left.setVerticalAlignment(Element.ALIGN_TOP);
        try (InputStream is = getClass().getResourceAsStream("/static/images/tecnm/sep_logo.png")) {
            if (is != null) {
                Image img = Image.getInstance(is.readAllBytes());
                img.scaleToFit(70f, 35f);
                left.addElement(img);
            }
        } catch (Exception ignored) {}
        outer.addCell(left);

        // Center: vacio
        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);
        right.setVerticalAlignment(Element.ALIGN_TOP);
        right.setPadding(2f);
        outer.addCell(right);

        // Right: TecNM logo only
        PdfPCell center = new PdfPCell();
        center.setBorder(Rectangle.NO_BORDER);
        center.setHorizontalAlignment(Element.ALIGN_RIGHT);
        center.setVerticalAlignment(Element.ALIGN_MIDDLE);
        center.setPadding(2f);

        try (InputStream is = getClass().getResourceAsStream("/static/images/tecnm/tecnologico_nacional.png")) {
            if (is != null) {
                Image img = Image.getInstance(is.readAllBytes());
                img.scaleToFit(90f, 50f);
                img.setAlignment(Image.ALIGN_CENTER);
                center.addElement(img);
            }
        } catch (Exception ignored) {}
        outer.addCell(center);
        return outer;
    }

    private Paragraph buildTitulosInstitucionales(String periodo) {
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.add(new Chunk("INSTITUTO TECNOLÓGICO DE CHILPANCINGO\n", f(11f, Font.BOLD, NEGRO)));
        p.add(new Chunk("SUBDIRECCIÓN ACADÉMICA\n", f(9f, Font.BOLD, NEGRO)));
        p.add(new Chunk("DEPARTAMENTO DE DESARROLLO ACADÉMICO\n", f(9f, Font.BOLD, NEGRO)));
        if (periodo != null && !periodo.isBlank()) {
            p.add(new Chunk("PERIODO " + periodo, f(9f, Font.NORMAL, AZUL_NOMBRE)));
        }
        p.setSpacingAfter(2f);
        return p;
    }

    // ── Tabla datos del alumno ─────────────────────────────────────────────────

    private PdfPTable buildTablaAlumno(String nombre, String control, String sexo,
                                       String tutor, String fecha, Carrera carrera) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100f);
        t.setSpacingAfter(0f);

        // Header row
        PdfPCell header = new PdfPCell(new Phrase("DATOS DEL TUTORADO/TUTORADA", f(9f, Font.BOLD, NEGRO)));
        header.setBackgroundColor(GRIS_HEADER);
        header.setPadding(5f);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(header);

        // 1. Nombre
        t.addCell(buildDataRow2("1. NOMBRE COMPLETO", nombre, true));

        // 2. Carrera — checkbox grid
        t.addCell(buildCarreraRow(carrera));

        // 3. Control + 4. Sexo
        t.addCell(buildControlSexoRow(control, sexo));

        // 5. Fecha + 6. Tutor
        t.addCell(buildTutorFechaRow(tutor, fecha));

        return t;
    }

    private PdfPCell buildDataRow2(String label, String value, boolean valueBlue) {
        PdfPTable inner = new PdfPTable(new float[]{2f, 5f});
        inner.setWidthPercentage(100f);
        PdfPCell lbl = cell(label, f(8f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT);
        PdfPCell val = cell(value, f(9f, Font.BOLD, valueBlue ? AZUL_NOMBRE : NEGRO), BLANCO, 5f, Element.ALIGN_LEFT);
        inner.addCell(lbl);
        inner.addCell(val);

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setPadding(0f);
        wrapper.setBorder(Rectangle.BOX);
        return wrapper;
    }

    private PdfPCell buildCarreraRow(Carrera carrera) {
        String[] nombres = {
            "INGENIERÍA EN SISTEMAS\nCOMPUTACIONALES (ISIC)",
            "INGENIERÍA\nINFORMÁTICA (IINF)",
            "INGENIERÍA\nCIVIL (ICIV)",
            "CONTADOR\nPÚBLICO (COPU)",
            "INGENIERÍA EN GESTIÓN\nEMPRESARIAL (IGEM)"
        };
        String[] claves = {"ISIC", "IINF", "ICIV", "COPU", "IGEM"};

        // Determine which carrera matches
        String carreraKey = "";
        if (carrera != null && carrera.getClave() != null) {
            carreraKey = carrera.getClave().toUpperCase();
        } else if (carrera != null && carrera.getNombre() != null) {
            String n = carrera.getNombre().toUpperCase();
            for (String c : claves) { if (n.contains(c)) { carreraKey = c; break; } }
        }

        // First row: header + 3 carreras
        PdfPTable grid = new PdfPTable(new float[]{2f, 3f, 3f, 3f});
        grid.setWidthPercentage(100f);

        PdfPCell lbl2 = cell("2. CARRERA", f(7.5f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT);
        lbl2.setRowspan(2);
        grid.addCell(lbl2);

        for (int i = 0; i < 3; i++) {
            boolean selected = claves[i].equals(carreraKey);
            grid.addCell(buildCarreraCell(nombres[i], selected));
        }
        for (int i = 3; i < 5; i++) {
            boolean selected = claves[i].equals(carreraKey);
            grid.addCell(buildCarreraCell(nombres[i], selected));
        }
        // Fill last row with empty cell
        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setBorder(Rectangle.NO_BORDER);
        grid.addCell(empty);

        PdfPCell wrapper = new PdfPCell(grid);
        wrapper.setPadding(0f);
        wrapper.setBorder(Rectangle.BOX);
        return wrapper;
    }

    private PdfPCell buildCarreraCell(String nombre, boolean selected) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setPadding(4f);
        c.setBackgroundColor(selected ? new Color(200, 220, 255) : BLANCO);

        Paragraph p = new Paragraph();
        p.add(new Chunk(selected ? "☒ " : "☐ ", f(10f, Font.BOLD, NEGRO)));
        p.add(new Chunk(nombre, f(7f, selected ? Font.BOLD : Font.NORMAL, NEGRO)));
        c.addElement(p);
        return c;
    }

    private PdfPCell buildControlSexoRow(String control, String sexo) {
        PdfPTable inner = new PdfPTable(new float[]{1.5f, 2f, 2f, 1.5f});
        inner.setWidthPercentage(100f);
        inner.addCell(cell("3. N° DE CONTROL", f(7.5f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT));
        inner.addCell(cell(control, f(9f, Font.BOLD, AZUL_NOMBRE), BLANCO, 5f, Element.ALIGN_LEFT));
        inner.addCell(cell("4. SEXO:", f(7f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT));
        inner.addCell(cell(sexo, f(9f, Font.BOLD, AZUL_NOMBRE), BLANCO, 5f, Element.ALIGN_LEFT));

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setPadding(0f);
        wrapper.setBorder(Rectangle.BOX);
        return wrapper;
    }

    private PdfPCell buildTutorFechaRow(String tutor, String fecha) {
        PdfPTable inner = new PdfPTable(new float[]{2.5f, 3f, 1.5f, 2f});
        inner.setWidthPercentage(100f);

        PdfPCell tutorLbl = cell("6. NOMBRE DEL\nTUTOR O TUTORA\n(docente que te imparte\nla tutoría)", f(7f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT);
        inner.addCell(tutorLbl);
        inner.addCell(cell(tutor, f(8.5f, Font.BOLD, AZUL_NOMBRE), BLANCO, 5f, Element.ALIGN_LEFT));
        inner.addCell(cell("5. FECHA:", f(7.5f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT));
        inner.addCell(cell(fecha, f(8.5f, Font.BOLD, AZUL_NOMBRE), BLANCO, 5f, Element.ALIGN_LEFT));

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setPadding(0f);
        wrapper.setBorder(Rectangle.BOX);
        return wrapper;
    }

    // ── Tabla preguntas P1 y P2 ──────────────────────────────────────────────

    private PdfPTable buildTablaPreguntas1y2(DeteccionNecesidades d) {
        PdfPTable t = new PdfPTable(new float[]{0.5f, 9.5f});
        t.setWidthPercentage(100f);
        t.setSpacingAfter(0f);

        // P1
        addPreguntaRow(t, "1", "De acuerdo a tus antecedentes académicos en tu educación media superior y a tus materias asignadas en el Instituto, ¿Consideras necesaria la asesoría académica respecto a alguna materia que estés por cursar?");
        addRespuestaP1(t, d);

        // P2
        addPreguntaRow(t, "2", "¿Cuentas con alguna beca?");
        addRespuestaSimple(t, "NOMBRE DE LA BECA", d.getTieneBeca(), d.getNombreBeca());

        return t;
    }

    // ── Tabla preguntas P3–P7 ────────────────────────────────────────────────

    private PdfPTable buildTablaPreguntas3a7(DeteccionNecesidades d) {
        PdfPTable t = new PdfPTable(new float[]{0.5f, 9.5f});
        t.setWidthPercentage(100f);

        // P3
        addPreguntaRow(t, "3", "¿Cuentas con escasez de materiales básicos e indispensables para el desarrollo de tus actividades académicas?");
        addRespuestaSimple(t, "DESCRIBE LOS MATERIALES QUE REQUIERES", d.getTieneEscasezMateriales(), d.getMaterialesRequeridos());

        // P4
        addPreguntaRow(t, "4", "¿Necesitas atención médica o algún cuidado especial?");
        addRespuestaSimple(t, "ESPECIFIQUE", d.getTieneAtencionMedica(), d.getEspecificacionMedica());

        // P5
        addPreguntaRow(t, "5", "¿Es importante para ti la vinculación escuela-familia, para padres, hijos y tutores? ¿Por qué?");
        addRespuestaSimple(t, "¿POR QUÉ?", d.getTieneVinculacionFamilia(), d.getRazonVinculacion());

        // P6
        addPreguntaRow(t, "6", "¿Requieres orientación psicológica?");
        addRespuestaPsicologica(t, d);

        // P7
        addPreguntaRow(t, "7", "Anota aquello que consideres necesario que no se encuentre en los apartados anteriores para que tu tutor tenga conocimiento de ello y recibas el apoyo adecuado.");
        addObservaciones(t, d.getObservaciones());

        return t;
    }

    // ── Helpers de filas ─────────────────────────────────────────────────────

    private void addPreguntaRow(PdfPTable t, String num, String texto) {
        PdfPCell numCell = new PdfPCell(new Phrase(num, f(9f, Font.BOLD, NEGRO)));
        numCell.setBackgroundColor(GRIS_CLARO);
        numCell.setPadding(5f);
        numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(numCell);

        PdfPCell txtCell = new PdfPCell(new Phrase(texto, f(8.5f, Font.NORMAL, NEGRO)));
        txtCell.setPadding(6f);
        t.addCell(txtCell);
    }

    private void addRespuestaP1(PdfPTable t, DeteccionNecesidades d) {
        // R cell
        PdfPCell rCell = new PdfPCell(new Phrase("R", f(9f, Font.BOLD, NEGRO)));
        rCell.setBackgroundColor(GRIS_CLARO);
        rCell.setPadding(5f);
        rCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(rCell);

        // Content: SI/NO + materias + Otra
        PdfPTable inner = new PdfPTable(new float[]{1f, 1f, 3f, 3f});
        inner.setWidthPercentage(100f);

        boolean hayNecesidad = (d.getNecesidadAlgebra() != null && d.getNecesidadAlgebra() == 1)
                || (d.getNecesidadCalculo() != null && d.getNecesidadCalculo() == 1)
                || (d.getNecesidadDerecho() != null && d.getNecesidadDerecho() == 1);

        inner.addCell(siNoCell(hayNecesidad, "SI"));
        inner.addCell(siNoCell(!hayNecesidad, "NO"));

        // Materias
        PdfPCell materiasLbl = cell("MATERIAS O TEMAS\nCON PROBLEMÁTICA:", f(7.5f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT);
        inner.addCell(materiasLbl);

        Paragraph materiasPar = new Paragraph();
        if (d.getNecesidadAlgebra() != null && d.getNecesidadAlgebra() == 1)
            materiasPar.add(new Chunk("Álgebra  ", f(8.5f, Font.BOLD, AZUL_NOMBRE)));
        if (d.getNecesidadCalculo() != null && d.getNecesidadCalculo() == 1)
            materiasPar.add(new Chunk("Cálculo  ", f(8.5f, Font.BOLD, AZUL_NOMBRE)));
        if (d.getNecesidadDerecho() != null && d.getNecesidadDerecho() == 1)
            materiasPar.add(new Chunk("Derecho  ", f(8.5f, Font.BOLD, AZUL_NOMBRE)));
        if (d.getNecesidadEconomica() != null && d.getNecesidadEconomica() == 1)
            materiasPar.add(new Chunk("Apoyo Económico  ", f(8.5f, Font.BOLD, AZUL_NOMBRE)));
        if (d.getNecesidadOtra() != null && !d.getNecesidadOtra().isBlank())
            materiasPar.add(new Chunk("Otra: " + d.getNecesidadOtra(), f(8.5f, Font.ITALIC, NEGRO)));
        if (materiasPar.isEmpty())
            materiasPar.add(new Chunk("—", f(8.5f, Font.NORMAL, GRIS_TEXT)));

        PdfPCell materiasVal = new PdfPCell(materiasPar);
        materiasVal.setPadding(5f);
        inner.addCell(materiasVal);

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setPadding(0f);
        t.addCell(wrapper);
    }

    private void addRespuestaSimple(PdfPTable t, String label, Integer valor, String detalle) {
        PdfPCell rCell = new PdfPCell(new Phrase("R", f(9f, Font.BOLD, NEGRO)));
        rCell.setBackgroundColor(GRIS_CLARO);
        rCell.setPadding(5f);
        rCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(rCell);

        boolean es1 = valor != null && valor == 1;
        PdfPTable inner = new PdfPTable(new float[]{1f, 1f, 2.5f, 4f});
        inner.setWidthPercentage(100f);
        inner.addCell(siNoCell(es1, "SI"));
        inner.addCell(siNoCell(!es1, "NO"));
        inner.addCell(cell(label, f(7.5f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT));
        inner.addCell(cell(safeStr(detalle), f(8.5f, Font.NORMAL, NEGRO), BLANCO, 5f, Element.ALIGN_LEFT));

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setPadding(0f);
        t.addCell(wrapper);
    }

    private void addRespuestaPsicologica(PdfPTable t, DeteccionNecesidades d) {
        PdfPCell rCell = new PdfPCell(new Phrase("R", f(9f, Font.BOLD, NEGRO)));
        rCell.setBackgroundColor(GRIS_CLARO);
        rCell.setPadding(5f);
        rCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(rCell);

        boolean es1 = d.getNecesidadPsicologica() != null && d.getNecesidadPsicologica() == 1;
        PdfPTable inner = new PdfPTable(new float[]{1f, 1f, 2.5f, 4f});
        inner.setWidthPercentage(100f);
        inner.addCell(siNoCell(es1, "SI"));
        inner.addCell(siNoCell(!es1, "NO"));
        inner.addCell(cell("TEMA ESPECÍFICO", f(7.5f, Font.BOLD, NEGRO), GRIS_CLARO, 5f, Element.ALIGN_LEFT));
        inner.addCell(cell(safeStr(d.getTemaPsicologico()), f(8.5f, Font.NORMAL, NEGRO), BLANCO, 5f, Element.ALIGN_LEFT));

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setPadding(0f);
        t.addCell(wrapper);
    }

    private void addObservaciones(PdfPTable t, String observaciones) {
        // Empty num cell
        PdfPCell emptyNum = new PdfPCell(new Phrase(""));
        emptyNum.setBorder(Rectangle.NO_BORDER);
        t.addCell(emptyNum);

        PdfPCell obsCell = new PdfPCell(new Phrase(safeStr(observaciones), f(9f, Font.NORMAL, NEGRO)));
        obsCell.setPadding(8f);
        obsCell.setMinimumHeight(80f);
        obsCell.setVerticalAlignment(Element.ALIGN_TOP);
        t.addCell(obsCell);
    }

    // ── Micro helpers ────────────────────────────────────────────────────────

    private PdfPCell siNoCell(boolean marked, String label) {
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.add(new Chunk(marked ? "[X] " : "[ ] ", f(9f, Font.BOLD, marked ? AZUL_NOMBRE : GRIS_TEXT)));
        p.add(new Chunk(label, f(8f, marked ? Font.BOLD : Font.NORMAL, NEGRO)));
        PdfPCell c = new PdfPCell(p);
        c.setPadding(5f);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private PdfPCell cell(String text, Font font, Color bg, float padding, int halign) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setBackgroundColor(bg);
        c.setPadding(padding);
        c.setHorizontalAlignment(halign);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        return p;
    }

    private String safeStr(String s) { return s != null ? s : ""; }

    // ── Page event: footer ────────────────────────────────────────────────────

    private static class HeaderFooterEvent extends PdfPageEventHelper {
        private final String periodo;
        HeaderFooterEvent(String periodo) { this.periodo = periodo; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            float pageWidth = document.getPageSize().getWidth();
            float left = document.leftMargin();
            float bottom = document.getPageSize().getBottom() + 18f;
            float usableWidth = pageWidth - document.leftMargin() - document.rightMargin();

            // Red line
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(new Color(180, 0, 0));
            cb.setLineWidth(1.2f);
            cb.moveTo(left, bottom + 22f);
            cb.lineTo(left + usableWidth, bottom + 22f);
            cb.stroke();

            // Footer table
            PdfPTable footer = new PdfPTable(new float[]{3f, 1f});
            footer.setTotalWidth(usableWidth);

            Font f6 = new Font(Font.HELVETICA, 6f, Font.NORMAL, new Color(60, 60, 60));
            Font f6r = new Font(Font.HELVETICA, 7f, Font.BOLD, new Color(180, 0, 0));

            // Left: address
            PdfPCell addrCell = new PdfPCell();
            addrCell.setBorder(Rectangle.NO_BORDER);
            addrCell.setPaddingTop(2f);
            Paragraph addr = new Paragraph();
            addr.add(new Chunk("Av. José Francisco Ruiz Massieu No. 5, Colonia Villa Moderna, Chilpancingo de los Bravo, Guerrero, México.\n", f6));
            addr.add(new Chunk("Tel. (747) 45 4 1300, Ext. 1326 y 1327  ·  Email: dda@chilpancingo.tecnm.mx\n", f6));
            addr.add(new Chunk("http://chilpancingo.tecnm.mx  ·  https://www.facebook.com/TecNMcampusChilpancingo", f6));
            addrCell.addElement(addr);
            footer.addCell(addrCell);

            // Right: year
            PdfPCell yearCell = new PdfPCell();
            yearCell.setBorder(Rectangle.NO_BORDER);
            yearCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            yearCell.setPaddingTop(2f);
            Paragraph yr = new Paragraph();
            yr.setAlignment(Element.ALIGN_RIGHT);
            yr.add(new Chunk("2026\naño de\nMargarita Maza", f6r));
            yearCell.addElement(yr);
            footer.addCell(yearCell);

            footer.writeSelectedRows(0, -1, left, bottom + 20f, cb);
        }
    }
}
