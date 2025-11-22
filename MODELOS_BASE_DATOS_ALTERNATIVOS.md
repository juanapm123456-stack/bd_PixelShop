# 🗄️ Modelos de Base de Datos Alternativos - Pixel Shop

## 📌 Introducción

Este documento presenta **4 enfoques diferentes** para diseñar la base de datos de Pixel Shop. Cada modelo tiene ventajas y desventajas dependiendo del contexto y objetivos del proyecto.

**Comparación rápida:**

| Modelo | Enfoque | Ventaja Principal | Desventaja Principal |
|--------|---------|-------------------|---------------------|
| **1. Usuario con Campo Rol** | Single Table | Simple, pocas tablas | Columnas con muchos NULL |
| **2. Herencia de Tablas** | Table Per Type | Datos separados por rol | Complejidad, JOINs |
| **3. Enfoque Económico** | Transacciones | Control financiero | Muchas tablas |
| **4. Orientado a Servicios** | DDD/Microservicios | Escalabilidad | Complejidad inicial |

---

# 🔵 Modelo 1: Usuario con Campo Rol (Single Table)

## Concepto
Un único modelo de `Usuario` con un campo `rol` (ENUM) y columnas opcionales para cada tipo de usuario.

## Filosofía
"Todos son usuarios, solo cambian los permisos y datos adicionales"

---

## Esquema de Tablas

### Tabla: `usuario`
```sql
CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('CLIENTE', 'PROVEEDOR', 'ADMIN') NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    url_foto_perfil VARCHAR(255),
    
    -- Campos específicos de PROVEEDOR (NULL para CLIENTE/ADMIN)
    cif_nif VARCHAR(20),
    direccion_fiscal VARCHAR(255),
    telefono_contacto VARCHAR(20),
    email_paypal VARCHAR(100),
    verificado BOOLEAN DEFAULT FALSE,
    fecha_verificacion DATETIME,
    
    -- Auditoría
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    
    INDEX idx_email (email),
    INDEX idx_rol (rol)
);
```

### Tabla: `juego`
```sql
CREATE TABLE juego (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio_actual DECIMAL(10,2) NOT NULL,
    genero VARCHAR(50),
    proveedor_id BIGINT NOT NULL,
    video_url VARCHAR(255),
    fecha_publicacion DATETIME,
    monto_pago_publicacion DECIMAL(10,2) DEFAULT 25.00,
    activo BOOLEAN DEFAULT TRUE,
    destacado BOOLEAN DEFAULT FALSE,
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    
    FOREIGN KEY (proveedor_id) REFERENCES usuario(id),
    INDEX idx_proveedor (proveedor_id),
    INDEX idx_activo (activo)
);
```

### Tabla: `imagen_juego`
```sql
CREATE TABLE imagen_juego (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    juego_id BIGINT NOT NULL,
    url VARCHAR(255) NOT NULL,
    public_id_cloudinary VARCHAR(255),
    orden INT NOT NULL,
    tipo ENUM('PORTADA', 'CAPTURA', 'LOGO') DEFAULT 'CAPTURA',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (juego_id) REFERENCES juego(id) ON DELETE CASCADE,
    INDEX idx_juego_orden (juego_id, orden)
);
```

### Tabla: `compra`
```sql
CREATE TABLE compra (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    juego_id BIGINT NOT NULL,
    precio_pagado DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('PAYPAL', 'TARJETA', 'REGALO') DEFAULT 'PAYPAL',
    estado ENUM('PENDIENTE', 'COMPLETADA', 'REEMBOLSADA') DEFAULT 'COMPLETADA',
    order_id_paypal VARCHAR(100),
    email_comprador VARCHAR(100),
    fecha_compra DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (juego_id) REFERENCES juego(id),
    UNIQUE KEY unique_compra (usuario_id, juego_id),
    INDEX idx_usuario (usuario_id),
    INDEX idx_juego (juego_id),
    INDEX idx_fecha (fecha_compra)
);
```

