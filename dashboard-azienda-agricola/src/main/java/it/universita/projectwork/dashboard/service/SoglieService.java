package it.universita.projectwork.dashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SoglieService {
    private volatile double caldo;
    private volatile double suoloSecco;
    private volatile double pioggia;
    private volatile double efficienza;

    public SoglieService(
            @Value("${soglie.caldo:35}") double caldo,
            @Value("${soglie.suoloSecco:20}") double suoloSecco,
            @Value("${soglie.pioggia:5}") double pioggia,
            @Value("${soglie.efficienza:0.30}") double efficienza) {
        this.caldo = caldo;
        this.suoloSecco = suoloSecco;
        this.pioggia = pioggia;
        this.efficienza = efficienza;
    }

    public double getCaldo() {
        return caldo;
    }

    public double getSuoloSecco() {
        return suoloSecco;
    }

    public double getPioggia() {
        return pioggia;
    }

    public double getEfficienza() {
        return efficienza;
    }

    public synchronized void update(Double caldo, Double suoloSecco, Double pioggia, Double efficienza) {
        if (caldo != null)
            this.caldo = caldo;
        if (suoloSecco != null)
            this.suoloSecco = suoloSecco;
        if (pioggia != null)
            this.pioggia = pioggia;
        if (efficienza != null)
            this.efficienza = efficienza;
    }
}