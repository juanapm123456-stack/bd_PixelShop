# 📚 Documentación Completa del Proyecto - Pixel Shop

## 🎯 Concepto General

**Pixel Shop** es una plataforma digital de distribución de videojuegos inspirada en Steam y Epic Games, donde:
- Los **usuarios** pueden comprar y coleccionar juegos
- Los **proveedores** pueden publicar y vender sus juegos
- La **plataforma** (admin) gestiona el ecosistema y obtiene comisiones

---

## 🏗️ Arquitectura del Proyecto

### Stack Tecnológico
- **Backend**: Spring Boot 3.4.2 + Java 21
- **Frontend**: Thymeleaf + Tailwind CSS
- **Base de Datos**: MySQL en Azure
- **Seguridad**: Spring Security + OAuth2
- **Pagos**: PayPal Checkout SDK (modo Sandbox)
- **Email**: Gmail SMTP
- **Almacenamiento de imágenes**: Cloudinary
- **Despliegue**: Azure MySQL Database

### Patrón de Arquitectura
- **MVC** (Model-View-Controller)
- **Repository Pattern** para acceso a datos
- **Service Layer** (parcialmente implementado)
- **DTO Pattern** (presente en carpeta dto/)

---

## 👥 Roles y Permisos

### 1. CLIENTE
**Capacidades:**
- ✅ Ver catálogo completo de juegos
- ✅ Ver detalles de cada juego
- ✅ Comprar juegos (máximo 1 copia por juego)
- ✅ Ver su biblioteca de juegos comprados
- ✅ Ver historial de compras ("Mis Compras")
- ✅ Editar su perfil

**Restricciones:**
- ❌ No puede comprar el mismo juego dos veces
- ❌ No puede publicar juegos
- ❌ No puede acceder al panel de administración

**¿Por qué así?**
- Simula el comportamiento de Steam: un usuario típico solo compra y juega
- La restricción de "1 copia por juego" evita compras duplicadas innecesarias
- Se mantiene separación clara entre consumidores y creadores

---

### 2. PROVEEDOR
**Capacidades:**
- ✅ Todo lo que puede hacer un CLIENTE
- ✅ Publicar juegos nuevos (pagando 25€ por publicación)
- ✅ Ver lista de "Mis Juegos" publicados
- ✅ Editar información de sus juegos
- ✅ Ver estadísticas de ventas de cada juego
- ✅ Ver ingresos pendientes y cobrados
- ✅ Gestionar datos fiscales (CIF/NIF)
- ✅ Configurar email de PayPal para recibir pagos

**Restricciones:**
- ❌ No puede publicar el mismo juego dos veces
- ❌ No puede editar juegos de otros proveedores
- ❌ No puede acceder al panel de administración
- ❌ Debe pagar 25€ ANTES de publicar cada juego

**¿Por qué así?**
- Los proveedores también son gamers, por eso pueden comprar juegos
- El pago de 25€ por publicación genera ingresos para la plataforma y evita spam
- La separación proveedor/admin evita conflictos de interés
- Los datos fiscales son obligatorios para cumplir con regulaciones

---

### 3. ADMIN (Propietario de Pixel Shop)
**Capacidades:**
- ✅ Ver catálogo completo
- ✅ Gestionar todos los usuarios (ver datos, eliminar cuentas)
- ✅ Ver ganancias totales de la plataforma
- ✅ Ver movimientos económicos globales
- ✅ Ver estadísticas de ventas por juego
- ✅ Acceder a panel de administración

**Restricciones:**
- ❌ No puede comprar juegos
- ❌ No puede publicar juegos
- ❌ No puede ser proveedor ni cliente

**¿Por qué así?**
- El admin es el operador de la plataforma, no un usuario final
- Evita que el admin abuse comprando gratis o manipulando ventas
- Separación total entre gestión y participación en el mercado
- En un escenario real, habría múltiples admins con diferentes permisos

---

## 💰 Modelo de Negocio y Reglas Económicas

### Flujo de Dinero

#### 1. Publicación de Juegos (Proveedor → Plataforma)
```
Proveedor paga 25€ → Plataforma (100% de ganancia)
```
- **¿Cuándo?** Al publicar un juego por primera vez
- **¿Por qué?** Filtrar publicaciones de calidad, generar ingresos base
- **Registro:** Tabla `publicacion_juego`

