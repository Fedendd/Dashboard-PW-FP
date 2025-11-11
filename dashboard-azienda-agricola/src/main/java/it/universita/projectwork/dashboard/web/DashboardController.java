package it.universita.projectwork.dashboard.web;

import it.universita.projectwork.dashboard.model.DatoAmbientale;
import it.universita.projectwork.dashboard.model.DatoProduttivo;
import it.universita.projectwork.dashboard.repo.DatoAmbientaleRepo;
import it.universita.projectwork.dashboard.repo.DatoProduttivoRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import it.universita.projectwork.dashboard.service.SoglieService;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
public class DashboardController {

        private static final int KPI_WINDOW = 20;
        private final DatoAmbientaleRepo ambientaleRepo;
        private final DatoProduttivoRepo produttivoRepo;
        private final SoglieService soglie;

        public DashboardController(DatoAmbientaleRepo ambientaleRepo,
                        DatoProduttivoRepo produttivoRepo,
                        SoglieService soglie) {
                this.ambientaleRepo = ambientaleRepo;
                this.produttivoRepo = produttivoRepo;
                this.soglie = soglie;
        }

        @GetMapping({ "/dashboard" })
        public String getDashboard(Model model) {

                List<DatoAmbientale> amb = ambientaleRepo.findTop50ByOrderByTimestampDesc();
                amb = amb.stream().sorted(Comparator.comparing(DatoAmbientale::getTimestamp)).toList();

                List<DatoProduttivo> prod = produttivoRepo.findTop50ByOrderByTimestampDesc();
                prod = prod.stream().sorted(Comparator.comparing(DatoProduttivo::getTimestamp)).toList();

                double totaleResa = prod.stream().map(p -> p.getResa() != null ? p.getResa() : 0.0)
                                .mapToDouble(Double::doubleValue).sum();
                double totaleAcqua = prod.stream()
                                .map(p -> p.getAcquaUtilizzata() != null ? p.getAcquaUtilizzata() : 0.0)
                                .mapToDouble(Double::doubleValue).sum();
                Double efficienzaIdrica = (totaleAcqua > 0) ? (totaleResa / totaleAcqua) : null;

                String effStr = (efficienzaIdrica != null)
                                ? String.format(Locale.US, "%.2f", efficienzaIdrica)
                                : null;
                model.addAttribute("effStr", effStr);

                var labelsAmb = amb.stream().map(a -> a.getTimestamp().toString()).toList();
                var tempList = amb.stream().map(DatoAmbientale::getTemperatura).toList();
                var umidList = amb.stream().map(DatoAmbientale::getUmiditaSuolo).toList();
                var pioggiaList = amb.stream().map(DatoAmbientale::getPioggia).toList();

                var labelsProd = prod.stream().map(p -> p.getTimestamp().toString()).toList();
                var resaList = prod.stream().map(DatoProduttivo::getResa).toList();

                var acquaList = prod.stream()
                                .map(DatoProduttivo::getAcquaUtilizzata)
                                .map(Double::doubleValue)
                                .toList();
                var crescitaList = prod.stream()
                                .map(DatoProduttivo::getCrescita)
                                .map(Double::doubleValue)
                                .toList();

                double wue = 0.0;
                if (!prod.isEmpty()) {
                        int n = Math.min(prod.size(), KPI_WINDOW);
                        var last = prod.subList(prod.size() - n, prod.size());
                        double sumResa = last.stream().map(DatoProduttivo::getResa)
                                        .mapToDouble(Double::doubleValue).sum();
                        double sumAcqua = last.stream().map(DatoProduttivo::getAcquaUtilizzata)
                                        .mapToDouble(Double::doubleValue).sum();
                        wue = (sumAcqua > 0) ? sumResa / sumAcqua : 0.0;
                }

                int trend = 0;
                {
                        int n = Math.min(crescitaList.size(), KPI_WINDOW);
                        if (n >= 6) {
                                var last = crescitaList.subList(crescitaList.size() - n, crescitaList.size());
                                int half = n / 2;
                                double m1 = last.subList(0, half).stream().mapToDouble(Double::doubleValue).average()
                                                .orElse(0);
                                double m2 = last.subList(half, n).stream().mapToDouble(Double::doubleValue).average()
                                                .orElse(0);
                                double delta = m2 - m1;
                                trend = (delta > 0.02) ? 1 : (delta < -0.02 ? -1 : 0);
                        }
                }
                model.addAttribute("acquaList", acquaList);
                model.addAttribute("crescitaList", crescitaList);
                model.addAttribute("wue", wue);
                model.addAttribute("trendCrescita", trend);

                model.addAttribute("labelsAmb", labelsAmb);
                model.addAttribute("tempList", tempList);
                model.addAttribute("umidList", umidList);
                model.addAttribute("pioggiaList", pioggiaList);
                model.addAttribute("labelsProd", labelsProd);
                model.addAttribute("resaList", resaList);
                model.addAttribute("efficienzaIdrica", efficienzaIdrica);
                model.addAttribute("sogliaCaldo", soglie.getCaldo());
                model.addAttribute("sogliaSuoloSecco", soglie.getSuoloSecco());
                model.addAttribute("sogliaPioggia", soglie.getPioggia());
                model.addAttribute("sogliaEfficienza", soglie.getEfficienza());

                return "dashboard";
        }
}