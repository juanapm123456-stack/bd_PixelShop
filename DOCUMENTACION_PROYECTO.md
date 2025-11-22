# 📚 Documentación del Proyecto PixelShop

## 🎯 Descripción General

**PixelShop** es una plataforma de marketplace digital para la compra y venta de videojuegos, donde los proveedores pueden publicar sus juegos y los clientes pueden comprarlos. La plataforma actúa como intermediario, gestionando las transacciones, comisiones y pagos entre todas las partes.

---

## 👥 Roles de Usuario

### 1. **CLIENTE**
- Usuario estándar que puede comprar juegos.
- Puede registrarse mediante formulario tradicional o Google OAuth2.
- Tiene acceso a su biblioteca personal de juegos comprados.
- Puede ver su historial de compras.
- Puede gestionar su perfil y eliminar su cuenta.

### 2. **PROVEEDOR**
- Usuario que publica y vende juegos en la plataforma.
- Tiene todas las funcionalidades de un CLIENTE (puede comprar juegos).
- Puede publicar juegos pagando una tarifa de **25€ por juego**.
- Recibe el **85% del precio de venta** (la plataforma se queda con 15%).
- Puede ver sus ingresos pendientes y cobrados.
- Puede solicitar cobros mediante PayPal o transferencia bancaria.
- Puede editar la información de sus juegos publicados.

### 3. **ADMIN**
- Tiene acceso completo a la administración de la plataforma.
- **NO puede comprar juegos** (restricción de negocio).
- Puede ver todos los usuarios registrados.
- Puede ver todos los movimientos de dinero de la plataforma.
- Puede ver las ganancias totales de la plataforma (comisión del 15%).

---

## 💰 Modelo de Negocio

### Comisiones y Tarifas

1. **Tarifa de Publicación**: 
   - Cada proveedor debe pagar **25€** para publicar un juego nuevo.
   - Este pago se realiza mediante PayPal Checkout.
   - El juego solo aparece en el catálogo después del pago exitoso.

2. **Comisión por Venta**:
   - La plataforma se queda con el **15%** de cada venta.
   - El proveedor recibe el **85%** del precio de venta.
   - Ejemplo: Si un juego cuesta 50€, el proveedor recibe 42.50€ y la plataforma 7.50€.

### Flujo de Dinero

#### Compra de un Juego:
1. Cliente selecciona un juego del catálogo.
2. Cliente hace clic en "Comprar" (50€).
3. Se redirige a PayPal Checkout (sandbox).
4. Cliente completa el pago en PayPal.
5. PayPal redirige a `/compra/confirmar`.
6. Sistema confirma el pago y crea:
   - **Compra**: Registro de la transacción.
   - **BibliotecaUsuario**: Añade el juego a la biblioteca del cliente.
   - **MovimientoProveedor**: Registra 42.50€ pendientes para el proveedor (PENDIENTE).
   - **Email de confirmación**: Se envía al cliente (si funciona Resend).
7. El juego aparece en "Mi Biblioteca" del cliente.

#### Cobro del Proveedor:
1. Proveedor accede a "Mis Ventas".
2. Ve sus ingresos pendientes (85% de las ventas).
3. Hace clic en "Cobrar".
4. Elige método de pago:
   
   **Opción A: PayPal**
   - Ingresa su email de PayPal (se guarda en `emailPayPal`).
   - Sistema ejecuta payout inmediato vía PayPal REST API.
   - El dinero llega a su cuenta PayPal en minutos.
   - Estado del movimiento cambia a COBRADO.
   
   **Opción B: Tarjeta Bancaria/Transferencia**
   - Ingresa número de cuenta (IBAN) y titular.
   - Sistema guarda los datos pero NO envía el dinero.
   - Estado del movimiento cambia a COBRADO.
   - Se muestra mensaje: "Se procesará en 3-5 días hábiles".
   - **(Manual): El admin debe hacer la transferencia fuera del sistema.**

---

## 🛒 Reglas de Compra

### Restricciones Importantes:

1. **Un Juego por Usuario**:
   - Un cliente **NO puede comprar el mismo juego dos veces**.
   - Si un juego ya está en la biblioteca del usuario, el botón de compra no aparece.
   - Validación en backend: `bibliotecaRepository.existsByUsuarioAndJuego()`.