#### 2. Venta de Juegos (Cliente → Plataforma → Proveedor)
```
Cliente paga 100% precio → Plataforma
Plataforma retiene 15% → Ganancias de la plataforma
Plataforma transfiere 85% → Proveedor
```
- **Ejemplo:** Juego a 50€
  - Cliente paga: 50€
  - Plataforma gana: 7.50€ (15%)
  - Proveedor recibe: 42.50€ (85%)

- **¿Por qué 15%?** 
  - Steam cobra ~30%
  - Epic Games cobra 12%
  - Pixel Shop toma 15% como punto medio competitivo

- **Registro:** Tabla `compra` + `movimiento_proveedor`

#### 3. Estados de Pago del Proveedor
```
PENDIENTE → El dinero está en la plataforma
PAGADO → El dinero se transfirió al proveedor
```
- Los proveedores ven ingresos "pendientes" y "cobrados"
- En la realidad, aquí se integraría PayPal Payouts API
- Actualmente es simulado (botón "Marcar como cobrado")

### Stock Ilimitado
- **Concepto:** Cada juego es digital, por lo tanto tiene stock infinito
- **Implementación:** No hay campo `stock` en la tabla `juego`
- **¿Por qué?** Los productos digitales no se agotan
- **Restricción:** 1 copia por usuario (para evitar duplicados)

---

## 🗄️ Estructura de Base de Datos

### Tabla: `usuario`
**Propósito:** Almacenar todos los usuarios del sistema

**Campos principales:**
- `id`: Identificador único
- `nombre`: Nombre completo
- `email`: Email (UNIQUE) - usado para login
- `password`: Contraseña encriptada con BCrypt
- `rol`: Enum (CLIENTE, PROVEEDOR, ADMIN)
- `datos_fiscales`: CIF/NIF (solo proveedores)
- `email_paypal`: Email de PayPal (solo proveedores)
- `fecha_registro`: Timestamp de creación

**¿Por qué un solo rol por usuario?**
- Simplifica la lógica de negocio
- Evita conflictos (ej: admin comprando juegos)
- En el futuro podría ser tabla separada `usuario_roles` (many-to-many)

**Restricciones:**
- Email UNIQUE: No dos usuarios con mismo email
- Rol NOT NULL: Todo usuario debe tener un rol
- Password encriptado: Nunca se guarda en texto plano

---

### Tabla: `juego`
**Propósito:** Catálogo de videojuegos disponibles

**Campos principales:**
- `id`: Identificador único
- `titulo`: Nombre del juego
- `descripcion`: Texto descriptivo
- `precio`: Decimal(10,2) - precio en euros
- `genero`: String (ej: "Acción", "RPG", "Estrategia")
- `imagen_url_1/2/3/4`: URLs de imágenes en Cloudinary
- `video_youtube_url`: URL de trailer/gameplay
- `proveedor_id`: FK a usuario (quien lo publicó)
- `fecha_publicacion`: Timestamp

**¿Por qué 4 imágenes separadas?**
- Imagen 1: Portada (usada en catálogo)
- Imágenes 2,3,4: Capturas de pantalla (usadas en carrusel de detalles)
- Alternativa mejor: Tabla `imagenes_juego` (one-to-many)

**¿Por qué video de YouTube y no subido?**
- YouTube maneja streaming, CDN, calidad adaptativa
- Subir videos propios requiere infraestructura costosa
- Los proveedores ya tienen trailers en YouTube

**Restricciones:**
- Precio >= 0: No precios negativos
- Proveedor_id NOT NULL: Todo juego tiene un dueño
- Título no vacío: Campo obligatorio

---

### Tabla: `compra`
**Propósito:** Registro de transacciones entre clientes y juegos

**Campos principales:**
- `id`: Identificador único
- `usuario_id`: FK a usuario (comprador)
- `juego_id`: FK a juego (comprado)
- `precio_pagado`: Decimal - precio en el momento de compra
- `fecha_compra`: Timestamp
- `metodo_pago`: String (ej: "PayPal", "Tarjeta")

**¿Por qué guardar precio_pagado?**
- El precio del juego puede cambiar en el futuro
- Necesitamos saber cuánto pagó realmente el cliente
- Importante para auditoría y cálculo de comisiones

