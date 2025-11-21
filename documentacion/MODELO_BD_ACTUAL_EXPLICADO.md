# 🗄️ MODELO DE BASE DE DATOS ACTUAL - EXPLICADO

**PixelShop - Estructura de Tablas con Nombres Mejorados**

Este documento muestra el modelo actual de tu base de datos con **nombres más claros** y explicaciones de qué hace cada tabla.

---

## 📊 RESUMEN GENERAL

Tu aplicación tiene **6 tablas principales**:

| Tabla | Propósito |
|-------|-----------|
| **usuarios** | Almacena las cuentas (clientes, proveedores, admins) |
| **juegos** | Almacena los productos (videojuegos) que se venden |
| **compras** | Registra cuando un cliente compra un juego |
| **movimiento_proveedor** | Registra el dinero que gana el proveedor por cada venta |
| **publicacion_juego** | Registra el pago de 25€ por publicar un juego |
| **biblioteca_usuario** | Almacena los juegos que tiene cada usuario |

---

# 📋 TABLAS DETALLADAS

---

## 1️⃣ **USUARIOS** 👤

**Tabla:** `usuarios`

**¿Qué hace?**  
Almacena todas las cuentas de usuario del sistema (clientes, proveedores y administradores).

### Campos:

```
id                  BIGINT          PRIMARY KEY AUTO_INCREMENT
nombre              VARCHAR(255)    NOT NULL
email               VARCHAR(255)    UNIQUE NOT NULL
password            VARCHAR(255)    NOT NULL (BCrypt encriptada)
rol                 VARCHAR(20)     NOT NULL (CLIENTE, PROVEEDOR, ADMIN)
datos_fiscales      VARCHAR(500)    NULL (información fiscal del proveedor)
fecha_registro      DATETIME        NOT NULL
activo              BOOLEAN         NOT NULL DEFAULT true
```

### Ejemplo de datos:

```
1 | Juan Pérez    | juan@gmail.com    | $2a$10$... | CLIENTE    | null          | 2025-11-20 10:00 | true
2 | Epic Games    | epic@games.com    | $2a$10$... | PROVEEDOR  | NIF: B12345   | 2025-11-15 09:30 | true
3 | Administrador | admin@pixelshop   | $2a$10$... | ADMIN      | null          | 2025-11-01 08:00 | true
```

### ¿Para qué sirve cada campo?

- **id**: Identificador único del usuario
- **nombre**: Nombre completo o nombre de la empresa
- **email**: Para login y comunicaciones
- **password**: Contraseña encriptada con BCrypt
- **rol**: Define qué puede hacer (CLIENTE compra, PROVEEDOR vende, ADMIN gestiona)
- **datos_fiscales**: Información fiscal del proveedor (CIF/NIF)
- **fecha_registro**: Cuándo se registró
- **activo**: Si la cuenta está activa (para baneos)

---

## 2️⃣ **JUEGOS** 🎮

**Tabla:** `juegos`

**¿Qué hace?**  
Almacena los videojuegos que están a la venta en la plataforma.

### Campos:

```
id                  BIGINT          PRIMARY KEY AUTO_INCREMENT
titulo              VARCHAR(200)    NOT NULL
descripcion         TEXT            NULL
precio              DECIMAL(10,2)   NOT NULL
proveedor_id        BIGINT          NOT NULL → FOREIGN KEY usuarios(id)
fecha_publicacion   DATETIME        NOT NULL
activo              BOOLEAN         NOT NULL DEFAULT true
imagen_url_1        VARCHAR(500)    NULL (portada principal)
imagen_url_2        VARCHAR(500)    NULL (captura 1)
imagen_url_3        VARCHAR(500)    NULL (captura 2)
imagen_url_4        VARCHAR(500)    NULL (captura 3)
video_youtube_url   VARCHAR(500)    NULL (trailer)
genero              VARCHAR(100)    NULL (RPG, Acción, Deportes...)
```

### Ejemplo de datos:

```
1 | Cyberpunk 2077  | RPG futurista... | 59.99 | 2 | 2025-11-20 | true | url1.jpg | url2.jpg | url3.jpg | url4.jpg | youtube.com/... | RPG
2 | FIFA 24         | Simulador...     | 69.99 | 2 | 2025-11-20 | true | url1.jpg | url2.jpg | url3.jpg | url4.jpg | youtube.com/... | Deportes
```

### ¿Para qué sirve cada campo?

- **id**: Identificador único del juego
- **titulo**: Nombre del juego
- **descripcion**: Descripción detallada del juego
- **precio**: Precio de venta (el cliente paga esto)
- **proveedor_id**: Quién publicó el juego (referencia a `usuarios`)
- **fecha_publicacion**: Cuándo se subió el juego
- **activo**: Si está visible en el catálogo
- **imagen_url_1/2/3/4**: URLs de las imágenes del juego
- **video_youtube_url**: URL del trailer en YouTube
- **genero**: Categoría del juego