2. **Sin Carrito de Compra**:
   - NO existe carrito de compras.
   - Cada compra es individual (1 juego a la vez).
   - Cada compra tiene su propio flujo de PayPal independiente.

3. **Admin No Puede Comprar**:
   - Validación en `SecurityConfig`: `requestMatchers("/compra/**").hasAnyRole("CLIENTE", "PROVEEDOR")`.
   - El rol ADMIN está excluido explícitamente de las compras.

4. **Proveedor SÍ Puede Comprar**:
   - Un proveedor puede comprar juegos de otros proveedores.
   - Un proveedor **NO puede comprar sus propios juegos** (validación en controller).

---

## 🔐 Autenticación

### Métodos de Registro/Login:

#### 1. **Formulario Tradicional** (`/register`):
- Usuario completa: nombre, email, contraseña.
- Contraseña se encripta con BCrypt.
- Se asigna rol CLIENTE o PROVEEDOR (seleccionable).
- Se envía email de bienvenida (si Resend funciona).

#### 2. **Google OAuth2** ("Continuar con Google"):
- Usuario autoriza acceso a su cuenta de Google.
- Sistema extrae: email, nombre, foto de perfil.
- Se genera una contraseña aleatoria (UUID) encriptada.
- El usuario **NO conoce esta contraseña** (solo puede entrar con Google).
- Se asigna rol CLIENTE por defecto.
- Se envía email de bienvenida (si Resend funciona).

### Datos de Sesión:
- Spring Security gestiona las sesiones.
- El usuario autenticado se obtiene con `@AuthenticationPrincipal`.
- Los permisos se validan por roles (ROLE_CLIENTE, ROLE_PROVEEDOR, ROLE_ADMIN).

---

## 💳 Integración con PayPal

### Sandbox (Desarrollo):
- **Modo**: `sandbox` (configurado en `application.properties`).
- **Client ID**: `AUJoWELN6zFo6AcAlcpHDjl2r-uXpjuIqLOK-5jCKJsYcIwRQwvx2WRzXS66p_7yFIyQUixHn0ZvNNEv`
- **Client Secret**: `EHDn8X6Wmy0zPkwXpTzHlJ5SN0_jWmQiM1qFgTjvnCv7dJdOPxEPOBRXfaU-7NZpYoCKb4E06mPBvV-i`
- **Ambiente de pruebas**: https://sandbox.paypal.com

### PayPal Checkout SDK (Compras):
- Se usa para que los **clientes paguen** la compra de juegos y la tarifa de publicación.
- Flujo:
  1. Frontend llama a `/paypal/create-payment`.
  2. Backend crea la orden con `OrdersCreateRequest`.
  3. Se redirige al cliente a PayPal para aprobar el pago.
  4. Cliente paga y regresa a `/compra/confirmar`.
  5. Backend captura el pago con `OrdersCaptureRequest`.

### PayPal REST API (Payouts):
- Se usa para que la plataforma **pague a los proveedores**.
- Flujo:
  1. Proveedor hace clic en "Cobrar" y elige PayPal.
  2. Backend llama a `PayPalPayoutService.enviarPago()`.
  3. Se construye un `CreatePayoutRequest` con:
     - Monto: Ingresos pendientes del proveedor.
     - Destinatario: Email de PayPal del proveedor.
     - Referencia: ID del movimiento.
  4. Se envía la solicitud a PayPal API.
  5. PayPal procesa el payout y transfiere el dinero.

### Requisitos para Producción:
- ⚠️ **PayPal Payouts requiere una cuenta Business aprobada**.
- ⚠️ Debes cambiar `paypal.mode=live` y usar credenciales de producción.

---

## 📧 Sistema de Emails (Resend)

### Configuración:
- **API Key**: `re_bwbKzPEJ_Joc6W4xS2jdBCyZN5S3MPN8g`
- **From Email**: `noreply@pixelshop.com`
- **Problema actual**: Los emails NO se están enviando (dominio sin verificar).

### Tipos de Email:

1. **Email de Bienvenida** (`enviarEmailBienvenida`):
   - Se envía al registrarse (formulario o Google OAuth2).
   - Incluye: saludo, beneficios de la plataforma, botón "Explorar Catálogo".