**Restricción ÚNICA:**
```sql
UNIQUE(usuario_id, juego_id)
```
- Evita que un usuario compre el mismo juego dos veces
- Base de datos garantiza la integridad
- Validación adicional en backend

---

### Tabla: `biblioteca_usuario`
**Propósito:** Biblioteca de juegos que posee cada usuario

**Campos principales:**
- `id`: Identificador único
- `usuario_id`: FK a usuario
- `juego_id`: FK a juego
- `fecha_adquisicion`: Timestamp

**¿Por qué tabla separada si ya existe `compra`?**
- **Separación de responsabilidades:**
  - `compra`: Transacción económica (historial)
  - `biblioteca_usuario`: Posesión actual (colección)
- **Futuro:** Un juego podría llegar a la biblioteca sin compra (regalos, suscripciones)
- **Consultas optimizadas:** Más rápido buscar en biblioteca que en compras

**Restricción ÚNICA:**
```sql
UNIQUE(usuario_id, juego_id)
```

**Reflexión crítica:**
- ⚠️ Actualmente redundante con `compra`
- ✅ Si se planea: regalos, códigos promocionales, suscripciones → justificada
- ❌ Si no → eliminar y usar solo `compra`

---

### Tabla: `publicacion_juego`
**Propósito:** Registro del pago de 25€ por publicar un juego

**Campos principales:**
- `id`: Identificador único
- `proveedor_id`: FK a usuario (proveedor)
- `juego_id`: FK a juego (publicado)
- `fecha_publicacion`: Timestamp
- `monto_pagado`: Decimal (siempre 25€)

**¿Por qué existe?**
- Auditoría: Saber cuándo y quién pagó
- Ingresos de la plataforma: 25€ por cada publicación
- Evitar republica: Si existe registro, no puede volver a publicar

**Restricción ÚNICA:**
```sql
UNIQUE(proveedor_id, juego_id)
```

**¿Es realmente necesaria?**
- ✅ Si se quiere historial detallado de publicaciones
- ❌ Podría ser solo un campo en `juego`: `fecha_pago_publicacion`
- ⚠️ Decisión de diseño: ¿Granularidad vs simplicidad?

---

### Tabla: `movimiento_proveedor`
**Propósito:** Registro de ingresos de cada proveedor por ventas

**Campos principales:**
- `id`: Identificador único
- `proveedor_id`: FK a usuario (proveedor)
- `juego_id`: FK a juego (vendido)
- `compra_id`: FK a compra (transacción)
- `monto`: Decimal - 85% del precio de venta
- `estado`: Enum (PENDIENTE, PAGADO)
- `fecha_venta`: Timestamp
- `fecha_pago`: Timestamp (nullable)

**¿Por qué existe?**
- Rastrear cuánto dinero debe la plataforma a cada proveedor
- Separar "pendiente" de "ya cobrado"
- En producción: Integrar con PayPal Payouts API

**Flujo:**
1. Cliente compra juego → Se crea `compra`
2. Sistema calcula 85% del precio → Se crea `movimiento_proveedor` (PENDIENTE)
3. Admin/sistema paga al proveedor → Estado cambia a PAGADO

**Cálculo:**
```
monto = precio_pagado * 0.85
comision_plataforma = precio_pagado * 0.15
```

---

## 🔐 Seguridad Implementada

### 1. Autenticación

#### Login Tradicional (Email + Password)
- **Clase:** `UserDetailsServiceImpl`
- **Proceso:**
  1. Usuario ingresa email y password
  2. Spring Security busca usuario en BD
  3. Compara password encriptado con BCrypt
  4. Si coincide → Crea sesión
  5. Si no → Rechaza acceso

**¿Por qué BCrypt?**
- Hash unidireccional (no se puede desencriptar)
- Salt automático (cada hash es único)
- Resistente a ataques de fuerza bruta
- Estándar de la industria

#### OAuth2 con Google
- **Clase:** `CustomOAuth2UserService`
- **Proceso:**
  1. Usuario hace clic en "Iniciar sesión con Google"
  2. Redirige a Google OAuth2
  3. Usuario autoriza en Google
  4. Google retorna token y email
  5. Sistema busca usuario por email
  6. Si existe → Inicia sesión
  7. Si no existe → Crea cuenta automáticamente con rol CLIENTE

