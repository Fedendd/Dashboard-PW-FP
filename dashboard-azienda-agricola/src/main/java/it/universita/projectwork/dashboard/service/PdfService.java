package it.universita.projectwork.dashboard.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfService {

    public byte[] render(String html) {
        final String safeHtml = sanitizeHtml(html);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(safeHtml, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la generazione del PDF", e);
        }
    }

    private String sanitizeHtml(String html) {
        if (html == null)
            return "";

        return html
                .replace("&nbsp;", "&#160;")
                .replace("&ensp;", "&#8194;")
                .replace("&emsp;", "&#8195;")

                .replace("&ndash;", "&#8211;")
                .replace("&mdash;", "&#8212;")
                .replace("&ldquo;", "&#8220;")
                .replace("&rdquo;", "&#8221;")
                .replace("&lsquo;", "&#8216;")
                .replace("&rsquo;", "&#8217;")

                .replace("&copy;", "&#169;")
                .replace("&reg;", "&#174;");
    }
}