### Tabla: `transaccion_proveedor`
```sql
CREATE TABLE transaccion_proveedor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    proveedor_id BIGINT NOT NULL,
    juego_id BIGINT NOT NULL,
    compra_id BIGINT NOT NULL,
    monto_bruto DECIMAL(10,2) NOT NULL,
    comision_plataforma DECIMAL(10,2) NOT NULL,
    monto_neto DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE', 'PROCESANDO', 'PAGADO', 'RECHAZADO') DEFAULT 'PENDIENTE',
    metodo_pago ENUM('PAYPAL_PAYOUT', 'TRANSFERENCIA'),
    referencia_pago VARCHAR(100),
    fecha_venta DATETIME,
    fecha_procesamiento DATETIME,
    fecha_pago DATETIME,
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (proveedor_id) REFERENCES usuario(id),
    FOREIGN KEY (juego_id) REFERENCES juego(id),
    FOREIGN KEY (compra_id) REFERENCES compra(id),
    INDEX idx_proveedor_estado (proveedor_id, estado)
);
```

### Tabla: `transaccion_plataforma`
```sql
CREATE TABLE transaccion_plataforma (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tipo ENUM('COMISION_VENTA', 'PAGO_PUBLICACION', 'PAGO_PROVEEDOR') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    juego_id BIGINT,
    compra_id BIGINT,
    transaccion_proveedor_id BIGINT,
    descripcion VARCHAR(255),
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (juego_id) REFERENCES juego(id),
    FOREIGN KEY (compra_id) REFERENCES compra(id),
    FOREIGN KEY (transaccion_proveedor_id) REFERENCES transaccion_proveedor(id),
    INDEX idx_tipo_fecha (tipo, fecha)
);
```

---

## Ventajas ✅

1. **Simplicidad**
   - Solo 6 tablas principales
   - Fácil de entender
   - Un solo `UsuarioRepository`

2. **Flexibilidad de Roles**
   - Un usuario puede cambiar de rol fácilmente
   - No requiere migración de datos entre tablas

3. **Consultas Simples**
   ```sql
   SELECT * FROM usuario WHERE rol = 'PROVEEDOR';
   ```

4. **Facilidad de Desarrollo**
   - Menos código
   - Menos clases JPA
   - Ideal para MVP

---

## Desventajas ❌

1. **Columnas con NULL**
   - Campos de proveedor siempre NULL para clientes/admins
   - Desperdicio de espacio

2. **Validaciones Complicadas**
   - Campos opcionales según rol
   - Difícil garantizar integridad en BD

3. **Escalabilidad Limitada**
   - Si proveedores necesitan 10+ campos más, tabla crece mucho
   - No soporta múltiples roles simultáneos

4. **Dificultad para Queries Específicas**
   ```sql
   -- Obtener proveedores verificados
   SELECT * FROM usuario 
   WHERE rol = 'PROVEEDOR' AND verificado = TRUE;
   ```
   Requiere filtrar por rol siempre

---

## Cuándo Usar Este Modelo 🎯

✅ **Ideal para:**
- MVP / Prototipos rápidos
- Equipos pequeños
- Proyectos con roles simples
- Pocos campos específicos por rol

❌ **No usar si:**
- Roles tienen 10+ campos específicos
- Usuarios pueden tener múltiples roles
- Necesitas separación estricta de datos

---

# 🟢 Modelo 2: Herencia de Tablas (Table Per Type)

## Concepto
Tabla base `usuario` con tablas separadas para cada rol que extienden la base.

## Filosofía
"Cada rol es una entidad diferente que comparte atributos básicos"

---

## Esquema de Tablas

### Tabla Base: `usuario`
```sql
CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    url_foto_perfil VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    
    INDEX idx_email (email)
);
```

