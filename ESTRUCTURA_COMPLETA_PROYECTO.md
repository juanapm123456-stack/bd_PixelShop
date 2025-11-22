# 🏗️ Estructura Completa del Proyecto - Pixel Shop

## 📂 Estructura de Directorios

```
spring_crud_thymeleaf_v21/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── App.java                          # Clase principal de Spring Boot
│   │   │   │
│   │   │   ├── config/                           # Configuraciones del proyecto
│   │   │   │   ├── CloudinaryConfig.java         # Configuración de Cloudinary
│   │   │   │   ├── DataSeeder.java               # Datos iniciales (seeder)
│   │   │   │   ├── PasswordEncoderConfig.java    # Configuración de BCrypt
│   │   │   │   ├── PayPalConfig.java             # Configuración de PayPal SDK
│   │   │   │   └── SecurityConfig.java           # Configuración de Spring Security
│   │   │   │
│   │   │   ├── controller/                       # Controladores MVC
│   │   │   │   ├── AdminController.java          # Panel de administrador
│   │   │   │   ├── AuthController.java           # Login y registro
│   │   │   │   ├── BaseController.java           # Clase base con métodos comunes
│   │   │   │   ├── BibliotecaController.java     # Biblioteca de usuario
│   │   │   │   ├── CatalogoController.java       # Catálogo de juegos
│   │   │   │   ├── CloudinaryApiController.java  # API de Cloudinary
│   │   │   │   ├── CompraController.java         # Proceso de compra
│   │   │   │   ├── PayPalController.java         # Integración con PayPal
│   │   │   │   ├── PerfilController.java         # Perfil de usuario
│   │   │   │   └── ProveedorController.java      # Panel de proveedor
│   │   │   │
│   │   │   ├── dto/                              # Data Transfer Objects
│   │   │   │   └── ApiResponse.java              # Respuesta genérica para APIs
│   │   │   │
│   │   │   ├── model/                            # Entidades JPA
│   │   │   │   ├── BibliotecaUsuario.java        # Biblioteca de juegos del usuario
│   │   │   │   ├── Compra.java                   # Registro de compras
│   │   │   │   ├── Juego.java                    # Juego publicado
│   │   │   │   ├── MovimientoProveedor.java      # Movimientos financieros del proveedor
│   │   │   │   ├── PublicacionJuego.java         # Registro de publicación (25€)
│   │   │   │   ├── Rol.java                      # Enum de roles (CLIENTE, PROVEEDOR, ADMIN)
│   │   │   │   └── Usuario.java                  # Usuario del sistema
│   │   │   │
│   │   │   ├── repository/                       # Repositorios JPA
│   │   │   │   ├── BibliotecaUsuarioRepository.java
│   │   │   │   ├── CompraRepository.java
│   │   │   │   ├── JuegoRepository.java
│   │   │   │   ├── MovimientoProveedorRepository.java
│   │   │   │   ├── PublicacionJuegoRepository.java
│   │   │   │   └── UsuarioRepository.java
│   │   │   │
│   │   │   └── service/                          # Servicios de lógica de negocio
│   │   │       ├── CloudinaryService.java        # Gestión de imágenes con Cloudinary
│   │   │       ├── CustomOAuth2UserService.java  # Autenticación con Google OAuth2
│   │   │       ├── EmailService.java             # Envío de emails con Gmail SMTP
│   │   │       ├── PayPalPayoutService.java      # Pagos a proveedores con PayPal
│   │   │       ├── PayPalService.java            # Pagos de clientes con PayPal
│   │   │       └── UserDetailsServiceImpl.java   # Autenticación con Spring Security
│   │   │
│   │   └── resources/
│   │       ├── application.properties            # Configuración de la aplicación
│   │       ├── static/                           # Archivos estáticos
│   │       │   ├── css/                          # Estilos CSS
│   │       │   │   ├── global.css                # (Eliminado - se usa Tailwind CSS)
│   │       │   │   ├── auth/                     # Estilos de login/register
│   │       │   │   │   ├── login.css             # ✅ Conservado - diseño custom
│   │       │   │   │   └── register.css          # ✅ Conservado - diseño custom
│   │       │   │   ├── admin/                    # (Estilos sin usar)
│   │       │   │   ├── catalogo/                 # (Eliminados - se usa Tailwind)
│   │       │   │   ├── proveedor/                # (Eliminados - se usa Tailwind)
│   │       │   │   └── usuario/                  # (Eliminados - se usa Tailwind)
│   │       │   └── images/                       # Imágenes del proyecto
│   │       │       └── juegos/                   # Imágenes de juegos publicados
│   │       │
│   │       └── templates/                        # Plantillas Thymeleaf
│   │           ├── admin/                        # Vistas de administrador
│   │           │   ├── ganancias.html            # Panel de ganancias
│   │           │   ├── movimientos.html          # Historial de transacciones
│   │           │   └── usuarios.html             # Gestión de usuarios
│   │           │
│   │           ├── auth/                         # Vistas de autenticación
│   │           │   ├── login.html                # Página de login (CSS custom)
│   │           │   └── register.html             # Página de registro (CSS custom)
│   │           │
│   │           ├── catalogo/                     # Vistas de catálogo
│   │           │   ├── index.html                # Catálogo principal (Tailwind CSS)
│   │           │   └── juego-detalle.html        # Detalles del juego (Tailwind CSS)
│   │           │
│   │           ├── checkout/                     # Vistas de pago
│   │           │   └── payment-options.html      # Opciones de pago PayPal (Tailwind CSS)
│   │           │
│   │           ├── fragments/                    # Fragmentos reutilizables
│   │           │   ├── footer.html               # Footer (Tailwind CSS)
│   │           │   └── header.html               # Navbar (Tailwind CSS)
│   │           │
│   │           ├── proveedor/                    # Vistas de proveedor
│   │           │   ├── editar-juego.html         # Editar juego publicado
│   │           │   ├── mis-juegos.html           # Listado de juegos propios
│   │           │   ├── publicar-juego.html       # Formulario publicar juego
│   │           │   └── ventas.html               # Panel de ventas y cobros
│   │           │
│   │           └── usuario/                      # Vistas de usuario
│   │               ├── mi-biblioteca.html        # Biblioteca de juegos (Tailwind CSS)
│   │               ├── mis-compras.html          # Historial de compras (Tailwind CSS)
│   │               └── perfil.html               # Editar perfil (Tailwind CSS)
│   │
│   └── test/
│       └── java/com/example/
│           └── SpringCrudThymeleafV21ApplicationTests.java
│
├── target/                                       # Archivos compilados
├── pom.xml                                       # Dependencias Maven
├── README.md                                     # Documentación del proyecto
├── FUNCIONALIDADES_FUTURAS.md                   # Características pendientes
├── DOCUMENTACION_COMPLETA_PROYECTO.md           # Documentación técnica completa
├── PLAN_REORGANIZACION.md                       # Plan de refactorización
└── MODELOS_BASE_DATOS_ALTERNATIVOS.md          # Diseños de BD alternativos
```