---

## 3️⃣ **COMPRAS** 🛒

**Tabla:** `compras`

**¿Qué hace?**  
Registra cada vez que un cliente compra un juego.

### Campos:

```
id                      BIGINT          PRIMARY KEY AUTO_INCREMENT
usuario_id              BIGINT          NOT NULL → FOREIGN KEY usuarios(id)
juego_id                BIGINT          NOT NULL → FOREIGN KEY juegos(id)
precio_pagado           DECIMAL(10,2)   NOT NULL
fecha_compra            DATETIME        NOT NULL
pagado_al_proveedor     BOOLEAN         NOT NULL DEFAULT false
```

### Restricción:
- **UNIQUE (usuario_id, juego_id)**: Un usuario no puede comprar el mismo juego dos veces

### Ejemplo de datos:

```
1 | 1 (Juan) | 1 (Cyberpunk) | 59.99 | 2025-11-21 10:30 | false
2 | 1 (Juan) | 2 (FIFA)      | 69.99 | 2025-11-21 11:00 | false
```

### ¿Para qué sirve cada campo?

- **id**: Identificador único de la compra
- **usuario_id**: Quién compró (el cliente)
- **juego_id**: Qué compró
- **precio_pagado**: Cuánto pagó el cliente
- **fecha_compra**: Cuándo se realizó la compra
- **pagado_al_proveedor**: Si ya se le pagó al proveedor (para liquidaciones)

### 💡 ¿Qué representa?

Cada fila = **"El cliente X compró el juego Y por Z euros"**

---

## 4️⃣ **MOVIMIENTO_PROVEEDOR** 💰

**Tabla:** `movimiento_proveedor`

**❌ NOMBRE CONFUSO** → Mejor nombre sería: **`ingresos_proveedor`** o **`ganancias_venta`**

**¿Qué hace?**  
Registra el dinero que gana el proveedor por cada venta (85% del precio).

### Campos:

```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
proveedor_id    BIGINT          NOT NULL → FOREIGN KEY usuarios(id)
compra_id       BIGINT          NOT NULL → FOREIGN KEY compras(id)
monto_neto      DECIMAL(10,2)   NOT NULL (85% del precio_pagado)
pagado          BOOLEAN         NOT NULL DEFAULT false
fecha           DATETIME        NOT NULL
```

### Ejemplo de datos:

```
1 | 2 (Epic Games) | 1 (Compra Cyberpunk) | 50.99 | false | 2025-11-21 10:30
2 | 2 (Epic Games) | 2 (Compra FIFA)      | 59.49 | false | 2025-11-21 11:00
```

**Cálculo:**
- Compra 1: Cliente pagó 59.99€ → Proveedor gana 50.99€ (85%)
- Compra 2: Cliente pagó 69.99€ → Proveedor gana 59.49€ (85%)

### ¿Para qué sirve cada campo?

- **id**: Identificador único del movimiento
- **proveedor_id**: Quién recibirá el dinero
- **compra_id**: De qué compra viene este dinero
- **monto_neto**: Cuánto gana el proveedor (85% del precio)
- **pagado**: Si ya se le transfirió el dinero al proveedor
- **fecha**: Cuándo se generó este ingreso

### 💡 ¿Qué representa?

Cada fila = **"El proveedor X ganó Y euros por la venta de este juego"**

### ⚠️ Problema con el nombre:

- **movimiento_proveedor** no es claro
- No sabes si es ingreso, gasto, transferencia...
- Mejor llamarlo: **`ingresos_proveedor`** o **`ganancias_venta`**

---

## 5️⃣ **PUBLICACION_JUEGO** 📢

**Tabla:** `publicacion_juego`

**❌ NOMBRE CONFUSO** → Mejor nombre sería: **`publicaciones`** o **`costos_publicacion`**

**¿Qué hace?**  
Registra el pago de 25€ que hace el proveedor para publicar un juego.

### Campos:

```
id                  BIGINT          PRIMARY KEY AUTO_INCREMENT
juego_id            BIGINT          NOT NULL UNIQUE → FOREIGN KEY juegos(id)
proveedor_id        BIGINT          NOT NULL → FOREIGN KEY usuarios(id)
fecha_publicacion   DATETIME        NOT NULL
pagado              BOOLEAN         NOT NULL DEFAULT false (si pagó los 25€)
```

### Ejemplo de datos:

```
1 | 1 (Cyberpunk) | 2 (Epic Games) | 2025-11-20 09:00 | true
2 | 2 (FIFA)      | 2 (Epic Games) | 2025-11-20 09:30 | true
```

