package com.example.service;

import com.example.model.Compra;
import com.example.model.Juego;
import com.example.model.Usuario;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    @Autowired
    private PayPalHttpClient payPalHttpClient;

    /**
     * Crea una orden de pago en PayPal
     * 
     * @param juego El juego que se va a comprar
     * @param returnUrl URL de retorno tras pago exitoso
     * @param cancelUrl URL de retorno si se cancela
     * @return ID de la orden creada
     */
    public String crearOrden(Juego juego, String returnUrl, String cancelUrl) throws IOException {
        // Configurar detalles del monto con breakdown
        AmountBreakdown breakdown = new AmountBreakdown()
            .itemTotal(new Money().currencyCode("EUR").value(juego.getPrecio().toString()));
        
        AmountWithBreakdown amount = new AmountWithBreakdown()
            .currencyCode("EUR")
            .value(juego.getPrecio().toString())
            .amountBreakdown(breakdown);

        // Configurar item de compra
        Item item = new Item()
            .name(juego.getTitulo())
            .description(juego.getDescripcion() != null ? 
                juego.getDescripcion().substring(0, Math.min(127, juego.getDescripcion().length())) : 
                "Juego digital")
            .unitAmount(new Money().currencyCode("EUR").value(juego.getPrecio().toString()))
            .quantity("1")
            .category("DIGITAL_GOODS");

        // Lista de items
        List<Item> items = new ArrayList<>();
        items.add(item);

        // Configurar unidad de compra
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
            .referenceId(juego.getId().toString())
            .description("Compra de juego: " + juego.getTitulo())
            .amountWithBreakdown(amount)
            .items(items);

        // Configurar contexto de aplicación
        ApplicationContext applicationContext = new ApplicationContext()
            .returnUrl(returnUrl)
            .cancelUrl(cancelUrl)
            .brandName("PixelShop")
            .landingPage("BILLING")
            .shippingPreference("NO_SHIPPING")
            .userAction("PAY_NOW");

        // Crear request de orden
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        orderRequest.applicationContext(applicationContext);
        orderRequest.purchaseUnits(List.of(purchaseUnit));

        // Ejecutar request
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.requestBody(orderRequest);

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();
            
            return order.id();
            
        } catch (IOException e) {
            throw new IOException("Error al crear orden en PayPal: " + e.getMessage());
        }
    }

    /**
     * Captura el pago de una orden aprobada
     * 
     * @param orderId ID de la orden de PayPal
     * @return Información de la captura
     */
    public Order capturarPago(String orderId) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        
        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            return response.result();
            
        } catch (IOException e) {
            throw new IOException("Error al capturar pago en PayPal: " + e.getMessage());
        }
    }

    /**
     * Obtiene los detalles de una orden
     * 
     * @param orderId ID de la orden
     * @return Detalles de la orden
     */
    public Order obtenerDetallesOrden(String orderId) throws IOException {
        OrdersGetRequest request = new OrdersGetRequest(orderId);
        
        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            return response.result();
            
        } catch (IOException e) {
            throw new IOException("Error al obtener detalles de la orden: " + e.getMessage());
        }
    }

    /**
     * Valida si una orden fue completada exitosamente
     * 
     * @param order Orden de PayPal
     * @return true si el pago fue completado
     */
    public boolean esPaymentCompletado(Order order) {
        if (order == null) return false;
        
        String status = order.status();
        return "COMPLETED".equals(status);
    }

    /**
     * Extrae el monto pagado de una orden
     * 
     * @param order Orden de PayPal
     * @return Monto pagado en BigDecimal
     */
    public BigDecimal extraerMontoPagado(Order order) {
        if (order == null || order.purchaseUnits() == null || order.purchaseUnits().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
        if (purchaseUnit.amountWithBreakdown() != null) {
            String value = purchaseUnit.amountWithBreakdown().value();
            return new BigDecimal(value);
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Extrae el ID del juego de una orden
     * 
     * @param order Orden de PayPal
     * @return ID del juego
     */
    public Long extraerJuegoId(Order order) {
        if (order == null || order.purchaseUnits() == null || order.purchaseUnits().isEmpty()) {
            return null;
        }
        
        PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
        String referenceId = purchaseUnit.referenceId();
        
        try {
            return Long.parseLong(referenceId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