**¿Por qué OAuth2?**
- Comodidad: No recordar otra contraseña
- Seguridad: Google maneja la autenticación
- Registro rápido: 1 click vs formulario completo

**Limitación actual:**
- No se puede elegir rol al registrarse con Google
- Siempre crea CLIENTE
- Solución futura: Página intermedia "Elige tu rol"

---

### 2. Autorización (Control de Acceso)

#### Spring Security Rules
```java
// Rutas públicas
permitAll(): /, /login, /register, /oauth2/**

// Solo autenticados
authenticated(): /perfil/**, /mi-biblioteca

// Por rol
hasRole("CLIENTE"): /compra/**
hasRole("PROVEEDOR"): /proveedor/**
hasRole("ADMIN"): /admin/**
```

#### Validaciones en Backend
**CompraController - Ejemplo:**
```
1. Usuario autenticado? → Sino, redirige a login
2. Juego existe? → Sino, error 404
3. Ya lo compró? → Sino, error "Ya tienes este juego"
4. Es su propio juego? → Sino, error "No puedes comprar tu propio juego"
5. Todo OK → Procede con PayPal
```

**¿Por qué validar en backend?**
- Frontend puede ser manipulado
- URLs pueden ser accedidas directamente
- Seguridad en profundidad (defense in depth)

---

### 3. Protección CSRF
- **Activado por defecto** en Spring Security
- Token CSRF en formularios Thymeleaf (automático)
- Evita ataques de Cross-Site Request Forgery

---

### 4. SQL Injection
- **Prevención:** Spring Data JPA usa **PreparedStatements**
- No hay concatenación manual de SQL
- Todos los repositorios usan métodos tipo `findById()`, `save()`, etc.

---

## 💳 Integración de PayPal

### Configuración
- **Modo:** Sandbox (entorno de pruebas)
- **SDK:** PayPal Checkout SDK 2.0.0
- **Variables de entorno:**
  - `PAYPAL_CLIENT_ID`: ID de aplicación PayPal
  - `PAYPAL_CLIENT_SECRET`: Secreto de aplicación

### Flujo de Compra

#### 1. Usuario hace clic en "Comprar Ahora"
- **Endpoint:** `POST /compra/{juegoId}`
- **Validaciones:**
  - Usuario autenticado
  - Juego existe
  - No lo compró antes
  - No es su propio juego (si es proveedor)
- **Acción:** Redirige a `/paypal/checkout/{juegoId}`

#### 2. Página de Checkout PayPal
- **Vista:** `checkout/payment-options.html`
- **Carga:** SDK de PayPal dinámicamente
- **Renderiza:** Botones "PayPal" y "Tarjeta de débito/crédito"

#### 3. Usuario selecciona método de pago
- **JavaScript:** Llama a `/paypal/create-order/{juegoId}`
- **Backend:**
  1. Crea orden en PayPal con precio del juego
  2. PayPal retorna `orderId`
  3. JavaScript recibe `orderId`

#### 4. Usuario completa pago en ventana PayPal
- PayPal procesa el pago
- Si aprueba → JavaScript llama a `onApprove()`
- Si cancela → JavaScript llama a `onCancel()`

#### 5. Captura de pago
- **JavaScript:** Redirige a `/paypal/success?token={orderId}&juegoId={id}`
- **Backend:**
  1. Captura orden en PayPal (cobro real)
  2. Verifica estado `COMPLETED`
  3. Crea registro en tabla `compra`
  4. Añade juego a `biblioteca_usuario`
  5. Calcula 85% para proveedor
  6. Crea registro en `movimiento_proveedor` (PENDIENTE)
  7. Redirige a biblioteca con mensaje "¡Compra exitosa!"

### ¿Por qué PayPal?
- ✅ Muy usado globalmente
- ✅ SDK robusto y bien documentado
- ✅ Modo Sandbox para pruebas sin dinero real
- ✅ Soporte para tarjetas sin cuenta PayPal
- ❌ Comisión ~3.4% + 0.35€ por transacción

