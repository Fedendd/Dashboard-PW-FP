package it.universita.projectwork.dashboard.web;

import it.universita.projectwork.dashboard.service.SoglieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/soglie")
public class SoglieController {

    private final SoglieService soglie;

    public SoglieController(SoglieService soglie) {
        this.soglie = soglie;
    }

    @GetMapping
    public Map<String, Object> get() {
        return Map.of(
                "caldo", soglie.getCaldo(),
                "suoloSecco", soglie.getSuoloSecco(),
                "pioggia", soglie.getPioggia(),
                "efficienza", soglie.getEfficienza());
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody Map<String, Double> body) {
        Double caldo = body.get("caldo");
        Double suoloSecco = body.get("suoloSecco");
        Double pioggia = body.get("pioggia");
        Double efficienza = body.get("efficienza");

        // Validazioni base
        if (caldo != null && (caldo < -20 || caldo > 60))
            return ResponseEntity.badRequest().body("Soglia caldo fuori range");
        if (suoloSecco != null && (suoloSecco < 0 || suoloSecco > 100))
            return ResponseEntity.badRequest().body("Soglia suoloSecco fuori range");
        if (pioggia != null && (pioggia < 0 || pioggia > 200))
            return ResponseEntity.badRequest().body("Soglia pioggia fuori range");
        if (efficienza != null && (efficienza < 0 || efficienza > 2))
            return ResponseEntity.badRequest().body("Soglia efficienza fuori range");

        soglie.update(caldo, suoloSecco, pioggia, efficienza);
        return ResponseEntity.ok(get());
    }
}