2. **Email de Confirmación de Compra** (`enviarConfirmacionCompra`):
   - Se envía después de una compra exitosa.
   - Incluye: título del juego, fecha, precio pagado, botón "Ver Mi Biblioteca".

3. **Email de Recuperación de Contraseña** (`enviarRecuperacionPassword`):
   - Se envía cuando el usuario olvida su contraseña.
   - Incluye: enlace con token temporal (expira en 1 hora).

4. **Email de Notificación de Envío** (`enviarNotificacionEnvio`):
   - Se envía cuando un juego físico es enviado (no implementado en este proyecto digital).

### Problema Actual:
- Resend requiere que el dominio `pixelshop.com` esté verificado.
- Solución temporal: Usar el dominio de pruebas de Resend o un dominio personal verificado.

---

## 📁 Base de Datos

### Configuración:
- **Motor**: MySQL 8.0.42 (Azure)
- **Host**: `mysqldemojuan.mysql.database.azure.com:3306`
- **Base de datos**: `bd_PixelShop`
- **Usuario**: `adminuser`
- **Contraseña**: `Poppy@azure`
- **DDL**: `spring.jpa.hibernate.ddl-auto=update` (crea/actualiza tablas automáticamente)

### Entidades Principales:

#### `Usuario`:
- **Campos**: id, nombre, email, password, rol, datosFiscales, emailPayPal, fechaRegistro, activo
- **Roles**: CLIENTE, PROVEEDOR, ADMIN
- **Relaciones**: 
  - `compras` (OneToMany con Compra)
  - `juegosPublicados` (OneToMany con Juego)
  - `movimientos` (OneToMany con MovimientoProveedor)

#### `Juego`:
- **Campos**: id, titulo, descripcion, precio, genero, imagenUrl, fechaPublicacion, activo, proveedor
- **Relaciones**:
  - `proveedor` (ManyToOne con Usuario)
  - `compras` (OneToMany con Compra)
  - `bibliotecas` (OneToMany con BibliotecaUsuario)

#### `Compra`:
- **Campos**: id, usuario, juego, fechaCompra, precioPagado
- **Relaciones**:
  - `usuario` (ManyToOne con Usuario)
  - `juego` (ManyToOne con Juego)

#### `BibliotecaUsuario`:
- **Campos**: id, usuario, juego, fechaAdquisicion
- **Propósito**: Almacena los juegos que posee cada usuario.
- **Validación**: `existsByUsuarioAndJuego()` evita duplicados.
- **Relaciones**:
  - `usuario` (ManyToOne con Usuario)
  - `juego` (ManyToOne con Juego)

#### `MovimientoProveedor`:
- **Campos**: id, proveedor, monto, fechaMovimiento, estado, descripcion, metodoCobro, numeroTarjeta, titularTarjeta, iban
- **Estados**: PENDIENTE, COBRADO
- **Métodos de cobro**: PAYPAL, TARJETA
- **Relaciones**:
  - `proveedor` (ManyToOne con Usuario)

---

## 🖼️ Gestión de Imágenes (Cloudinary)

### Configuración:
- **Cloud Name**: `pixelshop`
- **API Key**: `742135482932556`
- **API Secret**: `k71G2RrGPrW09sSzmVRQ2PHt0K4`

### Uso:
- Los proveedores suben imágenes de portada al publicar juegos.
- Las imágenes se suben a Cloudinary en la carpeta `juegos/`.
- Se devuelve una URL pública que se guarda en `Juego.imagenUrl`.
- Tamaño máximo: 5MB por imagen.

---

## 🔧 Tecnologías Utilizadas

### Backend:
- **Spring Boot 3.4.2**: Framework principal.
- **Spring Security 6.4.2**: Autenticación y autorización.
- **Spring Data JPA**: ORM para base de datos.
- **MySQL Connector**: Driver de MySQL.
- **Thymeleaf**: Motor de plantillas HTML.
- **BCrypt**: Encriptación de contraseñas.

### Servicios Externos:
- **PayPal Checkout SDK 2.0.0**: Pagos de clientes.
- **PayPal REST SDK 1.14.0**: Payouts a proveedores.
- **Resend Java SDK 4.11.0**: Envío de emails.
- **Cloudinary SDK 2.0.2**: Almacenamiento de imágenes.
- **Google OAuth2**: Login con Google.