### Tabla: `cliente`
```sql
CREATE TABLE cliente (
    id BIGINT PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    preferencia_genero VARCHAR(50),
    saldo_creditos DECIMAL(10,2) DEFAULT 0,
    newsletter_subscrito BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);
```

### Tabla: `proveedor`
```sql
CREATE TABLE proveedor (
    id BIGINT PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    cif_nif VARCHAR(20) NOT NULL,
    direccion_fiscal VARCHAR(255),
    telefono_contacto VARCHAR(20),
    email_paypal VARCHAR(100),
    cuenta_bancaria_iban VARCHAR(50),
    verificado BOOLEAN DEFAULT FALSE,
    fecha_verificacion DATETIME,
    puntuacion_promedio DECIMAL(3,2),
    total_ventas INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);
```

### Tabla: `admin`
```sql
CREATE TABLE admin (
    id BIGINT PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    nivel_acceso ENUM('BASICO', 'MODERADOR', 'SUPERADMIN') DEFAULT 'BASICO',
    puede_eliminar_usuarios BOOLEAN DEFAULT FALSE,
    puede_gestionar_pagos BOOLEAN DEFAULT FALSE,
    ultimo_acceso_panel DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);
```

### Tabla: `rol_usuario` (Para múltiples roles)
```sql
CREATE TABLE rol_usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    rol ENUM('CLIENTE', 'PROVEEDOR', 'ADMIN') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    UNIQUE KEY unique_usuario_rol (usuario_id, rol)
);
```

### Las demás tablas (`juego`, `imagen_juego`, `compra`, etc.) permanecen similares al Modelo 1

---

## Ventajas ✅

1. **Separación Clara**
   - Datos específicos en tablas separadas
   - Sin columnas NULL
   - Fácil agregar campos por rol

2. **Múltiples Roles**
   - Un usuario puede ser CLIENTE y PROVEEDOR simultáneamente
   - Tabla `rol_usuario` gestiona la relación

3. **Integridad de Datos**
   - Validaciones específicas por tabla
   - Constraints más fuertes

4. **Escalabilidad**
   - Agregar 20 campos a proveedores no afecta otras tablas
   - Fácil agregar nuevos roles

---

## Desventajas ❌

1. **Complejidad de Queries**
   ```sql
   -- Obtener usuario con datos de proveedor
   SELECT u.*, p.* 
   FROM usuario u
   LEFT JOIN proveedor p ON u.id = p.usuario_id
   WHERE u.id = ?;
   ```
   Siempre requiere JOINs

2. **Más Tablas**
   - 9+ tablas (vs 6 del modelo 1)
   - Más clases JPA
   - Más repositorios

3. **Transacciones Complejas**
   - Crear usuario + crear proveedor en 2 pasos
   - Rollback más complicado

4. **ORM Complicado**
   ```java
   @Entity
   @Inheritance(strategy = InheritanceType.JOINED)
   public class Usuario { ... }
   
   @Entity
   @PrimaryKeyJoinColumn(name = "usuario_id")
   public class Proveedor extends Usuario { ... }
   ```

---

## Cuándo Usar Este Modelo 🎯

✅ **Ideal para:**
- Roles con muchos campos específicos
- Usuarios con múltiples roles
- Proyectos grandes
- Equipos con experiencia en JPA

❌ **No usar si:**
- MVP / Prototipo rápido
- Equipo sin experiencia en herencia JPA
- Pocos campos específicos por rol

---

# 🟡 Modelo 3: Enfocado en Economía (Transaction-Centric)

## Concepto
Base de datos diseñada alrededor del flujo de dinero y transacciones.

## Filosofía
"El dinero es el corazón del negocio, todo debe rastrearse"

---

## Esquema de Tablas