### ¿Para qué sirve cada campo?

- **id**: Identificador único de la publicación
- **juego_id**: Qué juego se publicó (UNIQUE = cada juego solo puede publicarse una vez)
- **proveedor_id**: Quién lo publicó
- **fecha_publicacion**: Cuándo se publicó
- **pagado**: Si el proveedor pagó los 25€ de publicación

### 💡 ¿Qué representa?

Cada fila = **"El proveedor X pagó 25€ para publicar el juego Y"**

### ⚠️ Problema con el nombre:

- **publicacion_juego** es largo y redundante
- Mejor llamarlo: **`publicaciones`** (más corto) o **`costos_publicacion`** (más claro)

---

## 6️⃣ **BIBLIOTECA_USUARIO** 📚

**Tabla:** `biblioteca_usuario`

**❌ NOMBRE CONFUSO** → Mejor nombre sería: **`biblioteca`** o **`juegos_usuario`**

**¿Qué hace?**  
Almacena los juegos que tiene cada usuario (su "biblioteca personal").

### Campos:

```
id                  BIGINT          PRIMARY KEY AUTO_INCREMENT
usuario_id          BIGINT          NOT NULL → FOREIGN KEY usuarios(id)
juego_id            BIGINT          NOT NULL → FOREIGN KEY juegos(id)
fecha_adquisicion   DATETIME        NOT NULL
```

### Restricción:
- **UNIQUE (usuario_id, juego_id)**: Un usuario no puede tener el mismo juego dos veces en su biblioteca

### Ejemplo de datos:

```
1 | 1 (Juan) | 1 (Cyberpunk) | 2025-11-21 10:30
2 | 1 (Juan) | 2 (FIFA)      | 2025-11-21 11:00
```

### ¿Para qué sirve cada campo?

- **id**: Identificador único de la entrada
- **usuario_id**: A quién pertenece el juego
- **juego_id**: Qué juego tiene
- **fecha_adquisicion**: Cuándo lo obtuvo

### 💡 ¿Qué representa?

Cada fila = **"El usuario X tiene el juego Y en su biblioteca"**

### ⚠️ Problema con el nombre:

- **biblioteca_usuario** es redundante (obviamente la biblioteca es de un usuario)
- Mejor llamarlo: **`biblioteca`** (más corto)

---

# 🔄 RELACIONES ENTRE TABLAS

## Diagrama Visual:

```
┌─────────────┐
│  USUARIOS   │ ← Todas las cuentas
└──────┬──────┘
       │
       ├─────────────────────────────────┐
       │                                 │
       │ proveedor_id                    │ usuario_id
       │                                 │
┌──────▼──────┐                   ┌──────▼──────────┐
│   JUEGOS    │◀──────────────────│    COMPRAS      │ ← Cliente compra juego
└──────┬──────┘   juego_id        └──────┬──────────┘
       │                                  │
       │ juego_id                         │ compra_id
       │                                  │
┌──────▼────────────┐            ┌───────▼─────────────────┐
│ PUBLICACION_JUEGO │            │ MOVIMIENTO_PROVEEDOR    │ ← Proveedor gana dinero
└───────────────────┘            └─────────────────────────┘
       │
       │ juego_id, usuario_id
       │
┌──────▼─────────────┐
│ BIBLIOTECA_USUARIO │ ← Juegos del usuario
└────────────────────┘
```

---

## Flujo de una compra completa:

```
1. Cliente hace login → Tabla: USUARIOS (rol = CLIENTE)

2. Ve el catálogo → Tabla: JUEGOS (activo = true)

3. Compra un juego → Se crean 2 registros:
   
   a) Tabla: COMPRAS
      - usuario_id = ID del cliente
      - juego_id = ID del juego
      - precio_pagado = precio del juego
   
   b) Tabla: MOVIMIENTO_PROVEEDOR
      - proveedor_id = ID del proveedor
      - compra_id = ID de la compra
      - monto_neto = precio × 0.85 (85%)

4. El juego se añade a su biblioteca → Tabla: BIBLIOTECA_USUARIO
   - usuario_id = ID del cliente
   - juego_id = ID del juego
```

---

# 🔧 NOMBRES MEJORADOS (PROPUESTAS)

## Cambios sugeridos para mayor claridad:

| Tabla Actual | Nombre Mejorado | Razón |
|-------------|-----------------|-------|
| **movimiento_proveedor** | **ingresos_proveedor** | "Ingreso" es más claro que "movimiento" |
| **publicacion_juego** | **publicaciones** | Más corto, igualmente claro |
| **biblioteca_usuario** | **biblioteca** | Más corto, obvio que es de usuario |
| *(mantener)* **usuarios** | **usuarios** | ✅ Perfecto |
| *(mantener)* **juegos** | **juegos** | ✅ Perfecto |
| *(mantener)* **compras** | **compras** | ✅ Perfecto |