### Frontend:
- **Thymeleaf**: Renderizado de vistas en servidor.
- **Bootstrap 5.3**: Framework CSS responsive.
- **Font Awesome 6.x**: Iconos.
- **JavaScript Vanilla**: Validaciones y dinámicas del frontend.

---

## 📦 Dependencias Clave (pom.xml)

```xml
<!-- Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Boot Starter Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Spring Boot Starter Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Boot Starter OAuth2 Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Thymeleaf -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- PayPal Checkout SDK -->
<dependency>
    <groupId>com.paypal.sdk</groupId>
    <artifactId>checkout-sdk</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- PayPal REST API SDK (para Payouts) -->
<dependency>
    <groupId>com.paypal.sdk</groupId>
    <artifactId>rest-api-sdk</artifactId>
    <version>1.14.0</version>
</dependency>

<!-- Resend Email SDK -->
<dependency>
    <groupId>com.resend</groupId>
    <artifactId>resend-java</artifactId>
    <version>4.11.0</version>
</dependency>

<!-- Cloudinary -->
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http45</artifactId>
    <version>2.0.2</version>
</dependency>

<!-- DevTools (Hot Reload) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## ⚙️ Configuración (application.properties)

```properties
# Nombre de la aplicación
spring.application.name=spring_crud_thymeleaf_v21

# MySQL en Azure
spring.datasource.url=jdbc:mysql://mysqldemojuan.mysql.database.azure.com:3306/bd_PixelShop?sslMode=REQUIRED&serverTimezone=UTC
spring.datasource.username=adminuser
spring.datasource.password=Poppy@azure

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Subida de archivos
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=15MB

# Resend (Email)
resend.api-key=${RESEND_API_KEY}
email.from=noreply@pixelshop.com

# Cloudinary (Imágenes)
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_SECRET}
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# PayPal Sandbox
paypal.client-id=${PAYPAL_CLIENT_ID}
paypal.client-secret=${PAYPAL_CLIENT_SECRET}
paypal.mode=sandbox

# Logging (para debugging)
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2=TRACE
logging.level.com.example=DEBUG
```

---

## 🚀 Cómo Arrancar la Aplicación

### Opción 1: Script Batch (Recomendado)
```bash
cd C:\Prueba\spring_crud_thymeleaf_v21
.\start.bat
```

Este script carga automáticamente todas las variables de entorno necesarias:
- RESEND_API_KEY
- CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
- GOOGLE_CLIENT_ID, GOOGLE_SECRET
- PAYPAL_CLIENT_ID, PAYPAL_CLIENT_SECRET

### Opción 2: Maven Directo (Requiere variables de entorno)
```bash
mvn spring-boot:run
```

### Acceso:
- **URL**: http://localhost:8080
- **Puerto**: 8080

---

## 🗂️ Estructura del Proyecto

```
src/main/java/com/example/
├── App.java                          # Clase principal Spring Boot
├── config/
│   ├── CloudinaryConfig.java        # Configuración de Cloudinary
│   ├── EmailConfig.java             # Configuración de Resend
│   ├── PasswordEncoderConfig.java   # BCrypt Encoder (separado para evitar dependencia circular)
│   ├── PayPalConfig.java            # Configuración de PayPal Checkout
│   ├── SecurityConfig.java          # Seguridad y OAuth2
│   └── DataSeeder.java              # Datos iniciales (admin, juegos demo)
├── controller/
│   ├── AuthController.java          # Login, registro, logout
│   ├── CatalogoController.java      # Listar juegos, búsqueda
│   ├── CompraController.java        # Proceso de compra
│   ├── PerfilController.java        # Editar perfil, eliminar cuenta
│   ├── ProveedorController.java     # Publicar juegos, ventas, cobros
│   ├── UsuarioController.java       # Biblioteca, compras
│   └── AdminController.java         # Panel admin
├── model/
│   ├── Usuario.java                 # Entidad Usuario
│   ├── Juego.java                   # Entidad Juego
│   ├── Compra.java                  # Entidad Compra
│   ├── BibliotecaUsuario.java       # Entidad Biblioteca
│   ├── MovimientoProveedor.java     # Entidad Movimiento
│   ├── Rol.java                     # Enum de roles
│   └── EstadoMovimiento.java        # Enum de estados
├── repository/
│   ├── UsuarioRepository.java
│   ├── JuegoRepository.java
│   ├── CompraRepository.java
│   ├── BibliotecaUsuarioRepository.java
│   └── MovimientoProveedorRepository.java
└── service/
    ├── CustomOAuth2UserService.java     # Lógica OAuth2
    ├── UserDetailsServiceImpl.java     # Login tradicional
    ├── EmailService.java                # Envío de emails
    ├── PayPalService.java               # Checkout (compras)
    └── PayPalPayoutService.java         # Payouts (cobros)

