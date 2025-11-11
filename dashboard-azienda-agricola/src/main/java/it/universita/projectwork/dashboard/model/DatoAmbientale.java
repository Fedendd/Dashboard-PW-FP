package it.universita.projectwork.dashboard.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "DATI_AMBIENTALI")
public class DatoAmbientale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column
    private Double temperatura;

    @Column
    private Double umiditaSuolo;

    @Column
    private Double pioggia;

    public DatoAmbientale() {
    }

    public DatoAmbientale(Instant timestamp, Double temperatura, Double umiditaSuolo, Double pioggia) {
        this.timestamp = timestamp;
        this.temperatura = temperatura;
        this.umiditaSuolo = umiditaSuolo;
        this.pioggia = pioggia;
    }

    public Long getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getUmiditaSuolo() {
        return umiditaSuolo;
    }

    public void setUmiditaSuolo(Double umiditaSuolo) {
        this.umiditaSuolo = umiditaSuolo;
    }

    public Double getPioggia() {
        return pioggia;
    }

    public void setPioggia(Double pioggia) {
        this.pioggia = pioggia;
    }
}