### Tabla: `usuario` (Similar a Modelo 1)
```sql
CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('CLIENTE', 'PROVEEDOR', 'ADMIN') NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Tabla: `cuenta_usuario`
```sql
CREATE TABLE cuenta_usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT UNIQUE NOT NULL,
    saldo_disponible DECIMAL(10,2) DEFAULT 0,
    saldo_pendiente DECIMAL(10,2) DEFAULT 0,
    total_ganado DECIMAL(10,2) DEFAULT 0,
    total_gastado DECIMAL(10,2) DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CHECK (saldo_disponible >= 0),
    CHECK (saldo_pendiente >= 0)
);
```

### Tabla: `transaccion` (Master Transaction Table)
```sql
CREATE TABLE transaccion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tipo ENUM('COMPRA', 'VENTA', 'PUBLICACION', 'PAGO_PROVEEDOR', 'COMISION', 'REEMBOLSO') NOT NULL,
    estado ENUM('PENDIENTE', 'PROCESANDO', 'COMPLETADA', 'RECHAZADA', 'CANCELADA') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    moneda VARCHAR(3) DEFAULT 'EUR',
    
    -- Participantes
    usuario_origen_id BIGINT,
    usuario_destino_id BIGINT,
    
    -- Referencias
    juego_id BIGINT,
    compra_id BIGINT,
    referencia_externa VARCHAR(100),
    
    descripcion VARCHAR(255),
    metadata JSON,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_procesamiento DATETIME,
    fecha_completacion DATETIME,
    
    FOREIGN KEY (usuario_origen_id) REFERENCES usuario(id),
    FOREIGN KEY (usuario_destino_id) REFERENCES usuario(id),
    FOREIGN KEY (juego_id) REFERENCES juego(id),
    FOREIGN KEY (compra_id) REFERENCES compra(id),
    
    INDEX idx_tipo_estado (tipo, estado),
    INDEX idx_usuario_origen (usuario_origen_id),
    INDEX idx_usuario_destino (usuario_destino_id),
    INDEX idx_fecha (fecha_creacion)
);
```

### Tabla: `movimiento_cuenta`
```sql
CREATE TABLE movimiento_cuenta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cuenta_id BIGINT NOT NULL,
    transaccion_id BIGINT NOT NULL,
    tipo ENUM('INGRESO', 'EGRESO', 'RETENCION', 'LIBERACION') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    saldo_anterior DECIMAL(10,2) NOT NULL,
    saldo_posterior DECIMAL(10,2) NOT NULL,
    descripcion VARCHAR(255),
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cuenta_id) REFERENCES cuenta_usuario(id),
    FOREIGN KEY (transaccion_id) REFERENCES transaccion(id),
    INDEX idx_cuenta_fecha (cuenta_id, fecha)
);
```

### Tabla: `pago_proveedor`
```sql
CREATE TABLE pago_proveedor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    proveedor_id BIGINT NOT NULL,
    transaccion_id BIGINT NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('PAYPAL_PAYOUT', 'TRANSFERENCIA', 'STRIPE') NOT NULL,
    estado ENUM('SOLICITADO', 'EN_PROCESO', 'ENVIADO', 'COMPLETADO', 'FALLIDO') NOT NULL,
    referencia_externa VARCHAR(100),
    email_paypal VARCHAR(100),
    cuenta_bancaria_iban VARCHAR(50),
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_envio DATETIME,
    fecha_completado DATETIME,
    error_mensaje TEXT,
    
    FOREIGN KEY (proveedor_id) REFERENCES usuario(id),
    FOREIGN KEY (transaccion_id) REFERENCES transaccion(id),
    INDEX idx_proveedor_estado (proveedor_id, estado)
);
```

### Tabla: `comision_plataforma`
```sql
CREATE TABLE comision_plataforma (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    compra_id BIGINT NOT NULL,
    transaccion_id BIGINT NOT NULL,
    monto_venta DECIMAL(10,2) NOT NULL,
    porcentaje_comision DECIMAL(5,2) DEFAULT 15.00,
    monto_comision DECIMAL(10,2) NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (compra_id) REFERENCES compra(id),
    FOREIGN KEY (transaccion_id) REFERENCES transaccion(id),
    INDEX idx_fecha (fecha)
);
```

### Tabla: `balance_diario`
```sql
CREATE TABLE balance_diario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fecha DATE UNIQUE NOT NULL,
    total_ventas DECIMAL(10,2) DEFAULT 0,
    total_comisiones DECIMAL(10,2) DEFAULT 0,
    total_publicaciones DECIMAL(10,2) DEFAULT 0,
    total_pagos_proveedores DECIMAL(10,2) DEFAULT 0,
    balance_neto DECIMAL(10,2) DEFAULT 0,
    num_transacciones INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_fecha (fecha)
);
```

### Tabla: `auditoria_financiera`
```sql
CREATE TABLE auditoria_financiera (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entidad ENUM('TRANSACCION', 'CUENTA', 'PAGO', 'COMISION') NOT NULL,
    entidad_id BIGINT NOT NULL,
    accion ENUM('CREACION', 'ACTUALIZACION', 'CANCELACION') NOT NULL,
    usuario_id BIGINT,
    datos_anteriores JSON,
    datos_nuevos JSON,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX idx_entidad (entidad, entidad_id),
    INDEX idx_fecha (fecha)
);
```

---

## Ejemplo: Flujo de Compra

1. **Cliente compra juego (100€)**
```sql
-- Transacción de compra
INSERT INTO transaccion (tipo, estado, monto, usuario_origen_id, juego_id)
VALUES ('COMPRA', 'COMPLETADA', 100.00, 5, 10);