---

## 🎯 Clase Principal

### **App.java**
```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

**Función:**
- Clase principal de Spring Boot
- Arranca el servidor embebido (Tomcat)
- Configura el contexto de Spring automáticamente

---

## ⚙️ Configuraciones (`config/`)

### **CloudinaryConfig.java**
**Función:** Configura el cliente de Cloudinary para gestión de imágenes en la nube

**Responsabilidades:**
- Lee credenciales desde `application.properties` (`cloudinary.cloud-name`, `api-key`, `api-secret`)
- Crea un bean `Cloudinary` inyectable en otros componentes
- Configura conexión segura (HTTPS)

**¿Para qué sirve?**
Permite subir, eliminar y gestionar imágenes de juegos y perfiles de usuario sin almacenarlas localmente.

---

### **DataSeeder.java**
**Función:** Inserta datos de prueba al iniciar la aplicación (solo si la BD está vacía)

**Responsabilidades:**
- Crea usuarios de prueba: 1 admin, 2 proveedores, 3 clientes
- Crea 10 juegos de ejemplo con imágenes placeholder
- Registra publicaciones de juegos (cobro de 25€)
- Simula compras de ejemplo con biblioteca y movimientos de proveedor

**¿Para qué sirve?**
Facilita el desarrollo y testing sin necesidad de crear datos manualmente cada vez.

---

### **PasswordEncoderConfig.java**
**Función:** Configura el encriptador de contraseñas BCrypt

**Responsabilidades:**
- Crea un bean `PasswordEncoder` de tipo `BCryptPasswordEncoder`
- Utilizado para encriptar contraseñas antes de guardarlas
- Valida contraseñas al hacer login

**¿Para qué sirve?**
Garantiza que las contraseñas nunca se almacenen en texto plano en la base de datos.

---

### **PayPalConfig.java**
**Función:** Configura el cliente de PayPal para procesamiento de pagos

**Responsabilidades:**
- Lee credenciales desde variables de entorno (`PAYPAL_CLIENT_ID`, `PAYPAL_CLIENT_SECRET`)
- Configura entorno Sandbox o Live según `paypal.mode`
- Crea un bean `PayPalHttpClient` inyectable

**¿Para qué sirve?**
Permite crear órdenes de pago, capturar pagos y enviar pagos a proveedores (Payouts).

---

### **SecurityConfig.java**
**Función:** Configura Spring Security (autenticación y autorización)

**Responsabilidades:**
- Define rutas públicas (`/login`, `/register`, `/css/**`, `/images/**`)
- Restringe rutas según roles:
  - `/compra/**` → Solo CLIENTE y PROVEEDOR
  - `/proveedor/**` → Solo PROVEEDOR y ADMIN
  - `/admin/**` → Solo ADMIN
- Configura login con formulario tradicional
- Configura login con Google OAuth2
- Desactiva CSRF (⚠️ solo para desarrollo, activar en producción)

**¿Para qué sirve?**
Protege la aplicación asegurando que solo usuarios autenticados y autorizados accedan a cada funcionalidad.

---

## 🎮 Controladores (`controller/`)

### **BaseController.java**
**Función:** Clase base con métodos comunes para todos los controladores

**Responsabilidades:**
- `obtenerEmailDelUsuario(Authentication)`: Extrae el email del usuario autenticado (funciona con login normal y OAuth2)

**¿Para qué sirve?**
Evita duplicar código en todos los controladores que necesiten obtener el usuario actual.

---

### **AuthController.java**
**Rutas:**
- `GET /login` → Muestra formulario de login
- `GET /register` → Muestra formulario de registro
- `POST /register` → Procesa el registro

**Responsabilidades:**
- Valida que el email no esté registrado
- Encripta la contraseña con BCrypt
- Asigna rol CLIENTE por defecto
- Guarda el usuario en la BD
- Envía email de bienvenida con `EmailService`

**¿Para qué sirve?**
Gestiona el proceso completo de autenticación y registro de nuevos usuarios.

---

### **CatalogoController.java**
**Rutas:**
- `GET /` → Catálogo principal (todos los juegos activos)
- `GET /juego/{id}` → Detalles de un juego
- `GET /buscar?q=...` → Búsqueda de juegos por título

**Responsabilidades:**
- Lista todos los juegos activos
- Marca juegos ya comprados por el usuario actual (solo CLIENTE y PROVEEDOR, ADMIN no compra)
- Verifica si el usuario ya compró un juego para mostrar/ocultar botón "Comprar"
- Filtra juegos por título en búsqueda

**¿Para qué sirve?**
Muestra el catálogo público y permite explorar los juegos disponibles.

---

### **CompraController.java**
**Rutas:**
- `POST /compra/{juegoId}` → Inicia proceso de compra (redirige a PayPal)
- `GET /compra/confirmar/{juegoId}` → Confirma la compra tras pago exitoso
- `GET /compra/mis-compras` → Historial de compras del usuario

**Responsabilidades:**
- **Validación crítica:** Bloquea compras de ADMIN
- Verifica que el usuario no haya comprado ya el juego
- Crea registro de compra
- Añade juego a biblioteca del usuario
- Calcula y registra movimiento del proveedor (85% del precio)
- Envía email de confirmación con `EmailService`

**¿Para qué sirve?**
Procesa la compra completa: validación, registro, comisiones y notificaciones.

---

### **PayPalController.java**
**Rutas:**
- `GET /paypal/checkout/{juegoId}` → Página de checkout con PayPal
- `POST /paypal/create-order/{juegoId}` → Crea orden de pago en PayPal (API REST)
- `GET /paypal/success` → Callback tras pago exitoso
- `GET /paypal/cancel` → Callback si se cancela el pago
- `POST /paypal/card-payment/{juegoId}` → Simulación de pago con tarjeta (solo desarrollo)

**Responsabilidades:**
- Integra PayPal Checkout SDK
- Crea órdenes con `PayPalService.crearOrden()`
- Captura el pago con `PayPalService.capturarPago()`
- Valida que el pago se haya completado
- Redirige a `CompraController.confirmarCompra()` para finalizar

**¿Para qué sirve?**
Maneja todo el flujo de pago con PayPal: creación de orden, captura y confirmación.

---

### **BibliotecaController.java**
**Rutas:**
- `GET /mi-biblioteca` → Biblioteca de juegos del usuario

**Responsabilidades:**
- Obtiene usuario autenticado
- Lista todos los juegos en su biblioteca (ordenados por fecha de adquisición)
- Muestra fecha de compra de cada juego

**¿Para qué sirve?**
Permite al usuario ver todos los juegos que ha comprado.

---

### **PerfilController.java**
**Rutas:**
- `GET /perfil` → Muestra perfil del usuario
- `POST /perfil` → Actualiza datos del perfil
- `DELETE /perfil/eliminar` → Elimina la cuenta del usuario

**Responsabilidades:**
- Permite editar nombre, email, contraseña
- Para PROVEEDOR: editar datos fiscales y email PayPal
- Valida que el nuevo email no esté en uso
- Encripta nueva contraseña si se cambia
- Elimina cuenta (hard delete, no soft delete)

**¿Para qué sirve?**
Gestiona la información personal del usuario y permite eliminar su cuenta.

---

### **ProveedorController.java**
**Rutas:**
- `GET /proveedor/publicar` → Formulario para publicar juego
- `POST /proveedor/publicar` → Procesa publicación de juego
- `GET /proveedor/mis-juegos` → Lista juegos del proveedor
- `GET /proveedor/ventas` → Panel de ventas y cobros
- `POST /proveedor/cobrar/{movimientoId}` → Solicita cobro de venta
- `GET /proveedor/editar/{id}` → Formulario editar juego
- `POST /proveedor/editar/{id}` → Actualiza juego

**Responsabilidades:**
- **Publicar juego:**
  - Valida y sube 4 imágenes (1 portada + 3 capturas)
  - Guarda imágenes en `/static/images/juegos/`
  - Valida dimensiones (mínimo 200x200px, máximo 3000x3000px)
  - Valida tamaño (máximo 10MB por imagen)
  - Registra publicación con cobro de 25€
- **Ventas:**
  - Lista todos los movimientos del proveedor
  - Calcula ingresos pendientes de cobro
  - Permite solicitar pago por PayPal Payout o transferencia
  - Valida email PayPal y datos bancarios
  - Envía pago con `PayPalPayoutService`
- **Editar:**
  - Solo permite editar descripción, video e imágenes
  - NO permite cambiar título, precio ni proveedor

**¿Para qué sirve?**
Panel completo para que proveedores gestionen sus juegos y cobren sus ventas.

---

### **AdminController.java**
**Rutas:**
- `GET /admin/usuarios` → Lista todos los usuarios
- `POST /admin/usuario/eliminar/{id}` → Elimina un usuario
- `GET /admin/ganancias` → Panel de ganancias de la plataforma
- `GET /admin/movimientos` → Historial de todas las transacciones

**Responsabilidades:**
- **Usuarios:** Listar y eliminar usuarios (excepto admins)
- **Ganancias:**
  - Calcula comisiones (15% de todas las ventas)
  - Calcula ingresos por publicaciones (25€ x juego publicado)
  - Muestra ganancia total
- **Movimientos:**
  - Lista todas las compras (excluye compras de ADMIN si las hubiera)
  - Muestra fecha, usuario, juego, precio

**¿Para qué sirve?**
Panel administrativo para supervisar usuarios, finanzas y transacciones de la plataforma.

---

### **CloudinaryApiController.java**
**Función:** API REST para subir/eliminar imágenes a Cloudinary (no se usa actualmente)

**¿Para qué sirve?**
Permite subir imágenes desde JavaScript sin procesar en backend (útil para futuras features como fotos de perfil).

---

## 🗂️ Modelos (`model/`)

### **Usuario.java**
**Tabla:** `usuario`

**Campos:**
- `id` (Long) - PK
- `nombre` (String) - Nombre completo
- `email` (String, UNIQUE) - Email único
- `password` (String) - Contraseña encriptada con BCrypt
- `rol` (Enum: CLIENTE, PROVEEDOR, ADMIN) - Rol del usuario
- `datosFiscales` (Text) - Datos fiscales (solo proveedores)
- `fechaRegistro` (LocalDateTime) - Fecha de registro
- `activo` (Boolean) - Cuenta activa o no

**Relaciones:**
- `@OneToMany` con `Juego` (si es PROVEEDOR)
- `@OneToMany` con `Compra` (si compra juegos)
- `@OneToMany` con `BibliotecaUsuario` (juegos en biblioteca)

**¿Para qué sirve?**
Entidad central que representa a todos los usuarios (clientes, proveedores y admins).

---

### **Juego.java**
**Tabla:** `juego`

**Campos:**
- `id` (Long) - PK
- `titulo` (String) - Título del juego
- `descripcion` (Text) - Descripción
- `precio` (BigDecimal) - Precio en euros
- `genero` (String) - Género (RPG, Aventura, etc.)
- `imagenUrl1/2/3/4` (String) - URLs de imágenes
- `videoYoutubeUrl` (String) - URL de video de YouTube
- `proveedor` (Usuario) - FK al proveedor
- `fechaPublicacion` (LocalDateTime)
- `activo` (Boolean) - Juego visible o no

**Relaciones:**
- `@ManyToOne` con `Usuario` (proveedor)
- `@OneToMany` con `Compra`
- `@OneToMany` con `BibliotecaUsuario`

**¿Para qué sirve?**
Representa un juego publicado en la plataforma.

---

### **Compra.java**
**Tabla:** `compra`

**Campos:**
- `id` (Long) - PK
- `usuario` (Usuario) - FK al comprador
- `juego` (Juego) - FK al juego comprado
- `precioPagado` (BigDecimal) - Precio al momento de compra
- `fechaCompra` (LocalDateTime)
- `metodoPago` (String) - "PAYPAL", "TARJETA", etc.
- `pagadoAlProveedor` (Boolean) - Si ya se pagó al proveedor

**Relaciones:**
- `@ManyToOne` con `Usuario`
- `@ManyToOne` con `Juego`

**Restricción única:** No se puede comprar el mismo juego dos veces (UNIQUE KEY en `usuario_id`, `juego_id`)

**¿Para qué sirve?**
Registra cada transacción de compra entre cliente y plataforma.

---

### **BibliotecaUsuario.java**
**Tabla:** `biblioteca_usuario`

**Campos:**
- `id` (Long) - PK
- `usuario` (Usuario) - FK al usuario
- `juego` (Juego) - FK al juego
- `fechaAdquisicion` (LocalDateTime)

**Relaciones:**
- `@ManyToOne` con `Usuario`
- `@ManyToOne` con `Juego`

**⚠️ Problema identificado:** Esta tabla es **redundante** porque duplica información de `compra`. Se recomienda eliminarla en el plan de reorganización.

**¿Para qué sirve?**
Lista los juegos que un usuario posee (debería calcularse desde `compra` en lugar de tabla separada).

---

### **MovimientoProveedor.java**
**Tabla:** `movimiento_proveedor`

**Campos:**
- `id` (Long) - PK
- `proveedor` (Usuario) - FK al proveedor
- `compra` (Compra) - FK a la compra que generó el movimiento
- `montoNeto` (BigDecimal) - 85% del precio (comisión del 15% restada)
- `pagado` (Boolean) - Si ya se le pagó al proveedor
- `fechaCobro` (LocalDateTime) - Fecha del pago
- `metodoCobro` (String) - "PAYPAL", "TARJETA"
- `emailPayPalProveedor` (String) - Email de PayPal del proveedor
- `payoutBatchId` (String) - ID de PayPal Payout
- `numeroTarjeta` (String) - Últimos 4 dígitos o IBAN
- `titularTarjeta` (String) - Nombre del titular

**Relaciones:**
- `@ManyToOne` con `Usuario` (proveedor)
- `@ManyToOne` con `Compra`

**¿Para qué sirve?**
Registra las ganancias del proveedor por cada venta y si ya se le ha pagado.

---

### **PublicacionJuego.java**
**Tabla:** `publicacion_juego`

**Campos:**
- `id` (Long) - PK
- `juego` (Juego) - FK al juego publicado
- `proveedor` (Usuario) - FK al proveedor
- `fechaPublicacion` (LocalDateTime)
- `pagado` (Boolean) - Si pagó los 25€

**Relaciones:**
- `@OneToOne` con `Juego`
- `@ManyToOne` con `Usuario`

**⚠️ Problema identificado:** Esta tabla es **innecesaria** porque solo guarda un booleano de pago. Se recomienda eliminarla y agregar campos `fecha_pago_publicacion` y `monto_pago_publicacion` en `Juego`.

**¿Para qué sirve?**
Registra el pago de 25€ por publicar un juego (debería ser campo en `Juego`).

---

### **Rol.java**
**Enum:** `CLIENTE`, `PROVEEDOR`, `ADMIN`

**Valores:**
- **CLIENTE:** Solo puede comprar juegos
- **PROVEEDOR:** Puede comprar Y publicar juegos
- **ADMIN:** Solo gestiona plataforma, NO puede comprar

**¿Para qué sirve?**
Define los 3 tipos de usuarios del sistema y sus capacidades.

---

## 📊 Repositorios (`repository/`)

Todos los repositorios extienden `JpaRepository<Entidad, Long>` y proporcionan métodos CRUD automáticos.

### **UsuarioRepository.java**
**Métodos custom:**
- `Optional<Usuario> findByEmail(String email)` - Buscar por email
- `boolean existsByEmail(String email)` - Verificar si existe email

**¿Para qué sirve?**
Acceso a datos de usuarios (login, registro, búsqueda).

---

### **JuegoRepository.java**
**Métodos custom:**
- `List<Juego> findByActivoTrue()` - Listar juegos activos
- `List<Juego> findByProveedor(Usuario proveedor)` - Juegos de un proveedor
- `List<Juego> findByTituloContainingIgnoreCaseAndActivoTrue(String titulo)` - Búsqueda por título

**¿Para qué sirve?**
Acceso a datos de juegos (catálogo, búsqueda, gestión de proveedor).

---

### **CompraRepository.java**
**Métodos custom:**
- `List<Compra> findByUsuarioOrderByFechaCompraDesc(Usuario usuario)` - Compras de un usuario
- `boolean existsByUsuarioAndJuego(Usuario usuario, Juego juego)` - Verificar compra duplicada
- `@Query` `BigDecimal calcularTotalVentas()` - Suma total de todas las ventas

**¿Para qué sirve?**
Acceso a datos de compras (historial, validaciones, estadísticas).

---

### **BibliotecaUsuarioRepository.java**
**Métodos custom:**
- `List<BibliotecaUsuario> findByUsuarioOrderByFechaAdquisicionDesc(Usuario usuario)` - Biblioteca de un usuario

**¿Para qué sirve?**
Acceso a la biblioteca de juegos de un usuario.

---

### **MovimientoProveedorRepository.java**
**Métodos custom:**
- `List<MovimientoProveedor> findByProveedorOrderByFechaDesc(Usuario proveedor)` - Movimientos de un proveedor
- `@Query` `BigDecimal calcularIngresosPendientes(Usuario proveedor)` - Suma de ingresos no cobrados

**¿Para qué sirve?**
Acceso a movimientos financieros de proveedores (ventas, cobros).

---

### **PublicacionJuegoRepository.java**
**Métodos custom:**
- `@Query` `Long contarPublicacionesPagadas()` - Cuenta publicaciones pagadas (25€ cada una)

**¿Para qué sirve?**
Acceso a datos de publicaciones (estadísticas de ganancias por publicación).

---

## 🛠️ Servicios (`service/`)

### **UserDetailsServiceImpl.java**
**Función:** Implementa `UserDetailsService` de Spring Security

**Responsabilidades:**
- Busca usuario por email en la BD
- Convierte `Usuario` a `UserDetails` de Spring Security
- Asigna roles con prefijo `ROLE_` (ej: `ROLE_CLIENTE`)

**¿Para qué sirve?**
Permite a Spring Security autenticar usuarios con login tradicional (email + password).

---

### **CustomOAuth2UserService.java**
**Función:** Maneja autenticación con Google OAuth2

**Responsabilidades:**
- Recibe datos del usuario de Google (`email`, `name`, `picture`)
- Busca si el usuario ya existe en la BD
- Si no existe, crea nuevo usuario con rol CLIENTE
- Retorna `UserDetails` para Spring Security

**¿Para qué sirve?**
Permite login con Google sin necesidad de crear contraseña.

---

### **EmailService.java**
**Función:** Envía emails HTML con Gmail SMTP

**Responsabilidades:**
- `enviarEmailBienvenida(Usuario)`: Email al registrarse
- `enviarConfirmacionCompra(Compra)`: Email tras comprar juego
- `enviarRecuperacionPassword(email, nombre, token)`: Email para resetear contraseña
- `enviarNotificacionEnvio(Compra, numSeguimiento)`: Email de envío (no se usa actualmente)

**Configuración:**
- SMTP: `smtp.gmail.com:587`
- TLS habilitado
- Plantillas HTML con estilos inline

**¿Para qué sirve?**
Mantiene a los usuarios informados mediante emails automáticos.

---

### **PayPalService.java**
**Función:** Gestiona pagos de clientes con PayPal Checkout SDK

**Responsabilidades:**
- `crearOrden(Juego, returnUrl, cancelUrl)`: Crea orden de pago en PayPal
  - Configura item (nombre, descripción, precio)
  - Configura breakdown de montos
  - Configura URLs de retorno
  - Retorna `orderId`
- `capturarPago(orderId)`: Captura el pago tras aprobación del cliente
- `obtenerDetallesOrden(orderId)`: Obtiene info de una orden
- `esPaymentCompletado(Order)`: Verifica si el estado es "COMPLETED"
- `extraerMontoPagado(Order)`: Obtiene monto pagado
- `extraerJuegoId(Order)`: Obtiene ID del juego desde referenceId

**¿Para qué sirve?**
Procesa pagos de clientes mediante PayPal sin manejar datos de tarjetas directamente.

---

### **PayPalPayoutService.java**
**Función:** Envía pagos a proveedores con PayPal Payouts API

**Responsabilidades:**
- `enviarPagoProveedor(MovimientoProveedor, emailPayPal)`: Envía pago al proveedor
  - Valida monto mínimo ($1.00 USD)
  - Crea request de payout con SDK
  - Genera `sender_batch_id` único
  - Retorna `payout_batch_id` de PayPal

**Configuración:**
- Usa mismas credenciales que PayPal Checkout
- Modo Sandbox para desarrollo

**¿Para qué sirve?**
Automatiza el pago del 85% de las ventas a los proveedores.

---

### **CloudinaryService.java**
**Función:** Gestiona imágenes con Cloudinary CDN

**Responsabilidades:**
- `subirImagen(MultipartFile, folder)`: Sube imagen a Cloudinary
  - Valida tipo de archivo (solo imágenes)
  - Valida tamaño (máximo 5MB)
  - Retorna URL segura (HTTPS)
- `subirMultiplesImagenes(files[], folder)`: Sube varias imágenes
- `eliminarImagen(imageUrl)`: Elimina imagen por URL
  - Extrae `public_id` de la URL
  - Llama a API de Cloudinary
- `eliminarMultiplesImagenes(urls[])`: Elimina varias imágenes
- `esUrlCloudinary(url)`: Verifica si una URL es de Cloudinary

**¿Para qué sirve?**
Permite almacenar imágenes en la nube sin consumir espacio del servidor (actualmente no se usa, se guardan localmente).

---

## 📦 DTOs (`dto/`)

### **ApiResponse.java**
**Campos:**
- `success` (Boolean)
- `message` (String)
- `data` (Object)

**¿Para qué sirve?**
Respuesta estandarizada para endpoints REST API.

---

## 🔧 Tecnologías Utilizadas

### **Backend**
| Tecnología | Versión | Uso |
|------------|---------|-----|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 3.4.2 | Framework principal |
| **Spring Data JPA** | 3.4.2 | ORM para acceso a base de datos |
| **Spring Security** | 3.4.2 | Autenticación y autorización |
| **Spring Mail** | 3.4.2 | Envío de emails |
| **MySQL Connector** | 8.0.33 | Driver de MySQL |
| **Thymeleaf** | 3.4.2 | Motor de plantillas HTML |

### **Integraciones Externas**
| Servicio | SDK/Librería | Uso |
|----------|--------------|-----|
| **PayPal** | `com.paypal.sdk:checkout-sdk:2.0.0` | Pagos de clientes y proveedores |
| **Cloudinary** | `com.cloudinary:cloudinary-http45:1.39.0` | Almacenamiento de imágenes |
| **Google OAuth2** | `spring-boot-starter-oauth2-client` | Login con Google |
| **Gmail SMTP** | `spring-boot-starter-mail` | Envío de emails |

### **Base de Datos**
| Tecnología | Host | Uso |
|------------|------|-----|
| **MySQL** | Azure (`mysqldemojuan.mysql.database.azure.com`) | Base de datos principal |

### **Frontend**
| Tecnología | Versión/CDN | Uso |
|------------|-------------|-----|
| **Tailwind CSS** | CDN 3.4.1 | Framework CSS (mayoría de páginas) |
| **Font Awesome** | CDN 6.4.0 | Iconos |
| **CSS Custom** | login.css, register.css | Estilos personalizados para auth |
| **JavaScript Vanilla** | - | Interactividad (carrusel, dropdowns, modals) |

### **Build Tool**
| Herramienta | Versión | Uso |
|-------------|---------|-----|
| **Maven** | 3.x | Gestión de dependencias y build |

---

## 🔑 Variables de Entorno Requeridas

```properties
# Base de datos
MYSQL_URL=jdbc:mysql://mysqldemojuan.mysql.database.azure.com:3306/bd_pixel_shop
MYSQL_USERNAME=juanapm1234
MYSQL_PASSWORD=********

# Email (Gmail SMTP)
EMAIL_USERNAME=juanapmondragon@gmail.com
EMAIL_PASSWORD=******** (App Password)

# PayPal
PAYPAL_CLIENT_ID=********
PAYPAL_CLIENT_SECRET=********
PAYPAL_MODE=sandbox

# Cloudinary
CLOUDINARY_CLOUD_NAME=********
CLOUDINARY_API_KEY=********
CLOUDINARY_API_SECRET=********

# OAuth2 (Google)
GOOGLE_CLIENT_ID=********
GOOGLE_CLIENT_SECRET=********
```

---

## 📈 Flujo de Datos Principales

### **1. Registro de Usuario**
```
Usuario → AuthController.registrar()
       → PasswordEncoder.encode()
       → UsuarioRepository.save()
       → EmailService.enviarEmailBienvenida()
       → Redirect a /login
```

### **2. Compra de Juego**
```
Usuario → CompraController.comprarJuego()
       → Redirect a PayPalController.checkout()
       → PayPalService.crearOrden()
       → Usuario aprueba en PayPal
       → PayPalController.paymentSuccess()
       → PayPalService.capturarPago()
       → CompraController.confirmarCompra()
       → CompraRepository.save()
       → BibliotecaUsuarioRepository.save()
       → MovimientoProveedorRepository.save() (85% al proveedor)
       → EmailService.enviarConfirmacionCompra()
       → Redirect a /mi-biblioteca
```

### **3. Publicar Juego (Proveedor)**
```
Proveedor → ProveedorController.publicarJuego()
          → Validar 4 imágenes
          → Guardar imágenes en /static/images/juegos/
          → JuegoRepository.save()
          → PublicacionJuegoRepository.save() (25€)
          → Redirect a /proveedor/mis-juegos
```

### **4. Cobrar Ventas (Proveedor)**
```
Proveedor → ProveedorController.cobrarMovimiento()
          → Validar método de cobro
          → PayPalPayoutService.enviarPagoProveedor()
          → PayPal envía dinero al email del proveedor
          → MovimientoProveedorRepository.update(pagado=true)
          → Redirect a /proveedor/ventas
```

---

## 🎯 Resumen de Responsabilidades

| Capa | Responsabilidad | Ejemplos |
|------|----------------|----------|
| **Controllers** | Manejar peticiones HTTP, validaciones básicas, redireccionamientos | AuthController, CompraController |
| **Services** | Lógica de negocio, integraciones externas | PayPalService, EmailService |
| **Repositories** | Acceso a base de datos, queries personalizadas | UsuarioRepository, JuegoRepository |
| **Models** | Representar tablas de BD, relaciones JPA | Usuario, Juego, Compra |
| **Config** | Configuración de beans, seguridad | SecurityConfig, PayPalConfig |
| **Templates** | Vistas HTML con Thymeleaf | login.html, catalogo/index.html |

---

## ⚠️ Problemas Identificados (Ver PLAN_REORGANIZACION.md)

1. **Lógica de negocio en Controllers** - Debería estar en Services
2. **Tabla `biblioteca_usuario` redundante** - Se puede calcular desde `compra`
3. **Tabla `publicacion_juego` innecesaria** - Debería ser campo en `juego`
4. **Sin auditoría** - Faltan campos `created_at`, `updated_at`, `created_by`, `updated_by`
5. **Sin soft delete** - Eliminación física pierde datos históricos
6. **Imágenes como columnas** - Deberían estar en tabla separada `imagen_juego`

---

## 🚀 Siguientes Pasos

1. **Implementar features pendientes:**
   - Sistema de descargas de juegos (AWS S3)
   - Fotos de perfil con Cloudinary
   - Valoraciones y reseñas

2. **Refactorizar según PLAN_REORGANIZACION.md:**
   - Mover lógica a Services
   - Eliminar tablas redundantes
   - Implementar soft delete y auditoría

3. **Mejorar frontend:**
   - Convertir páginas de admin y proveedor a Tailwind CSS
   - Implementar modo oscuro

4. **Preparar para producción:**
   - Activar CSRF en SecurityConfig
   - Cambiar PayPal a modo Live
   - Configurar SSL/HTTPS
   - Implementar Redis cache
   - Configurar monitoring (Prometheus + Grafana)

---

**📄 Documentos Relacionados:**
- `FUNCIONALIDADES_FUTURAS.md` - Features pendientes (descargas, fotos de perfil)
- `DOCUMENTACION_COMPLETA_PROYECTO.md` - Documentación técnica detallada
- `PLAN_REORGANIZACION.md` - Plan de refactorización
- `MODELOS_BASE_DATOS_ALTERNATIVOS.md` - Diseños de BD alternativos