### Limitaciones actuales
- Solo PayPal Checkout (pago inmediato)
- No implementado PayPal Payouts (pagar a proveedores)
- No hay webhook para confirmación asíncrona
- No hay manejo de reembolsos

---

## 📧 Sistema de Emails

### Configuración
- **Servidor:** Gmail SMTP (smtp.gmail.com:587)
- **Autenticación:** Email + App Password (no password normal)
- **Variables:**
  - `EMAIL_USERNAME`: Email de la plataforma
  - `EMAIL_PASSWORD`: Contraseña de aplicación de Gmail

### Emails Enviados

#### 1. Registro de usuario
- **Cuándo:** Usuario completa formulario de registro
- **Destinatario:** Email del nuevo usuario
- **Contenido:**
  - Bienvenida a Pixel Shop
  - Resumen de funcionalidades según rol
  - Link al catálogo

#### 2. Compra de juego (Cliente)
- **Cuándo:** Pago exitoso en PayPal
- **Destinatario:** Email del comprador
- **Contenido:**
  - Confirmación de compra
  - Nombre del juego
  - Precio pagado
  - Link a biblioteca

#### 3. Venta de juego (Proveedor)
- **Cuándo:** Otro usuario compra su juego
- **Destinatario:** Email del proveedor
- **Contenido:**
  - Notificación de venta
  - Nombre del juego
  - Monto ganado (85%)
  - Link a panel de ventas

### ¿Por qué Gmail SMTP?
- ✅ Gratis hasta 500 emails/día
- ✅ Confiable y rápido
- ✅ Fácil configuración
- ❌ Limitado para producción (usar SendGrid, AWS SES, etc.)

---

## 🖼️ Gestión de Imágenes con Cloudinary

### Configuración
- **Servicio:** Cloudinary CDN
- **Variables:**
  - `CLOUDINARY_CLOUD_NAME`
  - `CLOUDINARY_API_KEY`
  - `CLOUDINARY_API_SECRET`

### Uso Actual
- **Subida de imágenes de juegos:** Proveedor sube hasta 4 imágenes
- **Transformaciones:** Automáticas (resize, compress, webp)
- **Almacenamiento:** En la nube, URLs permanentes
- **CDN:** Carga rápida desde cualquier parte del mundo

### Flujo de Subida
1. Proveedor selecciona imagen en formulario
2. Frontend envía MultipartFile a backend
3. `CloudinaryService.uploadImage()` sube a Cloudinary
4. Cloudinary retorna URL pública
5. Backend guarda URL en campo `imagen_url_X`

### ¿Por qué Cloudinary y no almacenamiento local?
- ✅ No ocupa espacio en servidor
- ✅ CDN global (carga rápida)
- ✅ Transformaciones automáticas
- ✅ Backup y disponibilidad garantizados
- ❌ Plan gratuito: 25GB de almacenamiento, 25GB de ancho de banda/mes

---

## 🎨 Frontend: Thymeleaf + Tailwind CSS

### Decisión de Tecnología

#### ¿Por qué Thymeleaf?
- ✅ Renderizado en servidor (SEO-friendly)
- ✅ Integración nativa con Spring Boot
- ✅ Sintaxis HTML natural
- ✅ Sin necesidad de API REST separada
- ❌ Menos interactivo que frameworks SPA (React, Vue)

#### ¿Por qué Tailwind CSS?
- ✅ Utility-first: Rápido desarrollo
- ✅ No hay conflictos de nombres de clases
- ✅ Responsive por defecto
- ✅ CDN simple (no requiere build)
- ❌ HTML más verboso

#### Migración Bootstrap → Tailwind
- **Antes:** Proyecto usaba Bootstrap 5
- **Problema:** Conflictos de estilos, diseño genérico
- **Solución:** Conversión completa a Tailwind CSS
- **Estado actual:**
  - ✅ Login/Register: CSS personalizado (diseño único)
  - ✅ Catálogo, Biblioteca, Perfil, Compras: Tailwind
  - ✅ Checkout: Tailwind
  - ⚠️ Admin y Proveedor: Pendiente de conversión

### Componentes Reutilizables