-- Movimiento: Egreso del cliente
INSERT INTO movimiento_cuenta (cuenta_id, tipo, monto, saldo_anterior, saldo_posterior)
VALUES (5, 'EGRESO', 100.00, 500.00, 400.00);

-- Comisión plataforma (15€)
INSERT INTO comision_plataforma (compra_id, monto_venta, monto_comision)
VALUES (50, 100.00, 15.00);

-- Transacción de venta para proveedor (85€)
INSERT INTO transaccion (tipo, estado, monto, usuario_destino_id, juego_id)
VALUES ('VENTA', 'PENDIENTE', 85.00, 3, 10);

-- Movimiento: Retención en cuenta proveedor
INSERT INTO movimiento_cuenta (cuenta_id, tipo, monto, saldo_anterior, saldo_posterior)
VALUES (3, 'RETENCION', 85.00, 200.00, 200.00); -- Saldo_pendiente aumenta

-- Balance diario
UPDATE balance_diario 
SET total_ventas = total_ventas + 100,
    total_comisiones = total_comisiones + 15,
    balance_neto = balance_neto + 15,
    num_transacciones = num_transacciones + 1
WHERE fecha = CURDATE();
```

---

## Ventajas ✅

1. **Control Financiero Total**
   - Cada euro rastreado
   - Auditoría completa
   - Reconciliación bancaria fácil

2. **Reportes Detallados**
   ```sql
   -- Ganancias del mes
   SELECT SUM(monto_comision) FROM comision_plataforma
   WHERE MONTH(fecha) = MONTH(CURDATE());
   
   -- Balance diario
   SELECT * FROM balance_diario 
   WHERE fecha BETWEEN '2024-01-01' AND '2024-01-31';
   ```

3. **Detección de Fraudes**
   - Movimientos sospechosos fáciles de detectar
   - Auditoría de cambios

4. **Contabilidad Simplificada**
   - Exportar a software contable directamente
   - Cumplimiento legal facilitado

---

## Desventajas ❌

1. **Complejidad Alta**
   - 12+ tablas solo para finanzas
   - Código más complejo
   - Curva de aprendizaje

2. **Rendimiento**
   - Muchas inserciones por transacción
   - Queries complejas con múltiples JOINs

3. **Overhead**
   - Para proyectos pequeños, excesivo
   - Requiere experiencia en sistemas financieros

4. **Difícil de Desarrollar**
   - Testing complejo
   - Rollbacks complicados

---

## Cuándo Usar Este Modelo 🎯

✅ **Ideal para:**
- E-commerce con alto volumen
- Necesidad de auditoría estricta
- Cumplimiento legal/contable
- Marketplaces con múltiples vendedores
- Proyectos con inversores externos

❌ **No usar si:**
- MVP / Startup temprana
- Equipo sin experiencia en finanzas
- Proyecto con pocos usuarios

---

# 🟣 Modelo 4: Orientado a Servicios (DDD/Microservicios)

## Concepto
Base de datos dividida por contextos acotados (Bounded Contexts) según Domain-Driven Design.

## Filosofía
"Cada servicio tiene su propia BD, comunicación mediante eventos"

---

## Arquitectura

```
┌─────────────────────────────────────────────┐
│          IDENTITY SERVICE                   │
│  (Gestión de usuarios y autenticación)      │
└─────────────────────────────────────────────┘
             ↓ events