---

# 📊 QUERIES TÍPICAS EXPLICADAS

## 1. Ver biblioteca de un usuario:

```sql
SELECT j.titulo, b.fecha_adquisicion
FROM biblioteca_usuario b
JOIN juegos j ON b.juego_id = j.id
WHERE b.usuario_id = 1;
```

**¿Qué hace?**  
Muestra todos los juegos que tiene el usuario 1 en su biblioteca.

---

## 2. Ver ganancias de un proveedor:

```sql
SELECT SUM(monto_neto) AS total_ganancias
FROM movimiento_proveedor
WHERE proveedor_id = 2 AND pagado = false;
```

**¿Qué hace?**  
Suma todo el dinero pendiente de pagar al proveedor 2.

---

## 3. Ver compras de un cliente:

```sql
SELECT j.titulo, c.precio_pagado, c.fecha_compra
FROM compras c
JOIN juegos j ON c.juego_id = j.id
WHERE c.usuario_id = 1;
```

**¿Qué hace?**  
Muestra todas las compras del usuario 1 con detalles.

---

## 4. Calcular ganancias de la plataforma (15%):

```sql
SELECT SUM(precio_pagado * 0.15) AS ganancia_plataforma
FROM compras;
```

**¿Qué hace?**  
Suma el 15% de todas las ventas (lo que gana PixelShop).

---

## 5. Ver costos de publicación de un proveedor:

```sql
SELECT COUNT(*) AS juegos_publicados, COUNT(*) * 25 AS total_pagado
FROM publicacion_juego
WHERE proveedor_id = 2 AND pagado = true;
```

**¿Qué hace?**  
Cuenta cuántos juegos publicó el proveedor 2 y cuánto pagó en total (25€ × cantidad).

---

# 💰 EJEMPLO COMPLETO: Flujo de Dinero

## Escenario: Juan compra Cyberpunk 2077 por 59.99€

### 1️⃣ Se registra la compra:

**Tabla: COMPRAS**
```
id: 1
usuario_id: 1 (Juan)
juego_id: 1 (Cyberpunk)
precio_pagado: 59.99
fecha_compra: 2025-11-21 10:30
pagado_al_proveedor: false
```

### 2️⃣ Se registra el ingreso del proveedor:

**Tabla: MOVIMIENTO_PROVEEDOR**
```
id: 1
proveedor_id: 2 (Epic Games)
compra_id: 1
monto_neto: 50.99 (85% de 59.99€)
pagado: false
fecha: 2025-11-21 10:30
```

### 3️⃣ Se añade a la biblioteca:

**Tabla: BIBLIOTECA_USUARIO**
```
id: 1
usuario_id: 1 (Juan)
juego_id: 1 (Cyberpunk)
fecha_adquisicion: 2025-11-21 10:30
```

### 💸 Resumen financiero:

- **Cliente pagó:** 59.99€
- **Proveedor ganó:** 50.99€ (85%)
- **Plataforma ganó:** 9.00€ (15%)

---

# 📈 ESTADÍSTICAS DEL MODELO

## Métricas del modelo actual:

- **Total de tablas:** 6
- **Relaciones (Foreign Keys):** 8
- **Tablas con nombres confusos:** 3
  - `movimiento_proveedor`
  - `publicacion_juego`
  - `biblioteca_usuario`

---

# ✅ VENTAJAS DEL MODELO ACTUAL

1. ✅ **Separación clara** - Cada tabla tiene una responsabilidad
2. ✅ **Auditabilidad** - Cada transacción queda registrada
3. ✅ **Escalable** - Fácil añadir nuevas funcionalidades
4. ✅ **Relaciones bien definidas** - Uso correcto de Foreign Keys
5. ✅ **Restricciones UNIQUE** - Previene duplicados

---

# ⚠️ PROBLEMAS DEL MODELO ACTUAL

1. ❌ **Nombres confusos** - `movimiento_proveedor` no es intuitivo
2. ❌ **Nombres largos** - `biblioteca_usuario`, `publicacion_juego`
3. ❌ **No se entiende al primer vistazo** - Necesitas explicación

---

# 🎯 CONCLUSIÓN

Tu modelo de base de datos está **bien diseñado** pero tiene **nombres poco claros**.

**Recomendación:**
- Cambiar `movimiento_proveedor` → `ingresos_proveedor`
- Cambiar `publicacion_juego` → `publicaciones`
- Cambiar `biblioteca_usuario` → `biblioteca`

Esto haría tu base de datos **mucho más intuitiva** sin cambiar su funcionalidad.

---

**Fecha:** 21 Noviembre 2025  
**Versión:** 1.0