#### 1. Header/Navbar (`fragments/header.html`)
- Logo de Pixel Shop
- Barra de búsqueda
- Menú dinámico según rol:
  - **No autenticado:** Catálogo, Login, Register
  - **Cliente:** Catálogo, Mi Biblioteca, Mis Compras, Perfil
  - **Proveedor:** + Publicar Juego, Mis Juegos, Ventas
  - **Admin:** + Usuarios, Ganancias, Movimientos
- Dropdown de usuario con foto (futuro)

#### 2. Footer (`fragments/footer.html`)
- Copyright Pixel Shop 2025
- Créditos tecnológicos
- Siempre al final (gracias a Flexbox en body)

#### 3. Head común
- Título dinámico
- Tailwind CSS CDN
- Font Awesome (iconos)
- Prism.js (syntax highlighting)

### Páginas Principales

#### Catálogo (`catalogo/index.html`)
- Grid responsive (1-4 columnas)
- Cards de juegos con:
  - Imagen portada
  - Título
  - Descripción (truncada)
  - Precio
  - Botón "Ver Detalles"
  - Badge "En tu biblioteca" (si ya lo tiene)

#### Detalles de Juego (`catalogo/juego-detalle.html`)
- Carrusel de imágenes (JavaScript vanilla)
- Video de YouTube embebido
- Información del juego
- Precio destacado
- Botón "Comprar Ahora" (si no lo tiene)
- Botón "Ya tienes este juego" (deshabilitado si lo tiene)

#### Mi Biblioteca (`usuario/mi-biblioteca.html`)
- Grid de juegos comprados
- Fecha de adquisición
- Botón "Ver Detalles"

#### Mis Compras (`usuario/mis-compras.html`)
- Tabla con historial:
  - ID de compra
  - Nombre del juego
  - Fecha y hora
  - Precio pagado
  - Botón "Ver juego"

---

## 🔄 Flujos Completos de Usuario

### Flujo 1: Registro y Primer Login

#### Opción A: Registro Manual
1. Usuario va a `/register`
2. Completa formulario:
   - Nombre completo
   - Email
   - Password (mínimo 6 caracteres)
   - Rol (CLIENTE o PROVEEDOR)
   - Datos fiscales (si es proveedor)
3. Backend valida:
   - Email no existe en BD
   - Campos no vacíos
   - Password encriptado
4. Guarda usuario en BD
5. Envía email de bienvenida
6. Redirige a `/login` con mensaje "Registro exitoso"
7. Usuario inicia sesión

#### Opción B: Registro con Google OAuth2
1. Usuario hace clic en "Iniciar sesión con Google"
2. Redirige a Google
3. Autoriza acceso a email y perfil
4. Google retorna a `/login/oauth2/code/google`
5. Backend:
   - Busca usuario por email de Google
   - Si NO existe:
     - Crea usuario con rol CLIENTE
     - Password = NULL (no lo necesita)
     - Envía email de bienvenida
   - Si existe:
     - Inicia sesión normal
6. Redirige a catálogo

---

### Flujo 2: Publicar un Juego (Proveedor)

1. Proveedor va a `/proveedor/publicar`
2. Completa formulario:
   - Título del juego
   - Descripción
   - Precio (ej: 29.99€)
   - Género
   - 4 imágenes (URLs de Cloudinary)
   - Video de YouTube (opcional)
3. Backend valida:
   - Usuario es PROVEEDOR
   - Título no vacío
   - Precio > 0
   - Imágenes válidas
4. Simula pago de 25€ (en producción sería con PayPal)
5. Guarda juego en BD con `proveedor_id`
6. Crea registro en `publicacion_juego`
7. Envía email de confirmación
8. Redirige a "Mis Juegos" con mensaje "Juego publicado con éxito"

**¿Por qué se paga antes de publicar?**
- Evita spam de publicaciones
- Genera ingresos para la plataforma
- Solo proveedores serios publicarán

---

### Flujo 3: Comprar un Juego (Cliente)

1. Cliente navega catálogo
2. Encuentra juego interesante
3. Click en "Ver Detalles"
4. Lee descripción, ve imágenes y video
5. Click en "Comprar Ahora"
6. Sistema valida:
   - Usuario autenticado
   - No lo compró antes
   - No es su propio juego (si es proveedor)
7. Redirige a `/paypal/checkout/{juegoId}`
8. Página de PayPal:
   - Muestra resumen del juego
   - Carga botones de PayPal