┌─────────────────────────────────────────────┐
│          CATALOG SERVICE                    │
│  (Publicación y gestión de juegos)          │
└─────────────────────────────────────────────┘
             ↓ events
┌─────────────────────────────────────────────┐
│          PURCHASE SERVICE                   │
│  (Compras y biblioteca de usuarios)         │
└─────────────────────────────────────────────┘
             ↓ events
┌─────────────────────────────────────────────┐
│          PAYMENT SERVICE                    │
│  (Procesamiento de pagos con PayPal)        │
└─────────────────────────────────────────────┘
             ↓ events
┌─────────────────────────────────────────────┐
│          PAYOUT SERVICE                     │
│  (Pagos a proveedores)                      │
└─────────────────────────────────────────────┘
```

---

## Base de Datos: IDENTITY SERVICE

### Tabla: `usuario`
```sql
CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) UNIQUE NOT NULL,  -- UUID para comunicación entre servicios
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('CLIENTE', 'PROVEEDOR', 'ADMIN') NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    url_foto_perfil VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Tabla: `proveedor_perfil`
```sql
CREATE TABLE proveedor_perfil (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_uuid VARCHAR(36) UNIQUE NOT NULL,
    cif_nif VARCHAR(20),
    email_paypal VARCHAR(100),
    verificado BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## Base de Datos: CATALOG SERVICE

### Tabla: `juego_catalog`
```sql
CREATE TABLE juego_catalog (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    proveedor_uuid VARCHAR(36) NOT NULL,  -- Referencia a Identity Service
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio_actual DECIMAL(10,2) NOT NULL,
    genero VARCHAR(50),
    video_url VARCHAR(255),
    fecha_publicacion DATETIME,
    activo BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_proveedor (proveedor_uuid)
);
```

### Tabla: `imagen_juego_catalog`
```sql
CREATE TABLE imagen_juego_catalog (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    juego_uuid VARCHAR(36) NOT NULL,
    url VARCHAR(255) NOT NULL,
    orden INT NOT NULL,
    tipo ENUM('PORTADA', 'CAPTURA') DEFAULT 'CAPTURA',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### Tabla: `publicacion_fee`
```sql
CREATE TABLE publicacion_fee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    juego_uuid VARCHAR(36) UNIQUE NOT NULL,
    monto DECIMAL(10,2) DEFAULT 25.00,
    estado_pago ENUM('PENDIENTE', 'PAGADO') DEFAULT 'PENDIENTE',
    payment_id_externo VARCHAR(100),
    fecha_pago DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## Base de Datos: PURCHASE SERVICE

### Tabla: `compra_purchase`
```sql
CREATE TABLE compra_purchase (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    usuario_uuid VARCHAR(36) NOT NULL,
    juego_uuid VARCHAR(36) NOT NULL,
    precio_pagado DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE', 'COMPLETADA', 'REEMBOLSADA') DEFAULT 'PENDIENTE',
    fecha_compra DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_usuario (usuario_uuid),
    INDEX idx_juego (juego_uuid),
    UNIQUE KEY unique_compra (usuario_uuid, juego_uuid)
);
```

### Tabla: `biblioteca_purchase`
```sql
CREATE TABLE biblioteca_purchase (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_uuid VARCHAR(36) NOT NULL,
    juego_uuid VARCHAR(36) NOT NULL,
    compra_uuid VARCHAR(36) NOT NULL,
    fecha_adquisicion DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_biblioteca (usuario_uuid, juego_uuid),
    INDEX idx_usuario (usuario_uuid)
);
```

---

## Base de Datos: PAYMENT SERVICE

### Tabla: `transaccion_payment`
```sql
CREATE TABLE transaccion_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    compra_uuid VARCHAR(36) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo ENUM('PAYPAL', 'STRIPE', 'TARJETA') DEFAULT 'PAYPAL',
    estado ENUM('PENDIENTE', 'PROCESANDO', 'COMPLETADA', 'FALLIDA') NOT NULL,
    order_id_externo VARCHAR(100),
    email_pagador VARCHAR(100),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_completado DATETIME,
    
    INDEX idx_compra (compra_uuid)
);
```

---

## Base de Datos: PAYOUT SERVICE

### Tabla: `payout_solicitud`
```sql
CREATE TABLE payout_solicitud (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    proveedor_uuid VARCHAR(36) NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE', 'EN_PROCESO', 'COMPLETADO', 'FALLIDO') NOT NULL,
    metodo ENUM('PAYPAL_PAYOUT', 'TRANSFERENCIA') DEFAULT 'PAYPAL_PAYOUT',
    email_paypal VARCHAR(100),
    referencia_externa VARCHAR(100),
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_completado DATETIME,
    
    INDEX idx_proveedor (proveedor_uuid)
);
```

### Tabla: `payout_detalle`
```sql
CREATE TABLE payout_detalle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payout_solicitud_uuid VARCHAR(36) NOT NULL,
    compra_uuid VARCHAR(36) NOT NULL,
    monto_bruto DECIMAL(10,2) NOT NULL,
    comision DECIMAL(10,2) NOT NULL,
    monto_neto DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## Comunicación Entre Servicios (Eventos)

