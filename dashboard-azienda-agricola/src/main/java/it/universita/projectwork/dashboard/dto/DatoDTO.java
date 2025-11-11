package it.universita.projectwork.dashboard.dto;

import java.time.Instant;

public class DatoDTO {
    private Instant timestamp;
    private Double temperatura;
    private Double umiditaSuolo;
    private Double pioggia;
    private Double resa;
    private Double crescita;
    private Double acquaUtilizzata;

    public DatoDTO() {
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