9. Cliente hace clic en método de pago
10. Ventana de PayPal se abre
11. Cliente completa pago:
    - Inicia sesión en PayPal
    - Confirma pago
12. PayPal procesa transacción
13. Backend captura pago:
    - Verifica estado `COMPLETED`
    - Crea `compra`
    - Añade a `biblioteca_usuario`
    - Calcula comisiones
    - Crea `movimiento_proveedor` (PENDIENTE)
14. Envía emails:
    - Cliente: "¡Compra exitosa!"
    - Proveedor: "Vendiste tu juego"
15. Redirige a "Mi Biblioteca"
16. Cliente ve su juego nuevo

---

### Flujo 4: Ver Ventas (Proveedor)

1. Proveedor va a `/proveedor/ventas`
2. Sistema muestra lista de juegos publicados
3. Para cada juego:
   - Total de ventas
   - Ingresos totales (85%)
   - Ingresos pendientes
   - Ingresos cobrados
4. Proveedor ve detalle de movimientos:
   - Fecha de venta
   - Comprador (ocultado por privacidad)
   - Monto ganado
   - Estado (PENDIENTE/PAGADO)
5. Proveedor marca ingresos como "Cobrados"
   - En producción: PayPal Payouts API transferiría el dinero
   - Actualmente: Solo cambia estado

---

### Flujo 5: Gestión de Usuarios (Admin)

1. Admin va a `/admin/usuarios`
2. Sistema muestra tabla de todos los usuarios:
   - Nombre
   - Email
   - Rol
   - Fecha de registro
3. Admin puede:
   - Ver detalles de cada usuario
   - Ver juegos comprados (clientes)
   - Ver juegos publicados (proveedores)
   - Ver historial de compras/ventas
   - **Eliminar cuenta permanentemente**
4. Al eliminar:
   - Confirma con mensaje de advertencia
   - Borra usuario de BD (CASCADE elimina compras/biblioteca)
   - En producción: Debería archivar en lugar de eliminar

---

## 🚨 Validaciones y Restricciones Implementadas

### En Base de Datos (Constraints)
```sql
-- Email único
UNIQUE(email) en tabla usuario

-- Una copia por usuario
UNIQUE(usuario_id, juego_id) en tabla compra
UNIQUE(usuario_id, juego_id) en tabla biblioteca_usuario

-- Una publicación por juego por proveedor
UNIQUE(proveedor_id, juego_id) en tabla publicacion_juego

-- No precios negativos
CHECK(precio >= 0) en tabla juego

-- Llaves foráneas con DELETE CASCADE
ON DELETE CASCADE en todas las FK
```

### En Backend (Service/Controller)
- ✅ Usuario no puede comprar su propio juego
- ✅ Usuario no puede comprar el mismo juego dos veces
- ✅ Proveedor no puede publicar el mismo juego dos veces
- ✅ Solo usuarios autenticados pueden comprar
- ✅ Solo proveedores pueden publicar
- ✅ Solo admins pueden eliminar usuarios
- ✅ Verificación de pago exitoso en PayPal antes de registrar compra

### En Frontend (JavaScript/HTML)
- ✅ Campos requeridos en formularios
- ✅ Validación de formato de email
- ✅ Password mínimo 6 caracteres
- ✅ Precio debe ser número positivo
- ✅ Confirmación antes de eliminar cuenta
- ✅ Doble confirmación para eliminación de cuenta

---

## 🤔 Reflexión: ¿Cómo Mejorar la Organización?

### Problemas Actuales

#### 1. **Lógica de Negocio en Controllers**
**Problema:** Muchos controllers tienen lógica compleja
```java
// Ejemplo: CompraController tiene:
- Validación de compra duplicada
- Cálculo de comisiones
- Envío de emails
- Creación de múltiples registros
```
**Solución:** Mover toda esta lógica a `CompraService`

#### 2. **Falta de Capa de Servicio Consistente**
**Problema:** Algunos controllers llaman directamente a repositories
**Solución:** SIEMPRE usar servicios como intermediarios
```
Controller → Service → Repository
```

#### 3. **Tabla `biblioteca_usuario` Redundante**
**Problema:** Duplica información de `compra`
**Solución:**
- **Opción A:** Eliminar `biblioteca_usuario`, usar query en `compra`
- **Opción B:** Mantener si se planea agregar regalos/suscripciones

