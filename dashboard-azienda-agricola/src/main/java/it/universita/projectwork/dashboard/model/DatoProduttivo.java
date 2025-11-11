package it.universita.projectwork.dashboard.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "DATI_PRODUTTIVI")
public class DatoProduttivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column
    private Double resa;

    @Column
    private Double crescita;

    @Column
    private Double acquaUtilizzata;

    public DatoProduttivo() {
    }

    public DatoProduttivo(Instant timestamp, Double resa, Double crescita, Double acquaUtilizzata) {
        this.timestamp = timestamp;
        this.resa = resa;
        this.crescita = crescita;
        this.acquaUtilizzata = acquaUtilizzata;
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

    public Double getResa() {
        return resa;
    }

    public void setResa(Double resa) {
        this.resa = resa;
    }

    public Double getCrescita() {
        return crescita;
    }

    public void setCrescita(Double crescita) {
        this.crescita = crescita;
    }

    public Double getAcquaUtilizzata() {
        return acquaUtilizzata;
    }

    public void setAcquaUtilizzata(Double acquaUtilizzata) {
        this.acquaUtilizzata = acquaUtilizzata;
    }
}
