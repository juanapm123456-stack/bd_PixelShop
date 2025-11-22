package com.example.service;

import com.example.model.Compra;
import com.example.model.Usuario;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private Resend resendClient;

    @Value("${email.from:noreply@pixelshop.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Envía email de bienvenida al registrarse
     */
    public void enviarEmailBienvenida(Usuario usuario) {
        try {
            System.out.println("🔧 Construyendo email de bienvenida para: " + usuario.getEmail());
            String htmlContent = construirEmailBienvenida(usuario);
            
            System.out.println("📤 Enviando a Resend API...");
            System.out.println("   From: " + fromEmail);
            System.out.println("   To: " + usuario.getEmail());
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(usuario.getEmail())
                .subject("¡Bienvenido a PixelShop! 🎮")
                .html(htmlContent)
                .build();
            
            CreateEmailResponse response = resendClient.emails().send(params);
            System.out.println("✅ Email de bienvenida enviado. ID: " + response.getId());
            
        } catch (ResendException e) {
            System.err.println("❌ Error al enviar email de bienvenida: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al enviar email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía confirmación de compra con detalles del pedido
     */
    public void enviarConfirmacionCompra(Compra compra) {
        try {
            String htmlContent = construirEmailConfirmacionCompra(compra);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(compra.getUsuario().getEmail())
                .subject("✅ Confirmación de compra - " + compra.getJuego().getTitulo())
                .html(htmlContent)
                .build();
            
            CreateEmailResponse response = resendClient.emails().send(params);
            System.out.println("✅ Email de confirmación enviado. ID: " + response.getId());
            
        } catch (ResendException e) {
            System.err.println("❌ Error al enviar confirmación de compra: " + e.getMessage());
        }
    }

    /**
     * Envía email de recuperación de contraseña
     */
    public void enviarRecuperacionPassword(String email, String nombreUsuario, String token) {
        try {
            String enlaceRecuperacion = "http://localhost:8080/auth/reset-password?token=" + token;
            String htmlContent = construirEmailRecuperacionPassword(nombreUsuario, enlaceRecuperacion);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(email)
                .subject("🔒 Recuperación de contraseña - PixelShop")
                .html(htmlContent)
                .build();
            
            CreateEmailResponse response = resendClient.emails().send(params);
            System.out.println("✅ Email de recuperación enviado. ID: " + response.getId());
            
        } catch (ResendException e) {
            System.err.println("❌ Error al enviar email de recuperación: " + e.getMessage());
        }
    }

    /**
     * Envía notificación de envío con número de seguimiento
     */
    public void enviarNotificacionEnvio(Compra compra, String numeroSeguimiento) {
        try {
            String htmlContent = construirEmailNotificacionEnvio(compra, numeroSeguimiento);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(compra.getUsuario().getEmail())
                .subject("🚚 ¡Tu juego está en camino! - " + compra.getJuego().getTitulo())
                .html(htmlContent)
                .build();
            
            CreateEmailResponse response = resendClient.emails().send(params);
            System.out.println("✅ Email de notificación de envío enviado. ID: " + response.getId());
            
        } catch (ResendException e) {
            System.err.println("❌ Error al enviar notificación de envío: " + e.getMessage());
        }
    }

    // ==================== PLANTILLAS HTML ====================

    private String construirEmailBienvenida(Usuario usuario) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 12px;
                        padding: 40px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                    }
                    h1 {
                        color: #667eea;
                        text-align: center;
                        margin-bottom: 20px;
                    }
                    p {
                        color: #333;
                        line-height: 1.8;
                        font-size: 16px;
                    }
                    .highlight {
                        color: #667eea;
                        font-weight: bold;
                    }
                    .btn {
                        display: inline-block;
                        padding: 14px 32px;
                        background: linear-gradient(135deg, #667eea, #764ba2);
                        color: white;
                        text-decoration: none;
                        border-radius: 8px;
                        margin-top: 20px;
                        font-weight: bold;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                        border-top: 1px solid #eee;
                        padding-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎮 ¡Bienvenido a PixelShop!</h1>
                    <p>Hola <span class="highlight">%s</span>,</p>
                    <p>¡Gracias por unirte a <strong>PixelShop</strong>! Tu cuenta ha sido creada exitosamente.</p>
                    <p>Ahora puedes:</p>
                    <ul>
                        <li>✨ Explorar nuestro catálogo de juegos</li>
                        <li>🛒 Comprar tus juegos favoritos</li>
                        <li>📚 Gestionar tu biblioteca personal</li>
                        <li>💳 Realizar pagos seguros con PayPal</li>
                    </ul>
                    <center>
                        <a href="http://localhost:8080/catalogo" class="btn">Explorar Catálogo</a>
                    </center>
                    <div class="footer">
                        <p>© 2025 PixelShop. Todos los derechos reservados.</p>
                        <p>Este es un correo automático, por favor no responder.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(usuario.getNombre());
    }

    private String construirEmailConfirmacionCompra(Compra compra) {
        String fecha = compra.getFechaCompra().format(DATE_FORMATTER);
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%);
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 12px;
                        padding: 40px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                    }
                    h1 {
                        color: #11998e;
                        text-align: center;
                        margin-bottom: 10px;
                    }
                    .success-icon {
                        text-align: center;
                        font-size: 60px;
                        margin-bottom: 20px;
                    }
                    .order-box {
                        background: #f8f9fa;
                        border-left: 4px solid #11998e;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 8px;
                    }
                    .order-item {
                        display: flex;
                        justify-content: space-between;
                        margin: 10px 0;
                        padding: 8px 0;
                        border-bottom: 1px solid #dee2e6;
                    }
                    .order-item:last-child {
                        border-bottom: none;
                    }
                    .total {
                        font-size: 28px;
                        color: #11998e;
                        font-weight: bold;
                        text-align: center;
                        margin-top: 20px;
                    }
                    .btn {
                        display: inline-block;
                        padding: 14px 32px;
                        background: linear-gradient(135deg, #11998e, #38ef7d);
                        color: white;
                        text-decoration: none;
                        border-radius: 8px;
                        margin-top: 20px;
                        font-weight: bold;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                        border-top: 1px solid #eee;
                        padding-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✅</div>
                    <h1>¡Compra Confirmada!</h1>
                    <p style="text-align: center; color: #666;">Gracias por tu compra, <strong>%s</strong></p>
                    
                    <div class="order-box">
                        <h3 style="margin-top: 0; color: #11998e;">📦 Detalles del Pedido</h3>
                        <div class="order-item">
                            <span><strong>Juego:</strong></span>
                            <span>%s</span>
                        </div>
                        <div class="order-item">
                            <span><strong>Fecha:</strong></span>
                            <span>%s</span>
                        </div>
                    </div>
                    
                    <div class="total">Total pagado: $%.2f</div>
                    
                    <p style="text-align: center; margin-top: 20px;">
                        Ya puedes acceder al juego desde tu biblioteca personal.
                    </p>
                    
                    <center>
                        <a href="http://localhost:8080/usuario/biblioteca" class="btn">Ver Mi Biblioteca</a>
                    </center>
                    
                    <div class="footer">
                        <p>© 2025 PixelShop. Todos los derechos reservados.</p>
                        <p>Este es un correo automático, por favor no responder.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                compra.getUsuario().getNombre(),
                compra.getJuego().getTitulo(),
                fecha,
                compra.getPrecioPagado()
            );
    }

    private String construirEmailRecuperacionPassword(String nombreUsuario, String enlaceRecuperacion) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%);
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 12px;
                        padding: 40px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                    }
                    h1 {
                        color: #f5576c;
                        text-align: center;
                    }
                    .warning-box {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 8px;
                    }
                    .btn {
                        display: inline-block;
                        padding: 14px 32px;
                        background: linear-gradient(135deg, #f093fb, #f5576c);
                        color: white;
                        text-decoration: none;
                        border-radius: 8px;
                        margin-top: 20px;
                        font-weight: bold;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                        border-top: 1px solid #eee;
                        padding-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🔒 Recuperación de Contraseña</h1>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta en PixelShop.</p>
                    
                    <div class="warning-box">
                        <p><strong>⚠️ Importante:</strong></p>
                        <ul style="margin: 10px 0;">
                            <li>Este enlace expira en <strong>1 hora</strong></li>
                            <li>Solo se puede usar una vez</li>
                        </ul>
                    </div>
                    
                    <center>
                        <a href="%s" class="btn">Restablecer Contraseña</a>
                    </center>
                    
                    <p style="margin-top: 30px; color: #666; font-size: 14px;">
                        Si no solicitaste este cambio, puedes ignorar este correo. Tu contraseña permanecerá sin cambios.
                    </p>
                    
                    <div class="footer">
                        <p>© 2025 PixelShop. Todos los derechos reservados.</p>
                        <p>Este es un correo automático, por favor no responder.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(nombreUsuario, enlaceRecuperacion);
    }

    private String construirEmailNotificacionEnvio(Compra compra, String numeroSeguimiento) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #4facfe 0%%, #00f2fe 100%%);
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 12px;
                        padding: 40px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                    }
                    h1 {
                        color: #4facfe;
                        text-align: center;
                    }
                    .tracking-box {
                        background: #e3f2fd;
                        padding: 25px;
                        border-radius: 12px;
                        margin: 25px 0;
                        text-align: center;
                        border: 2px dashed #4facfe;
                    }
                    .tracking-number {
                        font-size: 24px;
                        font-weight: bold;
                        color: #1976d2;
                        letter-spacing: 2px;
                        margin-top: 10px;
                    }
                    .info-box {
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 8px;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                        border-top: 1px solid #eee;
                        padding-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🚚 ¡Tu Juego Está en Camino!</h1>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>¡Buenas noticias! Tu pedido ha sido enviado y está en camino.</p>
                    
                    <div class="info-box">
                        <p><strong>📦 Juego:</strong> %s</p>
                    </div>
                    
                    <div class="tracking-box">
                        <p style="margin: 0; color: #666;">Número de seguimiento:</p>
                        <p class="tracking-number">%s</p>
                    </div>
                    
                    <p style="text-align: center; color: #666;">
                        Puedes usar este número para rastrear tu pedido en cualquier momento.
                    </p>
                    
                    <div class="footer">
                        <p>© 2025 PixelShop. Todos los derechos reservados.</p>
                        <p>Este es un correo automático, por favor no responder.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                compra.getUsuario().getNombre(),
                compra.getJuego().getTitulo(),
                numeroSeguimiento
            );
    }
}