#### 4. **Tabla `publicacion_juego` Innecesaria**
**Problema:** Solo guarda el pago de 25€
**Solución:** Campo `fecha_pago_publicacion` en tabla `juego`

#### 5. **Atributos Dispersos**
**Problema:** `datos_fiscales` y `email_paypal` en tabla `usuario`
**Solución:** Tabla separada `proveedor_datos` (one-to-one)

#### 6. **Múltiples Imágenes en Columnas**
**Problema:** `imagen_url_1`, `imagen_url_2`, `imagen_url_3`, `imagen_url_4`
**Solución:** Tabla `imagenes_juego` (one-to-many)

#### 7. **Sin Auditoría Completa**
**Problema:** No se registra quién modificó qué y cuándo
**Solución:** Campos `created_by`, `updated_by`, `created_at`, `updated_at` en todas las tablas

#### 8. **Falta de Soft Delete**
**Problema:** Eliminar usuario borra todo (pérdida de datos)
**Solución:** Campo `deleted_at` (soft delete) o tabla `usuarios_archivados`

---

## 📈 Métricas y Escalabilidad

### Limitaciones Actuales
- **Cloudinary Free:** 25GB/mes → suficiente para ~500 juegos
- **Gmail SMTP:** 500 emails/día → suficiente para ~100 compras/día
- **PayPal Sandbox:** Solo pruebas, no dinero real
- **MySQL Azure:** Plan básico, ~100 conexiones concurrentes

### Cuellos de Botella Futuros
1. **Imágenes:** Si cada juego pesa 5MB x 4 imágenes = 20MB → 1250 juegos = 25GB
2. **Emails:** Si 1000 ventas/día → necesita SendGrid/AWS SES
3. **Base de Datos:** Si millones de compras → necesita índices, particionamiento
4. **PayPal:** Comisión 3.4% + 0.35€ por transacción → considerar Stripe

### Mejoras para Producción
- ✅ Migrar a PayPal Production (dinero real)
- ✅ Implementar PayPal Payouts (pagar a proveedores automáticamente)
- ✅ Webhooks de PayPal (confirmación asíncrona)
- ✅ CDN para assets estáticos (Tailwind CSS local, no CDN)
- ✅ Redis para caché de catálogo
- ✅ Elasticsearch para búsqueda avanzada
- ✅ Monitoreo con Prometheus + Grafana
- ✅ Logs centralizados (ELK Stack)
- ✅ CI/CD con GitHub Actions
- ✅ Testing: Unit tests + Integration tests

---

## 🎯 Conclusión

### Fortalezas del Proyecto
✅ **Arquitectura MVC clara**
✅ **Seguridad robusta** (Spring Security + OAuth2)
✅ **Integraciones funcionando** (PayPal, Gmail, Cloudinary)
✅ **Separación de roles bien definida**
✅ **Modelo de negocio realista**
✅ **Frontend moderno con Tailwind CSS**

### Áreas de Mejora
⚠️ **Reorganizar capa de servicio** (menos lógica en controllers)
⚠️ **Eliminar tablas redundantes** (biblioteca_usuario, publicacion_juego)
⚠️ **Normalizar estructura de imágenes** (tabla separada)
⚠️ **Agregar auditoría completa** (created_at, updated_at, etc.)
⚠️ **Implementar soft delete** (no borrar datos reales)
⚠️ **Testing automatizado** (actualmente sin tests)

### Siguiente Nivel
🚀 **Descarga de juegos** (archivos ZIP con AWS S3)
🚀 **Foto de perfil** (con Cloudinary)
🚀 **Sistema de valoraciones** (estrellas + reseñas)
🚀 **Wishlist** (lista de deseados)
🚀 **Reembolsos** (dentro de 14 días)
🚀 **Suscripciones** (acceso a catálogo por 9.99€/mes)
🚀 **Códigos promocionales** (descuentos)
🚀 **Regalos** (enviar juego a otro usuario)

**Pixel Shop es una base sólida que simula correctamente el funcionamiento de una plataforma de distribución digital. Con algunas mejoras en organización y funcionalidades adicionales, podría ser un producto comercial real.** 🎮🚀
