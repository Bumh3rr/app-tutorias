package com.bumh3r.service.impl;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.*;
import com.bumh3r.repository.*;
import com.bumh3r.service.AsistenciaService;
import com.bumh3r.service.ConstanciaTutoradoPdfService;
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
public class ConstanciaTutoradoPdfServiceImpl implements ConstanciaTutoradoPdfService {

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

    @Autowired private ITutoradoRepository tutoradoRepository;
    @Autowired private ISemestreRepository semestreRepository;
    @Autowired private IGrupoTutoradoRepository grupoTutoradoRepository;
    @Autowired private AsistenciaService asistenciaService;

    @Override
    public byte[] generarConstanciaTutorado(Integer idTutorado, Integer idSemestre) throws Exception {
        Tutorado tutorado = tutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        Semestre semestre = semestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));

        List<GrupoTutorado> gts = grupoTutoradoRepository
                .findByTutoradoAndGrupoSemestreAndActivo(tutorado, semestre, 1);
        GrupoTutorado gt = gts.isEmpty() ? null : gts.get(0);

        ResumenAsistenciaDTO resumen = asistenciaService.calcularResumenAsistencia(idTutorado);
        double pct = resumen.getPorcentaje();
        String nivel;
        String valorNum;
        if (pct >= 100.0)     { nivel = "EXCELENTE";     valorNum = "4.00"; }
        else if (pct >= 90.0) { nivel = "BUENO";         valorNum = "3.50"; }
        else if (pct >= 80.0) { nivel = "REGULAR";       valorNum = "3.00"; }
        else                  { nivel = "NO ACREDITADO"; valorNum = "0.00"; }

        String fechaEnLetras  = convertirFechaALetras(LocalDate.now());
        String periodoMayus   = semestre.getPeriodo().toUpperCase() + " " + semestre.getAnio();
        String carreraMayus   = (tutorado.getCarrera() != null)
                ? tutorado.getCarrera().getNombre().toUpperCase() : "";
        String nombreTutorado = (tutorado.getNombre() + " " + tutorado.getApellido()).toUpperCase();
        String control        = tutorado.getNumeroControl();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 48, 48, 32, 28);
        PdfWriter.getInstance(document, baos);
        document.open();

        document.add(buildEncabezado());
        document.add(new LineSeparator(0.5f, 100, NEGRO, Element.ALIGN_CENTER, -2));
        Paragraph espacio = new Paragraph(" ");
        espacio.setLeading(8f);
        document.add(espacio);

        // 1. Título
        Paragraph titulo = new Paragraph(
                "ANEXO XVI. CONSTANCIA DE CUMPLIMIENTO DE ACTIVIDAD \nCOMPLEMENTARIA", F_BOLD_11);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(14f);
        document.add(titulo);

        // 2. Destinatario
        Paragraph dest = new Paragraph();
        dest.setSpacingAfter(12f);
        dest.add(new Chunk("MARGARITA ALCOCER SOLACHE\n", F_BOLD_9));
        dest.add(new Chunk("JEFA DEL DEPARTAMENTO DE SERVICIOS ESCOLARES\n", F_BOLD_9));
        dest.add(new Chunk("PRESENTE.", F_BOLD_9));
        document.add(dest);

        // 3. Cuerpo principal
        Paragraph cuerpo = new Paragraph();
        cuerpo.setAlignment(Element.ALIGN_JUSTIFIED);
        cuerpo.setLeading(16f);
        cuerpo.setSpacingAfter(12f);
        cuerpo.add(new Chunk("      La que suscribe ", F_BOLD_9));
        cuerpo.add(new Chunk("ADRIANA MALDONADO BRAVO", F_BOLD_9));
        cuerpo.add(new Chunk(
                ", por este medio se permite hacer de su conocimiento que la/el estudiante ", F_NORMAL_9));
        cuerpo.add(new Chunk(nombreTutorado, F_VALOR));
        cuerpo.add(new Chunk(" con número de control ", F_NORMAL_9));
        cuerpo.add(new Chunk(control, F_VALOR));
        cuerpo.add(new Chunk(" de la carrera de ", F_NORMAL_9));
        cuerpo.add(new Chunk(carreraMayus, F_VALOR));
        cuerpo.add(new Chunk(" participó como tutorado/a en el ", F_NORMAL_9));
        cuerpo.add(new Chunk("PROGRAMA INSTITUCIONAL DE TUTORÍAS (PIT)", F_BOLD_9));
        cuerpo.add(new Chunk(", con el nivel de desempeño ", F_NORMAL_9));
        cuerpo.add(new Chunk(nivel, F_VALOR));
        cuerpo.add(new Chunk(" y un valor numérico de ", F_NORMAL_9));
        cuerpo.add(new Chunk(valorNum, F_VALOR));
        cuerpo.add(new Chunk(", durante el período escolar ", F_NORMAL_9));
        cuerpo.add(new Chunk(periodoMayus, F_VALOR));
        cuerpo.add(new Chunk(" con un valor curricular de ", F_NORMAL_9));
        cuerpo.add(new Chunk("1 (UN) crédito", F_BOLD_9));
        cuerpo.add(new Chunk(".", F_NORMAL_9));
        document.add(cuerpo);

        // 4. Segundo párrafo
        Paragraph cierre = new Paragraph();
        cierre.setAlignment(Element.ALIGN_JUSTIFIED);
        cierre.setLeading(16f);
        cierre.setSpacingAfter(20f);
        cierre.add(new Chunk(
                "      Para los fines y usos que a la interesada le convenga y de acuerdo a los "
                + "archivos encontrados en el Departamento, se extiende la presente a los ", F_NORMAL_9));
        cierre.add(new Chunk(fechaEnLetras, F_BOLD_9));
        cierre.add(new Chunk(" en esta Ciudad de Chilpancingo de los Bravo, Guerrero.", F_NORMAL_9));
        document.add(cierre);

        // 5. Nota de nivel
        Paragraph nota = new Paragraph(
                "* Nivel calculado por el sistema según porcentaje de asistencia:\n"
                + "Excelente 100% (4.00)  ·  Bueno 90–99% (3.50)  ·  Regular 80–89% (3.00)",
                F_ITALIC_7);
        nota.setSpacingAfter(24f);
        document.add(nota);

        // 6. Firmas
        document.add(buildFirmasTutorado());

        // 7. Espacio Servicios Escolares
        PdfPTable svcEsc = new PdfPTable(new float[]{60f, 40f});
        svcEsc.setWidthPercentage(100f);
        PdfPCell vacioSvc = new PdfPCell(new Phrase(" "));
        vacioSvc.setBorder(Rectangle.NO_BORDER);
        svcEsc.addCell(vacioSvc);
        PdfPCell selloSvc = new PdfPCell(new Phrase(" "));
        selloSvc.setBorder(Rectangle.NO_BORDER);
        selloSvc.setFixedHeight(60f);
        svcEsc.addCell(selloSvc);
        document.add(svcEsc);

        // 8. C.c.p.
        Paragraph ccp = new Paragraph();
        ccp.setSpacingBefore(8f);
        ccp.add(new Chunk("C.c.p.- División de Estudios Profesionales.\n", F_NORMAL_8));
        ccp.add(new Chunk("Estudiante.\n", F_NORMAL_8));
        ccp.add(new Chunk("Archivo\n", F_NORMAL_8));
        ccp.add(new Chunk("SRZB/AMB/cacc", F_NORMAL_8));
        document.add(ccp);

        // 9. Pie
        document.add(new LineSeparator(1.5f, 100, ROJO_PIE, Element.ALIGN_CENTER, -2));
        Paragraph pie = new Paragraph(
                "Av. José Francisco Ruiz Massieu No. 5, Colonia Villa Moderna, "
                + "Chilpancingo de los Bravo, Guerrero, México. "
                + "Tel. (747) 45 4 1300, (747) 45 4 1322 · email: sa@chilpancingo.tecnm.mx · "
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
        PdfPCell gap = new PdfPCell(new Phrase("  "));
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

    private PdfPTable buildFirmasTutorado() {
        PdfPTable firmas = new PdfPTable(new float[]{50f, 50f});
        firmas.setWidthPercentage(100f);
        firmas.setSpacingAfter(0f);

        firmas.addCell(buildColumnaFirma("ATENTAMENTE", true,
                "ADRIANA MALDONADO BRAVO",
                new String[]{"JEFA DEL DEPARTAMENTO DE", "DESARROLLO ACADÉMICO"}));
        firmas.addCell(buildColumnaFirma("Vo.Bo.", false,
                "SERGIO RICARDO ZAGAL BARRERA",
                new String[]{"SUBDIRECTOR ACADÉMICO"}));

        return firmas;
    }

    private PdfPCell buildColumnaFirma(String tituloCol, boolean conLema,
                                       String nombre, String[] cargo) {
        PdfPTable col = new PdfPTable(1);
        col.setWidthPercentage(100f);

        addNoBorderCell(col, new Phrase(tituloCol, F_BOLD_9), Element.ALIGN_LEFT, 2f);

        if (conLema) {
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
        linea.setPaddingLeft(10f);
        linea.setPaddingRight(10f);
        col.addCell(linea);

        addNoBorderCell(col, new Phrase(nombre, F_BOLD_9), Element.ALIGN_CENTER, 2f);
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
