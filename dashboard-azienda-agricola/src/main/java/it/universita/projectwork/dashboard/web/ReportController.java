package it.universita.projectwork.dashboard.web;

import it.universita.projectwork.dashboard.model.DatoAmbientale;
import it.universita.projectwork.dashboard.model.DatoProduttivo;
import it.universita.projectwork.dashboard.repo.DatoAmbientaleRepo;
import it.universita.projectwork.dashboard.repo.DatoProduttivoRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class ReportController {

    private final DatoAmbientaleRepo ambientaleRepo;
    private final DatoProduttivoRepo produttivoRepo;

    public ReportController(DatoAmbientaleRepo ambientaleRepo,
            DatoProduttivoRepo produttivoRepo) {
        this.ambientaleRepo = ambientaleRepo;
        this.produttivoRepo = produttivoRepo;
    }

    private static final DateTimeFormatter FMT_FORM = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            .withLocale(Locale.ITALY);

    private static final DateTimeFormatter FMT_VIEW = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withLocale(Locale.ITALY);

    private String fmtTs(Instant ts) {
        if (ts == null)
            return "";
        LocalDateTime ldt = LocalDateTime.ofInstant(ts, ZoneId.systemDefault());
        return FMT_VIEW.format(ldt);
    }

    private String fmtTs(LocalDateTime ts) {
        return ts == null ? "" : FMT_VIEW.format(ts);
    }

    private boolean inRange(Instant ts, LocalDateTime from, LocalDateTime to) {
        if (ts == null)
            return false;
        LocalDateTime ldt = LocalDateTime.ofInstant(ts, ZoneId.systemDefault());
        boolean geFrom = (from == null) || !ldt.isBefore(from);
        boolean ltTo = (to == null) || ldt.isBefore(to);
        return geFrom && ltTo;
    }

    private boolean inRange(LocalDateTime ts, LocalDateTime from, LocalDateTime to) {
        if (ts == null)
            return false;
        boolean geFrom = (from == null) || !ts.isBefore(from);
        boolean ltTo = (to == null) || ts.isBefore(to);
        return geFrom && ltTo;
    }

    @GetMapping("/report")
    public String reportPage(@RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Model model) {

        LocalDateTime toLdt = parseOrDefaultToNow(to);
        LocalDateTime fromLdt = parseOrDefaultFrom(toLdt, from);

        List<DatoAmbientale> amb = ambientaliAsc(fromLdt, toLdt);
        List<DatoProduttivo> prod = produttiviAsc(fromLdt, toLdt);

        model.addAttribute("amb", amb);
        model.addAttribute("prod", prod);
        model.addAttribute("from", FMT_FORM.format(fromLdt));
        model.addAttribute("to", FMT_FORM.format(toLdt));

        return "report";
    }

    @GetMapping("/report/csv")
    public void exportCsv(@RequestParam String type,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            HttpServletResponse resp) throws Exception {

        LocalDateTime toLdt = parseOrDefaultToNow(to);
        LocalDateTime fromLdt = parseOrDefaultFrom(toLdt, from);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");

        String filename = "report_" + type + ".csv";
        resp.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        try (var w = new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8)) {
            if ("amb".equalsIgnoreCase(type)) {
                w.write("timestamp,temperatura,umiditaSuolo,pioggia\n");
                for (var d : ambientaliAsc(fromLdt, toLdt)) {
                    w.write(String.join(",",
                            quote(fmtTs(d.getTimestamp())),
                            n(d.getTemperatura()),
                            n(d.getUmiditaSuolo()),
                            n(d.getPioggia())));
                    w.write("\n");
                }
            } else {
                w.write("timestamp,resa,crescita,acquaUtilizzata\n");
                for (var p : produttiviAsc(fromLdt, toLdt)) {
                    w.write(String.join(",",
                            quote(fmtTs(p.getTimestamp())),
                            n(p.getResa()),
                            n(p.getCrescita()),
                            n(p.getAcquaUtilizzata())));
                    w.write("\n");
                }
            }
        }
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String from,
            @RequestParam(required = false) String to) throws Exception {

        LocalDateTime toLdt = parseOrDefaultToNow(to);
        LocalDateTime fromLdt = parseOrDefaultFrom(toLdt, from);

        List<DatoAmbientale> amb = ambientaliAsc(fromLdt, toLdt);
        List<DatoProduttivo> prod = produttiviAsc(fromLdt, toLdt);

        String html = buildPdfHtml(fromLdt, toLdt, amb, prod);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new PdfRendererBuilder()
                .useFastMode()
                .withHtmlContent(html, null)
                .toStream(baos)
                .run();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    private List<DatoAmbientale> ambientaliAsc(LocalDateTime from, LocalDateTime to) {
        List<DatoAmbientale> list = ambientaleRepo.findTop50ByOrderByTimestampDesc();
        return list.stream()
                .filter(d -> inRange(d.getTimestamp(), from, to))
                .sorted(Comparator.comparing(DatoAmbientale::getTimestamp))
                .collect(Collectors.toList());
    }

    private List<DatoProduttivo> produttiviAsc(LocalDateTime from, LocalDateTime to) {
        List<DatoProduttivo> list = produttivoRepo.findTop50ByOrderByTimestampDesc();
        return list.stream()
                .filter(d -> inRange(d.getTimestamp(), from, to))
                .sorted(Comparator.comparing(DatoProduttivo::getTimestamp))
                .collect(Collectors.toList());
    }

    private String buildPdfHtml(LocalDateTime from, LocalDateTime to,
            List<DatoAmbientale> amb,
            List<DatoProduttivo> prod) {

        StringBuilder sb = new StringBuilder(4096);
        sb.append("""
                    <!DOCTYPE html>
                    <html lang="it">
                    <head>
                      <meta charset="utf-8"/>
                      <style>
                        body { font-family: -apple-system, Segoe UI, Roboto, Arial, sans-serif; color:#111; }
                        h1 { font-size:20px; margin:0 0 4px 0; }
                        .muted { color:#666; font-size:12px; margin:0 0 12px 0; }
                        table { border-collapse: collapse; width:100%; margin:14px 0; }
                        th, td { border: 1px solid #ccc; padding: 6px 8px; font-size: 12px; }
                        th { background: #f2f2f2; }
                      </style>
                    </head>
                    <body>
                """);

        sb.append("<h1>Report dati – Settore primario</h1>");
        sb.append("<div class='muted'>Intervallo: ")
                .append(escape(fmtTs(from))).append(" → ").append(escape(fmtTs(to))).append("</div>");

        // Ambientali
        sb.append("<h2 style='font-size:16px;margin:8px 0'>Dati ambientali</h2>");
        sb.append("<table><thead><tr>")
                .append("<th>Timestamp</th><th>Temperatura (°C)</th><th>Umidità suolo (%)</th><th>Pioggia (mm)</th>")
                .append("</tr></thead><tbody>");
        for (var d : amb) {
            sb.append("<tr>")
                    .append("<td>").append(escape(fmtTs(d.getTimestamp()))).append("</td>")
                    .append("<td>").append(n(d.getTemperatura())).append("</td>")
                    .append("<td>").append(n(d.getUmiditaSuolo())).append("</td>")
                    .append("<td>").append(n(d.getPioggia())).append("</td>")
                    .append("</tr>");
        }
        sb.append("</tbody></table>");

        // Produttivi
        sb.append("<h2 style='font-size:16px;margin:8px 0'>Dati produttivi</h2>");
        sb.append("<table><thead><tr>")
                .append("<th>Timestamp</th><th>Resa</th><th>Crescita (%)</th><th>Acqua utilizzata</th>")
                .append("</tr></thead><tbody>");
        for (var p : prod) {
            sb.append("<tr>")
                    .append("<td>").append(escape(fmtTs(p.getTimestamp()))).append("</td>")
                    .append("<td>").append(n(p.getResa())).append("</td>")
                    .append("<td>").append(n(p.getCrescita())).append("</td>")
                    .append("<td>").append(n(p.getAcquaUtilizzata())).append("</td>")
                    .append("</tr>");
        }
        sb.append("</tbody></table>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private static String n(Number n) {
        return (n == null) ? "" : String.valueOf(n);
    }

    private static String quote(String s) {
        if (s == null)
            return "\"\"";
        String v = s.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }

    private static String escape(String s) {
        if (!StringUtils.hasText(s))
            return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static LocalDateTime parseOrDefaultToNow(String value) {
        if (StringUtils.hasText(value)) {
            return LocalDateTime.parse(value, FMT_FORM);
        }
        return LocalDateTime.now();
    }

    private static LocalDateTime parseOrDefaultFrom(LocalDateTime to, String from) {
        if (StringUtils.hasText(from)) {
            return LocalDateTime.parse(from, FMT_FORM);
        }
        return to.minusMinutes(60);
    }
}