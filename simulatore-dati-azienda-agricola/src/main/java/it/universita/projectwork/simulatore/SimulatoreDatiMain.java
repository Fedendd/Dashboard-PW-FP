package it.universita.projectwork.simulatore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Random;
import java.util.Locale;

public class SimulatoreDatiMain {

    public static void main(String[] args) throws Exception {
        String endpoint = System.getenv().getOrDefault("ENDPOINT_SERVER", "http://localhost:8080/api/dati");
        long intervalloMs = Long.parseLong(System.getenv().getOrDefault("INTERVALLO_MS", "5000"));
        System.out.println("Simulatore avviato. Endpoint: " + endpoint + " | Intervallo: " + intervalloMs + " ms");

        HttpClient client = HttpClient.newHttpClient();
        Random random = new Random();
        while (true) {
            double temperatura = 15 + random.nextDouble() * 15;
            double umidita = 20 + random.nextDouble() * 60;
            double pioggia = random.nextDouble() < 0.1 ? random.nextDouble() * 5.0 : 0.0;
            double resa = 50 + random.nextDouble() * 10;
            double crescita = 0.5 + random.nextDouble() * 0.5;
            double acqua = 100 + random.nextDouble() * 50;

            String payload = String.format(
                    Locale.US,
                    "{\"timestamp\":\"%s\",\"temperatura\":%.2f,\"umiditaSuolo\":%.2f,\"pioggia\":%.2f,\"resa\":%.2f,\"crescita\":%.2f,\"acquaUtilizzata\":%.2f}",
                    Instant.now().toString(), temperatura, umidita, pioggia, resa, crescita, acqua);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                System.out.println("Inviato: " + payload + " | Risposta: " + res.statusCode());
            } catch (Exception e) {
                System.err.println("Errore invio dati: " + e.getMessage());
            }
            Thread.sleep(intervalloMs);
        }
    }
}
