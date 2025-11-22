package com.example.controller;

import com.example.model.Compra;
import com.example.model.Juego;
import com.example.model.Usuario;
import com.example.service.EmailService;
import com.example.service.PayPalService;
import com.example.repository.CompraRepository;
import com.example.repository.JuegoRepository;
import com.example.repository.UsuarioRepository;
import com.paypal.orders.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/paypal")
public class PayPalController extends BaseController {

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private JuegoRepository juegoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Muestra la página de checkout con opciones de pago
     */
    @GetMapping("/checkout/{juegoId}")
    public String checkout(@PathVariable Long juegoId, 
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Juego juego = juegoRepository.findById(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

        // Verificar que no lo tenga ya
        if (compraRepository.existsByUsuarioAndJuego(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/juego/" + juegoId;
        }

        model.addAttribute("juego", juego);
        model.addAttribute("usuario", usuario);
        model.addAttribute("paypalClientId", System.getenv("PAYPAL_CLIENT_ID"));

        return "checkout/payment-options";
    }

    /**
     * Crea una orden de pago con PayPal
     */
    @PostMapping("/create-order/{juegoId}")
    @ResponseBody
    public String createOrder(@PathVariable Long juegoId) {
        try {
            System.out.println("=== Creando orden PayPal para juego ID: " + juegoId);
            
            Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

            System.out.println("Juego encontrado: " + juego.getTitulo() + " - Precio: " + juego.getPrecio());
            
            String returnUrl = "http://localhost:8080/paypal/success?juegoId=" + juegoId;
            String cancelUrl = "http://localhost:8080/paypal/cancel?juegoId=" + juegoId;

            String orderId = payPalService.crearOrden(juego, returnUrl, cancelUrl);
            
            System.out.println("Orden PayPal creada exitosamente: " + orderId);
            return orderId;

        } catch (Exception e) {
            System.err.println("ERROR al crear orden de PayPal: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al crear orden de PayPal: " + e.getMessage());
        }
    }

    /**
     * Maneja el retorno exitoso desde PayPal
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String token,
                                @RequestParam Long juegoId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Capturar el pago
            Order order = payPalService.capturarPago(token);

            if (payPalService.esPaymentCompletado(order)) {
                // Obtener datos
                String email = obtenerEmailDelUsuario(authentication);
                Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                Juego juego = juegoRepository.findById(juegoId)
                    .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

                BigDecimal montoPagado = payPalService.extraerMontoPagado(order);

                // Crear compra (el resto del proceso se maneja en CompraService)
                // Por simplicidad, redirigimos al proceso de compra normal
                redirectAttributes.addFlashAttribute("paypalOrderId", order.id());
                redirectAttributes.addFlashAttribute("montoPagado", montoPagado);
                
                return "redirect:/compra/confirmar/" + juegoId;
                
            } else {
                redirectAttributes.addFlashAttribute("error", "El pago no se completó correctamente");
                return "redirect:/paypal/checkout/" + juegoId;
            }

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/paypal/checkout/" + juegoId;
        }
    }

    /**
     * Maneja la cancelación del pago
     */
    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam Long juegoId,
                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("warning", "Has cancelado el pago");
        return "redirect:/juego/" + juegoId;
    }

    /**
     * Simula pago con tarjeta (solo para desarrollo)
     */
    @PostMapping("/card-payment/{juegoId}")
    public String cardPayment(@PathVariable Long juegoId,
                             @RequestParam String cardNumber,
                             @RequestParam String cardName,
                             @RequestParam String expiryDate,
                             @RequestParam String cvv,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        
        // Obtener email del usuario autenticado (aunque no se usa en esta simulación)
        String email = obtenerEmailDelUsuario(authentication);
        
        // Simulación de diferentes escenarios según el número de tarjeta
        String lastDigits = cardNumber.substring(cardNumber.length() - 4);

        switch (lastDigits) {
            case "1111": // Tarjeta rechazada
                redirectAttributes.addFlashAttribute("error", "❌ Tarjeta rechazada. Por favor, usa otro método de pago.");
                return "redirect:/paypal/checkout/" + juegoId;

            case "2222": // Tarjeta caducada
                redirectAttributes.addFlashAttribute("error", "❌ Tarjeta caducada. Verifica la fecha de expiración.");
                return "redirect:/paypal/checkout/" + juegoId;

            case "3333": // Fondos insuficientes
                redirectAttributes.addFlashAttribute("error", "❌ Fondos insuficientes. No se pudo completar la transacción.");
                return "redirect:/paypal/checkout/" + juegoId;

            default: // Pago exitoso
                redirectAttributes.addFlashAttribute("success", "✅ Pago procesado exitosamente con tarjeta");
                return "redirect:/compra/confirmar/" + juegoId;
        }
    }
}
