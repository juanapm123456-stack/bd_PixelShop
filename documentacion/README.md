# 📚 PixelShop - Documentación del Sistema

**Plataforma de Venta de Videojuegos Digital**

---

## 📋 Tabla de Contenidos

1. [Resumen del Proyecto](#-resumen-del-proyecto)
2. [Arquitectura del Sistema](#-arquitectura-del-sistema)
3. [Tecnologías Utilizadas](#-tecnologías-utilizadas)
4. [Roles de Usuario](#-roles-de-usuario)
5. [Modelo de Negocio](#-modelo-de-negocio)
6. [Estructura del Proyecto](#-estructura-del-proyecto)
7. [Guías Técnicas](#-guías-técnicas)
8. [Cómo Ejecutar](#-cómo-ejecutar)

---

## 🎮 Resumen del Proyecto

**PixelShop** es una plataforma web de comercio electrónico especializada en la venta de videojuegos digitales. Permite a los proveedores publicar sus juegos y a los clientes comprarlos, mientras la plataforma gestiona las transacciones y comisiones.

### Características Principales

✅ **Sistema de Roles**: ADMIN, PROVEEDOR, CLIENTE  
✅ **Gestión de Catálogo**: Publicación y edición de juegos  
✅ **Sistema de Compras**: Compra digital con biblioteca personal  
✅ **Comisiones Automáticas**: 85% proveedor / 15% plataforma  
✅ **Panel Administrativo**: Gestión de usuarios y visualización de ganancias  
✅ **Seguridad**: Spring Security con BCrypt  

---

## 🏗️ Arquitectura del Sistema

### Patrón de Diseño: MVC + Service Layer

```
┌─────────────────────────────────────────────┐
│             CAPA DE VISTA                   │
│         (Thymeleaf Templates)               │
│   login.html, catalogo/index.html, etc.    │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│          CAPA DE CONTROLADORES              │
│  AuthController, CompraController, etc.     │
└────────────────┬────────────────────────────┘
                 │ Inyecta Interfaces
┌────────────────▼────────────────────────────┐
│         CAPA DE SERVICIOS (Interfaces)      │
│  IServicioUsuario, IServicioCompra, etc.    │
└────────────────┬────────────────────────────┘
                 │ Implementadas por
┌────────────────▼────────────────────────────┐
│    CAPA DE SERVICIOS (Implementaciones)     │
│  ServicioUsuario, ServicioCompra, etc.      │
└────────────────┬────────────────────────────┘
                 │ Usa
┌────────────────▼────────────────────────────┐
│         CAPA DE REPOSITORIOS (JPA)          │
│  UsuarioRepository, CompraRepository, etc.  │
└────────────────┬────────────────────────────┘
                 │ Accede a
┌────────────────▼────────────────────────────┐
│          BASE DE DATOS (MySQL)              │
│    Tablas: usuarios, juegos, compras...     │
└─────────────────────────────────────────────┘
```

### ¿Por qué usamos Interfaces + Implementaciones?

**Patrón Estándar de Spring**: Separamos **QUÉ** hace el servicio (interfaz) de **CÓMO** lo hace (implementación).

**Ventajas:**
- ✅ Cumple el principio SOLID de Inversión de Dependencias
- ✅ Facilita crear tests unitarios con mocks
- ✅ Permite cambiar la implementación sin tocar los controladores
- ✅ Mejora la mantenibilidad y escalabilidad del código

---

## 💻 Tecnologías Utilizadas

### Backend
- **Java 21**: Lenguaje de programación
- **Spring Boot 3.x**: Framework principal
- **Spring Data JPA**: Persistencia con Hibernate
- **Spring Security**: Autenticación y autorización
- **BCrypt**: Encriptación de contraseñas

### Frontend
- **Thymeleaf**: Motor de plantillas
- **HTML5 + CSS3**: Estructura y estilos
- **JavaScript**: Interactividad (animaciones, validaciones)

### Base de Datos
- **MySQL**: Sistema de gestión de base de datos relacional

### Build Tool
- **Maven**: Gestión de dependencias y compilación

---

## 👥 Roles de Usuario

### 1. ADMIN (Administrador)
**Responsabilidades:**
- ✅ Gestionar usuarios (activar/desactivar)
- ✅ Ver lista completa de usuarios
- ✅ Ver ganancias de la plataforma
- ✅ Ver todos los movimientos financieros
- ❌ **NO puede comprar juegos**

**Acceso:**
- `/admin/usuarios` - Gestión de usuarios
- `/admin/ganancias` - Dashboard de ganancias
- `/admin/movimientos` - Historial de transacciones

---

### 2. PROVEEDOR (Publisher/Developer)
**Responsabilidades:**
- ✅ Publicar nuevos juegos (costo: 25€)
- ✅ Editar sus juegos publicados
- ✅ Ver sus ventas y movimientos financieros
- ✅ Comprar juegos de otros proveedores
- ✅ Recibir el 85% del precio de cada venta

**Acceso:**
- `/proveedor/publicar` - Publicar nuevo juego
- `/proveedor/mis-juegos` - Gestionar juegos publicados
- `/proveedor/ventas` - Ver ingresos y movimientos

**Reglas de Negocio:**
- Paga 25€ por publicar un juego
- Recibe 85% del precio cuando se vende su juego
- Puede comprar juegos de otros proveedores

---

### 3. CLIENTE (Customer)
**Responsabilidades:**
- ✅ Ver catálogo de juegos
- ✅ Comprar juegos
- ✅ Ver su biblioteca personal
- ✅ Ver historial de compras
- ✅ Gestionar su perfil

**Acceso:**
- `/catalogo` - Catálogo de juegos
- `/usuario/mi-biblioteca` - Juegos comprados
- `/usuario/mis-compras` - Historial de compras
- `/usuario/perfil` - Editar perfil

**Reglas de Negocio:**
- No puede comprar el mismo juego dos veces
- Los juegos comprados se añaden automáticamente a su biblioteca

---

## 💰 Modelo de Negocio

### Sistema de Comisiones

```
Venta de un juego de 100€
├─ 85€ → PROVEEDOR (85%)
└─ 15€ → PLATAFORMA (15%)
```

### Flujo de Dinero

#### 1. Publicación de Juego
```
Proveedor paga 25€ → PLATAFORMA
```

#### 2. Venta de Juego
```
Cliente paga 100€
├─ 85€ → Movimiento del PROVEEDOR (pendiente)
└─ 15€ → Ganancia de la PLATAFORMA
```

#### 3. Ganancias de la Plataforma
```
Ganancias Totales = (Comisiones de Ventas) + (Publicaciones)
                  = (15% × Total Ventas) + (25€ × Número Publicaciones)
```

### Ejemplo Práctico

**Escenario:**
- 10 juegos publicados = 10 × 25€ = **250€**
- 50 ventas totales de 1000€ = 15% × 1000€ = **150€**
- **Ganancias Totales = 250€ + 150€ = 400€**

---

## 📁 Estructura del Proyecto

```
spring_crud_thymeleaf_v21/
│
├── src/main/java/com/example/
│   ├── App.java                          # Clase principal
│   │
│   ├── config/
│   │   └── SecurityConfig.java           # Configuración Spring Security
│   │
│   ├── controller/                       # Controladores MVC
│   │   ├── AuthController.java           # Login/Registro
│   │   ├── CatalogoController.java       # Catálogo público
│   │   ├── CompraController.java         # Proceso de compra
│   │   ├── PerfilController.java         # Gestión de perfil
│   │   ├── AdminController.java          # Panel admin
│   │   ├── ProveedorController.java      # Panel proveedor
│   │   └── BibliotecaController.java     # Biblioteca usuario
│   │
│   ├── service/                          # Capa de servicios
│   │   ├── IServicioUsuario.java         # Interfaz usuarios
│   │   ├── ServicioUsuario.java          # Implementación usuarios
│   │   ├── IServicioJuego.java           # Interfaz juegos
│   │   ├── ServicioJuego.java            # Implementación juegos
│   │   ├── IServicioCompra.java          # Interfaz compras
│   │   ├── ServicioCompra.java           # Implementación compras
│   │   ├── IServicioProveedor.java       # Interfaz proveedores
│   │   ├── ServicioProveedor.java        # Implementación proveedores
│   │   ├── IServicioAdministrador.java   # Interfaz admin
│   │   ├── ServicioAdministrador.java    # Implementación admin
│   │   ├── IServicioBiblioteca.java      # Interfaz biblioteca
│   │   └── ServicioBiblioteca.java       # Implementación biblioteca
│   │
│   ├── model/                            # Entidades JPA
│   │   ├── Usuario.java                  # Entidad usuario
│   │   ├── Juego.java                    # Entidad juego
│   │   ├── Compra.java                   # Entidad compra
│   │   ├── BibliotecaUsuario.java        # Entidad biblioteca
│   │   ├── MovimientoProveedor.java      # Entidad movimientos
│   │   ├── PublicacionJuego.java         # Entidad publicaciones
│   │   └── Rol.java                      # Enum roles
│   │
│   └── repository/                       # Repositorios JPA
│       ├── UsuarioRepository.java
│       ├── JuegoRepository.java
│       ├── CompraRepository.java
│       ├── BibliotecaUsuarioRepository.java
│       ├── MovimientoProveedorRepository.java
│       └── PublicacionJuegoRepository.java
│
├── src/main/resources/
│   ├── application.properties            # Configuración app
│   ├── templates/                        # Vistas Thymeleaf
│   │   ├── auth/
│   │   │   ├── login.html
│   │   │   └── register.html
│   │   ├── catalogo/
│   │   │   ├── index.html
│   │   │   └── juego-detalle.html
│   │   ├── usuario/
│   │   │   ├── mi-biblioteca.html
│   │   │   ├── mis-compras.html
│   │   │   └── perfil.html
│   │   ├── proveedor/
│   │   │   ├── publicar-juego.html
│   │   │   ├── mis-juegos.html
│   │   │   ├── editar-juego.html
│   │   │   └── ventas.html
│   │   ├── admin/
│   │   │   ├── usuarios.html
│   │   │   ├── ganancias.html
│   │   │   └── movimientos.html
│   │   └── fragments/
│   │       ├── header.html
│   │       └── footer.html
│   │
│   └── static/                           # Recursos estáticos
│       ├── css/                          # Hojas de estilo
│       ├── js/                           # JavaScript
│       └── images/juegos/                # Imágenes de juegos
│
├── documentacion/                        # Documentación técnica
│   ├── README.md                         # Este archivo
│   ├── ARQUITECTURA.md                   # Arquitectura detallada
│   ├── SERVICIOS.md                      # Guía de servicios
│   ├── MODELO_NEGOCIO.md                 # Sistema de comisiones
│   └── SEGURIDAD.md                      # Spring Security
│
└── pom.xml                               # Dependencias Maven
```

---

## 📖 Guías Técnicas

### Documentación Disponible

1. **[ARQUITECTURA.md](ARQUITECTURA.md)**
   - Patrón Interfaz + Implementación
   - Explicación de cada capa
   - Flujo de datos completo
   - Principios SOLID aplicados

2. **[SERVICIOS.md](SERVICIOS.md)**
   - Lista de todos los servicios
   - Métodos de cada servicio con ejemplos
   - Reglas de negocio implementadas
   - Casos de uso

3. **[MODELO_NEGOCIO.md](MODELO_NEGOCIO.md)**
   - Sistema de comisiones explicado
   - Cálculo de ganancias
   - Flujo de pagos completo
   - Ejemplos numéricos

4. **[SEGURIDAD.md](SEGURIDAD.md)**
   - Configuración de Spring Security
   - Sistema de autenticación
   - Control de acceso por roles
   - Encriptación de contraseñas

---

## 🚀 Cómo Ejecutar

### Requisitos Previos
- ☕ Java 21 o superior
- 🐬 MySQL 8.0 o superior
- 📦 Maven 3.6 o superior

### 1. Configurar Base de Datos

```sql
CREATE DATABASE pixelshop;
```

### 2. Configurar `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pixelshop
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
```

### 3. Compilar y Ejecutar

```bash
# Limpiar y compilar
mvn clean compile

# Ejecutar aplicación
mvn spring-boot:run
```

### 4. Acceder a la Aplicación

```
http://localhost:8080
```

### 5. Crear Usuario Administrador (Primera vez)

Registra un usuario y manualmente cambia su rol en la base de datos:

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE email = 'admin@pixelshop.com';
```

---

## 🎯 Endpoints Principales

### Públicos (Sin autenticación)
- `GET /` → Redirige al login
- `GET /login` → Página de login
- `GET /registro` → Página de registro
- `POST /login` → Procesar login
- `POST /registro` → Procesar registro

### Catálogo (Autenticados)
- `GET /catalogo` → Ver catálogo de juegos
- `GET /juego/{id}` → Ver detalle de juego

### Cliente
- `GET /usuario/mi-biblioteca` → Biblioteca personal
- `GET /usuario/mis-compras` → Historial de compras
- `GET /usuario/perfil` → Ver/Editar perfil
- `POST /comprar/{id}` → Comprar juego

### Proveedor
- `GET /proveedor/publicar` → Formulario publicar juego
- `POST /proveedor/publicar` → Guardar juego nuevo
- `GET /proveedor/mis-juegos` → Ver juegos publicados
- `GET /proveedor/editar/{id}` → Editar juego
- `POST /proveedor/editar/{id}` → Actualizar juego
- `GET /proveedor/ventas` → Ver ventas e ingresos

### Administrador
- `GET /admin/usuarios` → Gestionar usuarios
- `GET /admin/ganancias` → Ver ganancias plataforma
- `GET /admin/movimientos` → Ver todos los movimientos
- `POST /admin/usuarios/activar/{id}` → Activar usuario
- `POST /admin/usuarios/desactivar/{id}` → Desactivar usuario

---

## 🔐 Seguridad Implementada

### Autenticación
- **BCrypt** para encriptar contraseñas
- **Spring Security** para gestión de sesiones
- Verificación de email único en registro

### Autorización (Control de Acceso)
- **ADMIN**: Solo puede acceder a `/admin/**`
- **PROVEEDOR**: Puede acceder a `/proveedor/**` y `/catalogo`, `/comprar`
- **CLIENTE**: Puede acceder a `/usuario/**`, `/catalogo`, `/comprar`
- **Todos**: Pueden acceder a `/catalogo` y `/juego/**`

### Validaciones de Negocio
- ✅ Usuario no puede comprar el mismo juego dos veces
- ✅ Admin no puede comprar juegos
- ✅ Solo el proveedor puede editar sus propios juegos
- ✅ Validación de imágenes (tamaño y dimensiones)

---

## 📊 Modelo de Datos

### Entidades Principales

**Usuario**
- id, nombre, email, password (encriptada), rol, datosFiscales, fechaRegistro, activo

**Juego**
- id, titulo, descripcion, precio, genero, imagen, fechaPublicacion, proveedor_id

**Compra**
- id, usuario_id, juego_id, fechaCompra, precio, pagadaAlProveedor

**BibliotecaUsuario**
- id, usuario_id, juego_id, fechaAdquisicion

**MovimientoProveedor**
- id, proveedor_id, compra_id, monto, fecha, cobrado

**PublicacionJuego**
- id, juego_id, proveedor_id, costo (25€), fechaPublicacion

---

## 📝 Notas Adicionales

### Convenciones de Código
- **Nombres en español**: Métodos y variables tienen nombres descriptivos en español para facilitar la comprensión
- **Comentarios Javadoc**: Todos los métodos públicos están documentados
- **Constantes**: Valores como comisiones y costos están definidos como constantes

### Reglas de Negocio Clave
1. **Comisión Plataforma**: 15% de cada venta
2. **Comisión Proveedor**: 85% de cada venta
3. **Costo Publicación**: 25€ por juego publicado
4. **Admin no compra**: Los administradores no pueden realizar compras
5. **Sin duplicados**: No se puede comprar el mismo juego dos veces

---

## 🤝 Autor

**Juan** - Proyecto Spring Boot PixelShop  
Universidad - Noviembre 2025

---

## 📄 Licencia

Este proyecto es educativo y fue creado con fines académicos.

---

**¿Necesitas más información?** Consulta las otras guías en la carpeta `documentacion/`:
- [ARQUITECTURA.md](ARQUITECTURA.md) - Arquitectura técnica detallada
- [SERVICIOS.md](SERVICIOS.md) - Guía completa de servicios
- [MODELO_NEGOCIO.md](MODELO_NEGOCIO.md) - Sistema de comisiones
- [SEGURIDAD.md](SEGURIDAD.md) - Configuración de seguridad