src/main/resources/
├── application.properties           # Configuración principal
├── templates/                       # Vistas Thymeleaf
│   ├── auth/                       # Login, registro
│   ├── catalogo/                   # Listado de juegos, detalle
│   ├── usuario/                    # Biblioteca, compras, perfil
│   ├── proveedor/                  # Publicar, mis juegos, ventas
│   ├── admin/                      # Panel de administración
│   └── fragments/                  # Header, footer
└── static/
    ├── css/                        # Estilos por sección
    └── images/                     # Imágenes locales
```

---

## 🔒 Seguridad

### Configuración de Acceso:

```java
// Rutas públicas (sin login)
.requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()

// Rutas autenticadas (cualquier usuario)
.requestMatchers("/", "/catalogo/**", "/perfil").authenticated()

// Compras: Solo CLIENTE y PROVEEDOR
.requestMatchers("/compra/**").hasAnyRole("CLIENTE", "PROVEEDOR")

// Biblioteca: Solo CLIENTE y PROVEEDOR
.requestMatchers("/mi-biblioteca").hasAnyRole("CLIENTE", "PROVEEDOR")

// Área de proveedores: PROVEEDOR y ADMIN
.requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")

// Administración: Solo ADMIN
.requestMatchers("/admin/**").hasRole("ADMIN")
```

### Protecciones Implementadas:
- **CSRF Deshabilitado**: Solo para desarrollo (debe activarse en producción).
- **Contraseñas**: Encriptadas con BCrypt.
- **OAuth2**: Tokens gestionados por Spring Security.
- **Roles**: Validación en cada endpoint.

---

## 🐛 Problemas Conocidos y Soluciones

### 1. **Emails No Se Envían (Resend)**
- **Problema**: Dominio `pixelshop.com` no verificado en Resend.
- **Solución**: 
  - Verificar el dominio en Resend Dashboard.
  - O usar un dominio de pruebas de Resend.
  - O usar el email personal verificado.

### 2. **Google OAuth2 Error 401**
- **Problema**: Client Secret incorrecto o expirado.
- **Solución**: 
  - Ir a https://console.cloud.google.com/apis/credentials
  - Copiar el Client Secret correcto.
  - Actualizar `start.bat` con el nuevo secret.
  - **Actual**: `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`

### 3. **PayPal Payouts No Funciona**
- **Problema**: Cuenta sandbox no tiene permisos de Business.
- **Solución**:
  - Solicitar aprobación de PayPal Payouts.
  - O usar cuenta Business real en producción.

### 4. **Puerto 8080 Ocupado**
- **Problema**: Otra instancia de Java corriendo.
- **Solución**: 
  ```bash
  taskkill /F /IM java.exe
  ```

### 5. **Dependencia Circular (PasswordEncoder)**
- **Problema**: SecurityConfig y CustomOAuth2UserService se inyectaban mutuamente.
- **Solución**: Crear `PasswordEncoderConfig` separado con el Bean.

---

## 📊 Casos de Uso Completos

### Caso 1: Cliente Compra un Juego
1. Cliente navega el catálogo en `/`.
2. Hace clic en un juego para ver detalles.
3. Hace clic en "Comprar".
4. Se redirige a PayPal para pagar 50€.
5. Cliente aprueba el pago.
6. PayPal redirige a `/compra/confirmar?token=XXX`.
7. Sistema captura el pago y crea:
   - Compra (50€).
   - BibliotecaUsuario (añade juego).
   - MovimientoProveedor (42.50€ PENDIENTE para el proveedor).
8. Se envía email de confirmación.
9. Cliente ve el juego en "Mi Biblioteca".

### Caso 2: Proveedor Publica un Juego
1. Proveedor accede a "Publicar Juego".
2. Llena formulario: título, descripción, precio, género, imagen.
3. Hace clic en "Publicar" (requiere pago de 25€).
4. Se redirige a PayPal para pagar la tarifa.
5. PayPal redirige a `/proveedor/confirmar-publicacion?token=XXX`.
6. Sistema captura el pago y guarda el juego en BD.
7. El juego aparece en el catálogo público.

### Caso 3: Proveedor Cobra sus Ganancias (PayPal)
1. Proveedor accede a "Mis Ventas".
2. Ve "Ingresos pendientes: 42.50€".
3. Hace clic en "Cobrar".
4. Selecciona "PayPal".
5. Ingresa email de PayPal (auto-completa si ya existe).
6. Hace clic en "Solicitar Cobro".
7. Sistema ejecuta payout vía PayPal API.
8. El dinero llega a la cuenta PayPal del proveedor.
9. Estado del movimiento cambia a COBRADO.

### Caso 4: Usuario se Registra con Google OAuth2
1. Usuario accede a `/login`.
2. Hace clic en "Continuar con Google".
3. Selecciona su cuenta de Google.
4. Autoriza acceso a email y perfil.
5. Sistema crea nuevo Usuario con:
   - Email y nombre de Google.
   - Contraseña aleatoria (UUID) encriptada.
   - Rol CLIENTE.
6. Se envía email de bienvenida.
7. Usuario entra directamente al catálogo.

### Caso 5: Admin Revisa Movimientos
1. Admin accede a `/admin/movimientos`.
2. Ve tabla con todos los movimientos de la plataforma.
3. Ve estado: PENDIENTE o COBRADO.
4. Ve método: PAYPAL o TARJETA.
5. Para TARJETA, ve IBAN y titular (para transferencia manual).

---

## 🎯 Próximas Mejoras Sugeridas

1. **Sistema de Reseñas**: Permitir que los clientes califiquen juegos.
2. **Filtros Avanzados**: Filtrar por precio, popularidad, fecha.
3. **Carrito de Compras**: Comprar múltiples juegos a la vez.
4. **Descuentos y Cupones**: Sistema de promociones.
5. **Verificación de Email**: Enviar link de confirmación al registrarse.
6. **Panel de Estadísticas**: Gráficas para proveedores y admin.
7. **Sistema de Mensajería**: Chat entre proveedores y clientes.
8. **API REST**: Exponer endpoints para aplicaciones móviles.
9. **Activar CSRF**: En producción, habilitar protección CSRF.
10. **Migrar a PayPal Live**: Cambiar de sandbox a producción.

---

## 📞 Soporte y Mantenimiento

### Logs Importantes:
- Todos los intentos de email se loguean con emojis (📧, ✅, ❌).
- OAuth2 tiene logs TRACE para debugging.
- PayPal loguea todas las transacciones.

### Variables de Entorno Críticas:
- `GOOGLE_CLIENT_ID` y `GOOGLE_SECRET`: Login con Google.
- `PAYPAL_CLIENT_ID` y `PAYPAL_CLIENT_SECRET`: Pagos.
- `RESEND_API_KEY`: Emails.
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`: Imágenes.

### Contacto Base de Datos:
- **Host**: mysqldemojuan.mysql.database.azure.com
- **Puerto**: 3306
- **User**: adminuser
- **Pass**: Poppy@azure

---

## ✅ Checklist de Producción

- [ ] Cambiar `paypal.mode` a `live`
- [ ] Actualizar credenciales de PayPal a producción
- [ ] Activar CSRF en `SecurityConfig`
- [ ] Verificar dominio en Resend
- [ ] Configurar HTTPS (Let's Encrypt)
- [ ] Cambiar `spring.jpa.hibernate.ddl-auto` a `validate`
- [ ] Deshabilitar logs DEBUG/TRACE
- [ ] Configurar límite de rate limiting
- [ ] Backup automático de base de datos
- [ ] Monitoreo de errores (Sentry, New Relic)

---

**Última actualización**: 22 de Noviembre de 2025  
**Versión del proyecto**: 2.1  
**Autor**: Juan PM
