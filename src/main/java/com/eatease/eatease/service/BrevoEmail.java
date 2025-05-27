package com.eatease.eatease.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BrevoEmail {

    private static final String API_KEY;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        Properties props = new Properties();
        try (InputStream input = BrevoEmail.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Ficheiro application.properties não encontrado");
            }
            props.load(input);
            API_KEY = props.getProperty("brevo.api.key");
            if (API_KEY == null || API_KEY.isEmpty()) {
                throw new RuntimeException("brevo.api.key não definido em application.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar application.properties", e);
        }
    }

    public static void sendEmail(
            String fromEmail, String fromName,
            String toEmail, String toName,
            String subject, String htmlContent) throws Exception {

        ObjectNode payload = MAPPER.createObjectNode();
        ObjectNode sender = payload.putObject("sender");
        sender.put("name", fromName).put("email", fromEmail);

        ArrayNode toArray = payload.putArray("to");
        ObjectNode recipient = toArray.addObject();
        recipient.put("email", toEmail).put("name", toName);

        payload.put("subject", subject);
        payload.put("htmlContent", htmlContent);

        String requestBody = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("api-key", API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("✅ Email enviado com sucesso! Resposta: " + response.body());
        } else {
            System.err.println("❌ Falha ao enviar email. Código: "
                    + response.statusCode() + " — " + response.body());
        }
    }
}