### Tabla: `event_outbox` (En cada servicio)
```sql
CREATE TABLE event_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    payload JSON NOT NULL,
    estado ENUM('PENDIENTE', 'ENVIADO', 'FALLIDO') DEFAULT 'PENDIENTE',
    reintentos INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME,
    
    INDEX idx_estado (estado)
);
```

### Eventos Típicos

**Identity Service:**
- `UsuarioRegistrado`
- `UsuarioActualizado`
- `ProveedorVerificado`

**Catalog Service:**
- `JuegoPublicado`
- `JuegoActualizado`
- `JuegoEliminado`

**Purchase Service:**
- `CompraIniciada`
- `CompraCompletada`
- `BibliotecaActualizada`

**Payment Service:**
- `PagoRecibido`
- `PagoProcesado`

**Payout Service:**
- `PayoutSolicitado`
- `PayoutCompletado`

---

## Ventajas ✅

1. **Escalabilidad Independiente**
   - Cada servicio se escala según necesidad
   - Catalog Service puede manejar 10K requests/seg
   - Payment Service puede estar separado físicamente

2. **Despliegue Independiente**
   - Actualizar Purchase Service sin afectar Catalog
   - Rollback independiente

3. **Tecnologías Diferentes**
   - Payment Service en Node.js
   - Catalog Service en Java
   - Payout Service en Python

4. **Resiliencia**
   - Si Catalog cae, Purchase sigue funcionando
   - Circuit breakers entre servicios

5. **Equipos Independientes**
   - Equipo A: Identity + Catalog
   - Equipo B: Purchase + Payment
   - Equipo C: Payout

---

## Desventajas ❌

1. **Complejidad Extrema**
   - 5 bases de datos diferentes
   - Eventual consistency
   - Debugging difícil

