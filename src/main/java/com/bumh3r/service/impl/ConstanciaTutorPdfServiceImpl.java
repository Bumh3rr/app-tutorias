package com.bumh3r.service.impl;

import com.bumh3r.entity.*;
import com.bumh3r.repository.*;
import com.bumh3r.service.ConstanciaTutorPdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class ConstanciaTutorPdfServiceImpl implements ConstanciaTutorPdfService {

    private static final Color NEGRO       = Color.BLACK;
    private static final Color GRIS_HEADER = new Color(232, 232, 232);
    private static final Color GRIS_TEXTO  = new Color(68, 68, 68);
    private static final Color ROJO_PIE    = new Color(139, 0, 0);
    private static final Color AZUL_VALOR  = new Color(0, 51, 153);

    private static final Font F_NORMAL_8 = new Font(Font.HELVETICA, 8,  Font.NORMAL, NEGRO);
    private static final Font F_NORMAL_9 = new Font(Font.HELVETICA, 9,  Font.NORMAL, NEGRO);
    private static final Font F_BOLD_8   = new Font(Font.HELVETICA, 8,  Font.BOLD,   NEGRO);
    private static final Font F_BOLD_9   = new Font(Font.HELVETICA, 9,  Font.BOLD,   NEGRO);
    private static final Font F_BOLD_11  = new Font(Font.HELVETICA, 11, Font.BOLD,   NEGRO);
    private static final Font F_ITALIC_7 = new Font(Font.HELVETICA, 7,  Font.ITALIC, GRIS_TEXTO);
    private static final Font F_VALOR    = new Font(Font.HELVETICA, 9,  Font.BOLD,   AZUL_VALOR);
    private static final Font F_PIE      = new Font(Font.HELVETICA, 6,  Font.NORMAL, GRIS_TEXTO);

    @Autowired private ITutorRepository tutorRepository;
    @Autowired private ISemestreRepository semestreRepository;
    @Autowired private IGrupoRepository grupoRepository;
    @Autowired private IGrupoTutoradoRepository grupoTutoradoRepository;

    @Override
    public byte[] generarConstanciaTutor(Integer idTutor, Integer idSemestre) throws Exception {
        Tutor tutor = tutorRepository.findById(idTutor)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        Semestre semestre = semestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));

        List<Grupo> grupos = grupoRepository.findByActivoAndTutorAndSemestre(1, tutor, semestre);

        long totalTutorados = grupos.stream()
                .mapToLong(g -> grupoTutoradoRepository.countByGrupoAndActivo(g, 1))
                .sum();

        String periodo      = semestre.getPeriodo().toLowerCase() + " " + semestre.getAnio();
        String periodoCapit = capitalize(periodo);
        String fechaEnLetras = convertirFechaALetras(LocalDate.now());
        String tutorMayus   = (tutor.getApellido() + " " + tutor.getNombre()).toUpperCase();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 48, 48, 32, 28);
        PdfWriter.getInstance(document, baos);
        document.open();

        document.add(buildEncabezado());
        document.add(new LineSeparator(0.5f, 100, NEGRO, Element.ALIGN_CENTER, -2));
        Paragraph espacio = new Paragraph(" ");
        espacio.setLeading(8f);
        document.add(espacio);

        // 1. Párrafo inicial
        Paragraph intro = new Paragraph();
        intro.setAlignment(Element.ALIGN_JUSTIFIED);
        intro.setLeading(16f);
        intro.setSpacingAfter(10f);
        intro.add(new Chunk("      La que suscribe C. ", F_NORMAL_9));
        intro.add(new Chunk("ADRIANA MALDONADO BRAVO", F_BOLD_9));
        intro.add(new Chunk(
                ", jefa del Departamento de Desarrollo Académico del ", F_NORMAL_9));
        intro.add(new Chunk("Instituto Tecnológico de Chilpancingo.", F_NORMAL_9));
        document.add(intro);

        // 2. HACE CONSTAR
        Paragraph hc = new Paragraph("HACE CONSTAR", F_BOLD_11);
        hc.setAlignment(Element.ALIGN_CENTER);
        hc.setSpacingBefore(6f);
        hc.setSpacingAfter(10f);
        document.add(hc);

        // 3. Cuerpo principal
        Paragraph cuerpo = new Paragraph();
        cuerpo.setAlignment(Element.ALIGN_JUSTIFIED);
        cuerpo.setLeading(16f);
        cuerpo.setSpacingAfter(12f);
        cuerpo.add(new Chunk("      Que la C. ", F_NORMAL_9));
        cuerpo.add(new Chunk(tutorMayus, F_VALOR));
        cuerpo.add(new Chunk(" catedrática de este Instituto, fungió como ", F_NORMAL_9));
        cuerpo.add(new Chunk("TUTORA", F_BOLD_9));
        cuerpo.add(new Chunk(
                " dentro del Programa Institucional de Tutorías, y realizó las actividades que"
                + " se programaron en el semestre ", F_NORMAL_9));
        cuerpo.add(new Chunk(periodo, F_VALOR));
        cuerpo.add(new Chunk(
                ", entregando en tiempo y forma un informe con el reporte semestral del tutor,"
                + " que contiene el número de estudiantes atendidos por semestre, cumpliendo al ",
                F_NORMAL_9));
        cuerpo.add(new Chunk("100% de los indicadores de eficiencia académica", F_BOLD_9));
        cuerpo.add(new Chunk(
                " de la acción tutorial que son: Calidad de la atención tutorial, Congruencia,"
                + " Cumplimiento de metas establecidas, impacto, viabilidad y funcionalidad.",
                F_NORMAL_9));
        document.add(cuerpo);

        // 4. Tabla semestre/tutorados
        PdfPTable tablaDatos = new PdfPTable(new float[]{50f, 50f});
        tablaDatos.setWidthPercentage(55f);
        tablaDatos.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaDatos.setSpacingBefore(10f);
        tablaDatos.setSpacingAfter(12f);

        Font fHeaderTabla = new Font(Font.HELVETICA, 8, Font.BOLD, NEGRO);
        PdfPCell hSemestre = new PdfPCell(new Phrase("Semestre", fHeaderTabla));
        hSemestre.setBackgroundColor(GRIS_HEADER);
        hSemestre.setHorizontalAlignment(Element.ALIGN_CENTER);
        hSemestre.setPadding(5f);
        tablaDatos.addCell(hSemestre);

        PdfPCell hNum = new PdfPCell(
                new Phrase("Número de Estudiantes Tutorados", fHeaderTabla));
        hNum.setBackgroundColor(GRIS_HEADER);
        hNum.setHorizontalAlignment(Element.ALIGN_CENTER);
        hNum.setPadding(5f);
        tablaDatos.addCell(hNum);

        Font fDato = new Font(Font.HELVETICA, 9, Font.NORMAL, AZUL_VALOR);
        PdfPCell dSemestre = new PdfPCell(new Phrase(periodoCapit, fDato));
        dSemestre.setHorizontalAlignment(Element.ALIGN_CENTER);
        dSemestre.setPadding(5f);
        tablaDatos.addCell(dSemestre);

        PdfPCell dNum = new PdfPCell(new Phrase(totalTutorados + " tutorados", fDato));
        dNum.setHorizontalAlignment(Element.ALIGN_CENTER);
        dNum.setPadding(5f);
        tablaDatos.addCell(dNum);

        document.add(tablaDatos);

        // 5. Párrafo de cierre
        Paragraph cierre = new Paragraph();
        cierre.setAlignment(Element.ALIGN_JUSTIFIED);
        cierre.setLeading(16f);
        cierre.setSpacingAfter(24f);
        cierre.add(new Chunk(
                "      Para los fines y usos que a la interesada convengan, se extiende la"
                + " presente a los ", F_NORMAL_9));
        cierre.add(new Chunk(fechaEnLetras, F_BOLD_9));
        cierre.add(new Chunk(
                ", de esta ciudad de Chilpancingo, de los Bravo Guerrero.", F_NORMAL_9));
        document.add(cierre);

        // 6. Firmas
        document.add(buildFirmasTutor());

        // 7. C.c.p.
        Paragraph ccp = new Paragraph();
        ccp.setSpacingBefore(10f);
        ccp.add(new Chunk("C.c.p Archivo\n", F_NORMAL_8));
        ccp.add(new Chunk("SRZB/AMB/spm", F_NORMAL_8));
        document.add(ccp);

        // 8. Pie
        document.add(new LineSeparator(1.5f, 100, ROJO_PIE, Element.ALIGN_CENTER, -2));
        Paragraph pie = new Paragraph(
                "Av. José Francisco Ruiz Massieu No. 5, Colonia Villa Moderna, "
                + "Chilpancingo de los Bravo, Guerrero, México. "
                + "Tel. (747) 45 4 1300, Ext. 1326 y 1340 · email: dda@chilpancingo.tecnm.mx · "
                + "http://chilpancingo.tecnm.mx", F_PIE);
        pie.setAlignment(Element.ALIGN_CENTER);
        document.add(pie);

        document.close();
        return baos.toByteArray();
    }

    private PdfPTable buildEncabezado() {
        // 5 columns: sep_logo | gap | tecnm_logo | spacer | (emblema + text)
        PdfPTable header = new PdfPTable(new float[]{22f, 16f, 14f, 21f, 40f});
        header.setWidthPercentage(100f);
        header.setSpacingAfter(4f);

        // col 1: SEP logo
        header.addCell(imgCell("/static/images/tecnm/sep_logo.png", 33f, Element.ALIGN_LEFT));

        // col 2: visual gap between the two logos
        PdfPCell gap = new PdfPCell(new Phrase(" "));
        gap.setBorder(Rectangle.NO_BORDER);
        header.addCell(gap);

        // col 3: TecNM logo
        header.addCell(imgCell("/static/images/tecnm/tecnologico_nacional.png", 32f, Element.ALIGN_LEFT));

        // col 4: empty spacer
        PdfPCell spacer = new PdfPCell(new Phrase(" "));
        spacer.setBorder(Rectangle.NO_BORDER);
        header.addCell(spacer);

        // col 5: emblema (top) + institutional text (below), both right-aligned
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        rightCell.setPadding(2f);

        try (InputStream is = getClass().getResourceAsStream("/static/images/tecnm/emblema.png")) {
            if (is != null) {
                Image img = Image.getInstance(is.readAllBytes());
                img.scaleToFit(999f, 48f);
                img.setAlignment(Image.ALIGN_RIGHT);
                rightCell.addElement(img);
            }
        } catch (Exception ignored) {}

        Paragraph instPar = new Paragraph();
        instPar.setAlignment(Element.ALIGN_RIGHT);
        instPar.add(new Chunk("Instituto Tecnológico de Chilpancingo\n", F_BOLD_9));
        instPar.add(new Chunk("Departamento de Desarrollo Académico",
                new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS_TEXTO)));
        rightCell.addElement(instPar);
        header.addCell(rightCell);

        return header;
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

    private PdfPTable buildFirmasTutor() {
        PdfPTable firmas = new PdfPTable(new float[]{33f, 34f, 33f});
        firmas.setWidthPercentage(100f);
        firmas.setSpacingAfter(0f);

        // Col 1: ATENTAMENTE — DDA
        firmas.addCell(buildColumna3(true,
                "ADRIANA MALDONADO BRAVO",
                new String[]{"Jefa del Departamento de", "Desarrollo Académico"}));

        // Col 2: Subdirector
        firmas.addCell(buildColumna3(false,
                "SERGIO RICARDO ZAGAL BARRERA",
                new String[]{"Subdirector Académico"}));

        // Col 3: CIT
        firmas.addCell(buildColumna3(false,
                "SUSANA PINEDA MILLÁN",
                new String[]{"Coordinadora Institucional", "de Tutorías"}));

        return firmas;
    }

    private PdfPCell buildColumna3(boolean conLema, String nombre, String[] cargo) {
        PdfPTable col = new PdfPTable(1);
        col.setWidthPercentage(100f);

        if (conLema) {
            addNoBorderCell(col, new Phrase("ATENTAMENTE", F_BOLD_9), Element.ALIGN_LEFT, 2f);
            PdfPCell lemaCell = new PdfPCell();
            lemaCell.setBorder(Rectangle.NO_BORDER);
            lemaCell.setPadding(1f);
            Paragraph lema = new Paragraph();
            lema.add(new Chunk("Excelencia en Educación Tecnológica®\n", F_ITALIC_7));
            lema.add(new Chunk("Crear Tecnología es Forjar Libertad", F_ITALIC_7));
            lemaCell.addElement(lema);
            col.addCell(lemaCell);
        } else {
            PdfPCell vacio = new PdfPCell(new Phrase("\n\n", F_ITALIC_7));
            vacio.setBorder(Rectangle.NO_BORDER);
            vacio.setPadding(1f);
            col.addCell(vacio);
            PdfPCell vacio2 = new PdfPCell(new Phrase(" "));
            vacio2.setBorder(Rectangle.NO_BORDER);
            vacio2.setPadding(1f);
            col.addCell(vacio2);
        }

        PdfPCell sello = new PdfPCell(new Phrase(" "));
        sello.setBorder(Rectangle.NO_BORDER);
        sello.setFixedHeight(70f);
        col.addCell(sello);

        PdfPCell linea = new PdfPCell();
        linea.setBorder(Rectangle.TOP);
        linea.setBorderColorTop(NEGRO);
        linea.setBorderWidthTop(0.5f);
        linea.setFixedHeight(4f);
        linea.setPaddingLeft(6f);
        linea.setPaddingRight(6f);
        col.addCell(linea);

        addNoBorderCell(col, new Phrase(nombre, F_BOLD_8), Element.ALIGN_CENTER, 2f);
        for (String c : cargo) {
            addNoBorderCell(col, new Phrase(c, F_NORMAL_8), Element.ALIGN_CENTER, 1f);
        }

        PdfPCell wrapper = new PdfPCell();
        wrapper.addElement(col);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setPadding(4f);
        return wrapper;
    }

    private void addNoBorderCell(PdfPTable t, Phrase content, int align, float padding) {
        PdfPCell c = new PdfPCell(content);
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        c.setPadding(padding);
        t.addCell(c);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String convertirFechaALetras(LocalDate fecha) {
        String[] dias = {"","uno","dos","tres","cuatro","cinco","seis","siete",
            "ocho","nueve","diez","once","doce","trece","catorce","quince",
            "dieciséis","diecisiete","dieciocho","diecinueve","veinte",
            "veintiuno","veintidós","veintitrés","veinticuatro","veinticinco",
            "veintiséis","veintisiete","veintiocho","veintinueve","treinta","treinta y uno"};
        String[] meses = {"","enero","febrero","marzo","abril","mayo","junio",
            "julio","agosto","septiembre","octubre","noviembre","diciembre"};
        return dias[fecha.getDayOfMonth()] + " días del mes de "
               + meses[fecha.getMonthValue()] + " de " + convertirAnioALetras(fecha.getYear());
    }

    private String convertirAnioALetras(int anio) {
        return switch (anio) {
            case 2024 -> "dos mil veinticuatro";
            case 2025 -> "dos mil veinticinco";
            case 2026 -> "dos mil veintiséis";
            case 2027 -> "dos mil veintisiete";
            case 2028 -> "dos mil veintiocho";
            case 2029 -> "dos mil veintinueve";
            case 2030 -> "dos mil treinta";
            default   -> String.valueOf(anio);
        };
    }
}
