package com.bumh3r.service.impl;

import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.repository.ICoordinadorCarreraRepository;
import com.bumh3r.service.NombramientoCoordinadorPdfService;
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
import java.util.NoSuchElementException;

@Primary
@Service
public class NombramientoCoordinadorPdfServiceImpl implements NombramientoCoordinadorPdfService {

    private static final Color NEGRO      = Color.BLACK;
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

    @Autowired
    private ICoordinadorCarreraRepository coordinadorRepository;

    @Override
    public byte[] generarNombramiento(Integer idCoordinador) throws Exception {
        CoordinadorCarrera coordinador = coordinadorRepository.findById(idCoordinador)
                .orElseThrow(() -> new NoSuchElementException("Coordinador no encontrado"));

        String nombreCompleto = (coordinador.getApellido() + " " + coordinador.getNombre()).toUpperCase();
        String cargo          = coordinador.getCargo() != null ? coordinador.getCargo().toUpperCase() : "COORDINADOR(A) DE CARRERA";
        String carreraNombre  = coordinador.getCarrera()  != null ? coordinador.getCarrera().getNombre()  : "—";
        String semestreTexto  = coordinador.getSemestre() != null
                ? capitalize(coordinador.getSemestre().getPeriodo().toLowerCase() + " " + coordinador.getSemestre().getAnio())
                : "—";
        String fechaEnLetras  = convertirFechaALetras(LocalDate.now());

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

        // 2. NOMBRA
        Paragraph titulo = new Paragraph("N O M B R A", F_BOLD_11);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(6f);
        titulo.setSpacingAfter(10f);
        document.add(titulo);

        // 3. Cuerpo principal
        Paragraph cuerpo = new Paragraph();
        cuerpo.setAlignment(Element.ALIGN_JUSTIFIED);
        cuerpo.setLeading(16f);
        cuerpo.setSpacingAfter(12f);
        cuerpo.add(new Chunk("      A la C. ", F_NORMAL_9));
        cuerpo.add(new Chunk(nombreCompleto, F_VALOR));
        cuerpo.add(new Chunk(" con número de control ", F_NORMAL_9));
        cuerpo.add(new Chunk(coordinador.getNumeroControl(), F_BOLD_9));
        cuerpo.add(new Chunk(", como ", F_NORMAL_9));
        cuerpo.add(new Chunk(cargo, F_BOLD_9));
        cuerpo.add(new Chunk(" de la carrera de ", F_NORMAL_9));
        cuerpo.add(new Chunk(carreraNombre, F_VALOR));
        cuerpo.add(new Chunk(
                " del Instituto Tecnológico de Chilpancingo, dentro del Programa Institucional"
                + " de Tutorías para el semestre ", F_NORMAL_9));
        cuerpo.add(new Chunk(semestreTexto, F_VALOR));
        cuerpo.add(new Chunk(
                ", en cumplimiento a lo establecido en el Programa Nacional de Tutorías del"
                + " Tecnológico Nacional de México.",
                F_NORMAL_9));
        document.add(cuerpo);

        // 4. Tabla de datos del coordinador
        PdfPTable tablaDatos = new PdfPTable(new float[]{32f, 22f, 24f, 22f});
        tablaDatos.setWidthPercentage(95f);
        tablaDatos.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaDatos.setSpacingBefore(10f);
        tablaDatos.setSpacingAfter(12f);

        Font fHeader = new Font(Font.HELVETICA, 8, Font.BOLD, NEGRO);
        Font fDato   = new Font(Font.HELVETICA, 9, Font.NORMAL, AZUL_VALOR);

        for (String header : new String[]{"Nombre Completo", "No. Control", "Carrera", "Semestre"}) {
            PdfPCell h = new PdfPCell(new Phrase(header, fHeader));
            h.setBackgroundColor(GRIS_HEADER);
            h.setHorizontalAlignment(Element.ALIGN_CENTER);
            h.setPadding(5f);
            tablaDatos.addCell(h);
        }

        for (String dato : new String[]{
                coordinador.getNombre() + " " + coordinador.getApellido(),
                coordinador.getNumeroControl(),
                carreraNombre,
                semestreTexto}) {
            PdfPCell d = new PdfPCell(new Phrase(dato, fDato));
            d.setHorizontalAlignment(Element.ALIGN_CENTER);
            d.setPadding(5f);
            tablaDatos.addCell(d);
        }

        document.add(tablaDatos);

        // 5. Párrafo de cierre
        Paragraph cierre = new Paragraph();
        cierre.setAlignment(Element.ALIGN_JUSTIFIED);
        cierre.setLeading(16f);
        cierre.setSpacingAfter(24f);
        cierre.add(new Chunk(
                "      Para los fines y usos que a la interesada convengan, se extiende el"
                + " presente a los ", F_NORMAL_9));
        cierre.add(new Chunk(fechaEnLetras, F_BOLD_9));
        cierre.add(new Chunk(
                ", de esta ciudad de Chilpancingo, de los Bravo Guerrero.", F_NORMAL_9));
        document.add(cierre);

        // 6. Firmas
        document.add(buildFirmas());

        // 7. C.c.p.
        Paragraph ccp = new Paragraph();
        ccp.setSpacingBefore(10f);
        ccp.add(new Chunk("C.c.p Archivo\n", F_NORMAL_8));
        ccp.add(new Chunk("SRZB/AMB/spm", F_NORMAL_8));
        document.add(ccp);

        // 8. Pie de página
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

    @Override
    public String validar(Integer idCoordinador) {
        CoordinadorCarrera coordinador = coordinadorRepository.findById(idCoordinador).orElse(null);
        if (coordinador == null || !Integer.valueOf(1).equals(coordinador.getActivo()))
            return "El coordinador no existe o ha sido dado de baja del sistema.";
        return null;
    }

    private PdfPTable buildEncabezado() {
        PdfPTable header = new PdfPTable(new float[]{22f, 16f, 14f, 21f, 40f});
        header.setWidthPercentage(100f);
        header.setSpacingAfter(4f);

        header.addCell(imgCell("/static/images/tecnm/sep_logo.png", 33f, Element.ALIGN_LEFT));

        PdfPCell gap = new PdfPCell(new Phrase(" "));
        gap.setBorder(Rectangle.NO_BORDER);
        header.addCell(gap);

        header.addCell(imgCell("/static/images/tecnm/tecnologico_nacional.png", 32f, Element.ALIGN_LEFT));

        PdfPCell spacer = new PdfPCell(new Phrase(" "));
        spacer.setBorder(Rectangle.NO_BORDER);
        header.addCell(spacer);

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

    private PdfPTable buildFirmas() {
        PdfPTable firmas = new PdfPTable(new float[]{33f, 34f, 33f});
        firmas.setWidthPercentage(100f);
        firmas.setSpacingAfter(0f);

        firmas.addCell(buildColumna3(true,
                "ADRIANA MALDONADO BRAVO",
                new String[]{"Jefa del Departamento de", "Desarrollo Académico"}));
        firmas.addCell(buildColumna3(false,
                "SERGIO RICARDO ZAGAL BARRERA",
                new String[]{"Subdirector Académico"}));
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
        for (String c : cargo)
            addNoBorderCell(col, new Phrase(c, F_NORMAL_8), Element.ALIGN_CENTER, 1f);

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