2. **Transacciones Distribuidas**
   - No hay ACID entre servicios
   - Saga pattern necesario
   - Compensaciones en caso de error

3. **Latencia**
   - Múltiples llamadas HTTP entre servicios
   - Overhead de red

4. **Duplicación de Datos**
   - Cada servicio tiene caché de datos de otros
   - Sincronización compleja

5. **Overhead de Infraestructura**
   - API Gateway
   - Service Discovery (Eureka, Consul)
   - Message Broker (Kafka, RabbitMQ)
   - Tracing distribuido (Zipkin, Jaeger)

6. **No para Equipos Pequeños**
   - Requiere DevOps experto
   - Monitoreo complejo

---

## Cuándo Usar Este Modelo 🎯

✅ **Ideal para:**
- Empresas con 100K+ usuarios
- Múltiples equipos de desarrollo
- Alta demanda de escalabilidad
- Proyectos con años de vida planificados
- Necesidad de diferentes tecnologías

❌ **No usar si:**
- MVP / Startup
- Equipo < 10 desarrolladores
- < 10K usuarios esperados
- Sin experiencia en microservicios
- Presupuesto limitado

---

# 📊 Comparación Final

| Criterio | Modelo 1 | Modelo 2 | Modelo 3 | Modelo 4 |
|----------|----------|----------|----------|----------|
| **Complejidad** | ⭐ Baja | ⭐⭐ Media | ⭐⭐⭐ Alta | ⭐⭐⭐⭐⭐ Extrema |
| **Num. Tablas** | 6-7 | 9-10 | 12-15 | 15-20 (divididas) |
| **Escalabilidad** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Mantenibilidad** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Performance** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Tiempo Desarrollo** | 1-2 semanas | 3-4 semanas | 5-6 semanas | 3-4 meses |
| **Equipo Mínimo** | 1-2 devs | 2-3 devs | 3-4 devs | 5+ devs + DevOps |
| **Costo Infraestructura** | $ | $$ | $$$ | $$$$$ |
| **Testing** | Fácil | Medio | Difícil | Muy difícil |
| **Ideal para** | MVP | Producto | Empresa | Corporativo |

---

# 🎯 Recomendación para Pixel Shop

## Situación Actual
- Proyecto funcional en producción
- Equipo pequeño (1-3 devs)
- < 1000 usuarios esperados inicialmente
- Presupuesto limitado
- Necesidad de agregar features rápido

## Modelo Recomendado: **Modelo 1 Mejorado**

### Por qué:
✅ Balance perfecto entre simplicidad y escalabilidad
✅ Permite agregar features rápido
✅ Fácil de mantener con equipo pequeño
✅ Suficiente para 10K-50K usuarios
✅ Migración a Modelo 2 posible si crece

### Mejoras sobre actual:
1. Eliminar `biblioteca_usuario` ✅
2. Eliminar `publicacion_juego` ✅
3. Agregar tabla `imagen_juego` ✅
4. Agregar tabla `transaccion_plataforma` ✅
5. Agregar soft delete ✅
6. Agregar auditoría ✅

### Cuándo migrar a Modelo 2:
- 10+ campos específicos por rol
- Usuarios necesitan múltiples roles
- Más de 50K usuarios activos

### Cuándo considerar Modelo 3:
- Necesidad de auditoría legal estricta
- Inversores externos exigen reportes detallados
- Más de 1000 transacciones/día

### Cuándo considerar Modelo 4:
- Más de 100K usuarios activos
- Múltiples equipos de desarrollo
- Diferentes partes del sistema escalan diferente

---

## 💡 Conclusión

No existe "el mejor modelo". Existe el modelo **adecuado para tu contexto actual**.

**Regla de oro:** Empieza simple, evoluciona cuando lo necesites.

🚀 **Pixel Shop debe empezar con Modelo 1 mejorado y evolucionar gradualmente.**
