package com.example.service;

import com.example.model.MovimientoProveedor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Servicio para manejar PayPal Payouts usando REST API directa
 */
@Service
public class PayPalPayoutService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Obtiene un token de acceso de PayPal
     */
    private String obtenerAccessToken() throws Exception {
        String url = mode.equals("sandbox") 
            ? "https://api-m.sandbox.paypal.com/v1/oauth2/token"
            : "https://api-m.paypal.com/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        
        throw new Exception("No se pudo obtener el token de acceso de PayPal");
    }

    /**
     * Envía un pago a un proveedor usando PayPal Payouts API REST
     */
    public String enviarPagoProveedor(MovimientoProveedor movimiento, String emailPayPal) throws Exception {
        try {
            String accessToken = obtenerAccessToken();
            String batchId = "PIXELSHOP-" + UUID.randomUUID().toString().substring(0, 8);
            
            String url = mode.equals("sandbox")
                ? "https://api-m.sandbox.paypal.com/v1/payments/payouts"
                : "https://api-m.paypal.com/v1/payments/payouts";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Construir el JSON del payout
            Map<String, Object> payoutRequest = new HashMap<>();
            
            Map<String, Object> senderBatchHeader = new HashMap<>();
            senderBatchHeader.put("sender_batch_id", batchId);
            senderBatchHeader.put("email_subject", "Has recibido un pago de PixelShop");
            senderBatchHeader.put("email_message", "Pago por la venta del juego: " + movimiento.getCompra().getJuego().getTitulo());
            payoutRequest.put("sender_batch_header", senderBatchHeader);

            Map<String, Object> item = new HashMap<>();
            item.put("recipient_type", "EMAIL");
            item.put("amount", Map.of("value", movimiento.getMontoNeto().toString(), "currency", "EUR"));
            item.put("receiver", emailPayPal);
            item.put("note", "Pago por venta en PixelShop");
            item.put("sender_item_id", "ITEM-" + movimiento.getId());

            payoutRequest.put("items", List.of(item));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payoutRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                Map<String, Object> batchHeader = (Map<String, Object>) response.getBody().get("batch_header");
                return (String) batchHeader.get("payout_batch_id");
            }
            
            throw new Exception("Error en la respuesta de PayPal: " + response.getStatusCode());
            
        } catch (Exception e) {
            System.err.println("Error al enviar pago a proveedor: " + e.getMessage());
            throw new Exception("Error al procesar el pago: " + e.getMessage());
        }
    }
}
