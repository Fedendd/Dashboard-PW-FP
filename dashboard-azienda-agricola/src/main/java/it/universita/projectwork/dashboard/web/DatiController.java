package it.universita.projectwork.dashboard.web;

import it.universita.projectwork.dashboard.dto.DatoDTO;
import it.universita.projectwork.dashboard.model.DatoAmbientale;
import it.universita.projectwork.dashboard.model.DatoProduttivo;
import it.universita.projectwork.dashboard.repo.DatoAmbientaleRepo;
import it.universita.projectwork.dashboard.repo.DatoProduttivoRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DatiController {

    private final DatoAmbientaleRepo ambientaleRepo;
    private final DatoProduttivoRepo produttivoRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public DatiController(DatoAmbientaleRepo ambientaleRepo,
            DatoProduttivoRepo produttivoRepo,
            SimpMessagingTemplate messagingTemplate) {
        this.ambientaleRepo = ambientaleRepo;
        this.produttivoRepo = produttivoRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/dati")
    public ResponseEntity<Void> riceviDati(@RequestBody DatoDTO dto) {
        if (dto.getTimestamp() == null) {
            dto.setTimestamp(Instant.now());
        }
        DatoAmbientale da = new DatoAmbientale(dto.getTimestamp(), dto.getTemperatura(), dto.getUmiditaSuolo(),
                dto.getPioggia());
        DatoProduttivo dp = new DatoProduttivo(dto.getTimestamp(), dto.getResa(), dto.getCrescita(),
                dto.getAcquaUtilizzata());
        ambientaleRepo.save(da);
        produttivoRepo.save(dp);

        messagingTemplate.convertAndSend("/topic/dati", dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dati")
    public List<DatoDTO> getDatiTra(@RequestParam("from") Instant from, @RequestParam("to") Instant to) {
        var amb = ambientaleRepo.findByTimestampBetweenOrderByTimestampAsc(from, to);
        var prod = produttivoRepo.findByTimestampBetweenOrderByTimestampAsc(from, to);

        int i = 0;
        int j = 0;
        var result = new java.util.ArrayList<DatoDTO>();
        while (i < amb.size() || j < prod.size()) {
            Instant ta = (i < amb.size()) ? amb.get(i).getTimestamp() : null;
            Instant tp = (j < prod.size()) ? prod.get(j).getTimestamp() : null;

            if (ta != null && (tp == null || ta.isBefore(tp) || ta.equals(tp))) {
                DatoDTO dto = new DatoDTO();
                dto.setTimestamp(amb.get(i).getTimestamp());
                dto.setTemperatura(amb.get(i).getTemperatura());
                dto.setUmiditaSuolo(amb.get(i).getUmiditaSuolo());
                dto.setPioggia(amb.get(i).getPioggia());
                if (tp != null && tp.equals(ta)) {
                    dto.setResa(prod.get(j).getResa());
                    dto.setCrescita(prod.get(j).getCrescita());
                    dto.setAcquaUtilizzata(prod.get(j).getAcquaUtilizzata());
                    j++;
                }
                result.add(dto);
                i++;
            } else {
                DatoDTO dto = new DatoDTO();
                dto.setTimestamp(prod.get(j).getTimestamp());
                dto.setResa(prod.get(j).getResa());
                dto.setCrescita(prod.get(j).getCrescita());
                dto.setAcquaUtilizzata(prod.get(j).getAcquaUtilizzata());
                result.add(dto);
                j++;
            }
        }
        return result;
    }
}
