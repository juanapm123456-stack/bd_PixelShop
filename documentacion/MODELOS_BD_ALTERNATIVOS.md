# 🗄️ MODELOS DE BASE DE DATOS ALTERNATIVOS

**PixelShop - Propuestas Simplificadas**

Este documento presenta **2 modelos de base de datos alternativos** más simples e intuitivos que el modelo actual.

---

## 📋 Modelo Actual (Complejo)

**Problema:** Nombres confusos y muchas tablas

```
usuarios
compras
juegos
movimiento_proveedor  ← Confuso
publicacion_juego     ← Confuso
biblioteca_usuario
```

---

# ✅ MODELO 1: SIMPLIFICADO (Recomendado)

## 🎯 Filosofía

- Nombres claros y directos
- Menos tablas, más columnas
- Fácil de entender
- Todo en español

---

## 📊 Tablas del Modelo 1

### **usuarios** (6 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
nombre          VARCHAR(255)    NOT NULL
email           VARCHAR(255)    UNIQUE NOT NULL
password        VARCHAR(255)    NOT NULL
rol             VARCHAR(20)     NOT NULL (ADMIN, PROVEEDOR, CLIENTE)
activo          BOOLEAN         DEFAULT true
```

**Ejemplo:**
```
1 | Juan Pérez | juan@email.com | $2a$10$... | CLIENTE | true
2 | Epic Games | epic@games.com | $2a$10$... | PROVEEDOR | true
3 | Admin      | admin@shop.com | $2a$10$... | ADMIN | true
```

---

### **juegos** (8 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
titulo          VARCHAR(255)    NOT NULL
descripcion     TEXT
precio          DECIMAL(10,2)   NOT NULL
genero          VARCHAR(50)
imagen          VARCHAR(500)    (URL de la imagen principal)
proveedor_id    BIGINT          FOREIGN KEY → usuarios(id)
activo          BOOLEAN         DEFAULT true
```

**Ejemplo:**
```
1 | Cyberpunk 2077 | RPG futurista... | 59.99 | RPG | url... | 2 | true
2 | FIFA 24        | Simulador...     | 69.99 | Deportes | url... | 2 | true
```

---

### **ventas** (6 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
juego_id        BIGINT          FOREIGN KEY → juegos(id)
cliente_id      BIGINT          FOREIGN KEY → usuarios(id)
precio_total    DECIMAL(10,2)   (lo que pagó el cliente)
fecha           DATETIME        DEFAULT NOW()
pagado          BOOLEAN         DEFAULT false (si el proveedor cobró)
```

**Ejemplo:**
```
1 | 1 (Cyberpunk) | 1 (Juan) | 59.99 | 2025-11-21 10:30 | false
2 | 2 (FIFA)      | 1 (Juan) | 69.99 | 2025-11-21 11:00 | false
```

**¿Qué representa?**
- Juan compró Cyberpunk por 59.99€
- Juan compró FIFA por 69.99€

---

### **ingresos** (6 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
venta_id        BIGINT          FOREIGN KEY → ventas(id)
proveedor_id    BIGINT          FOREIGN KEY → usuarios(id)
monto           DECIMAL(10,2)   (85% del precio)
fecha           DATETIME        DEFAULT NOW()
cobrado         BOOLEAN         DEFAULT false
```

**Ejemplo:**
```
1 | 1 | 2 (Epic Games) | 50.99 | 2025-11-21 10:30 | false
2 | 2 | 2 (Epic Games) | 59.49 | 2025-11-21 11:00 | false
```

**¿Qué representa?**
- Epic Games ganó 50.99€ (85% de 59.99€) por venta 1
- Epic Games ganó 59.49€ (85% de 69.99€) por venta 2

---

### **publicaciones** (5 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
juego_id        BIGINT          FOREIGN KEY → juegos(id)
proveedor_id    BIGINT          FOREIGN KEY → usuarios(id)
costo           DECIMAL(10,2)   DEFAULT 25.00
fecha           DATETIME        DEFAULT NOW()
```

**Ejemplo:**
```
1 | 1 (Cyberpunk) | 2 (Epic Games) | 25.00 | 2025-11-20 09:00
2 | 2 (FIFA)      | 2 (Epic Games) | 25.00 | 2025-11-20 09:30
```

**¿Qué representa?**
- Epic Games pagó 25€ para publicar Cyberpunk
- Epic Games pagó 25€ para publicar FIFA

---

### **biblioteca** (4 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
usuario_id      BIGINT          FOREIGN KEY → usuarios(id)
juego_id        BIGINT          FOREIGN KEY → juegos(id)
fecha           DATETIME        DEFAULT NOW()
```

**Ejemplo:**
```
1 | 1 (Juan) | 1 (Cyberpunk) | 2025-11-21 10:30
2 | 1 (Juan) | 2 (FIFA)      | 2025-11-21 11:00
```

**¿Qué representa?**
- Juan tiene Cyberpunk en su biblioteca
- Juan tiene FIFA en su biblioteca

---

## 🔍 Comparación con Modelo Actual

| Tabla Actual | Tabla Simplificada | Mejora |
|-------------|-------------------|--------|
| `compras` | `ventas` | ✅ Más claro |
| `movimiento_proveedor` | `ingresos` | ✅ Mucho más claro |
| `publicacion_juego` | `publicaciones` | ✅ Más corto |
| `biblioteca_usuario` | `biblioteca` | ✅ Más corto |

---

## 💡 Ventajas del Modelo 1

1. ✅ **Nombres en español** - Más natural para hispanohablantes
2. ✅ **Tabla "ingresos"** - Claro que es dinero para el proveedor
3. ✅ **Tabla "ventas"** - Todos entienden qué es una venta
4. ✅ **Menos columnas** - Solo lo esencial
5. ✅ **Más intuitivo** - Lees el nombre y sabes qué hace

---

## 📈 Ejemplo de Flujo: Cliente compra un juego

```
1. Cliente hace compra:
   INSERT INTO ventas (juego_id, cliente_id, precio_total)
   VALUES (1, 5, 59.99);

2. Se registra ingreso del proveedor:
   INSERT INTO ingresos (venta_id, proveedor_id, monto)
   VALUES (1, 2, 50.99);  -- 85% de 59.99

3. Se añade a biblioteca:
   INSERT INTO biblioteca (usuario_id, juego_id)
   VALUES (5, 1);
```

**Sencillo y directo** ✅

---

## 🗂️ Diagrama del Modelo 1

```
┌─────────────┐
│  usuarios   │
├─────────────┤
│ id          │
│ nombre      │
│ email       │
│ password    │
│ rol         │
│ activo      │
└──────┬──────┘
       │
       │ proveedor_id
       │
┌──────▼──────┐         ┌──────────────┐
│   juegos    │────────▶│ publicaciones│
├─────────────┤         ├──────────────┤
│ id          │         │ id           │
│ titulo      │ juego_id│ juego_id     │
│ descripcion │◀────────│ proveedor_id │
│ precio      │         │ costo        │
│ genero      │         │ fecha        │
│ imagen      │         └──────────────┘
│ proveedor_id│
│ activo      │
└──────┬──────┘
       │
       │ juego_id
       │
┌──────▼──────┐         ┌──────────────┐
│   ventas    │────────▶│   ingresos   │
├─────────────┤         ├──────────────┤
│ id          │ venta_id│ id           │
│ juego_id    │◀────────│ venta_id     │
│ cliente_id  │         │ proveedor_id │
│ precio_total│         │ monto        │
│ fecha       │         │ fecha        │
│ pagado      │         │ cobrado      │
└──────┬──────┘         └──────────────┘
       │
       │ juego_id, usuario_id
       │
┌──────▼──────┐
│ biblioteca  │
├─────────────┤
│ id          │
│ usuario_id  │
│ juego_id    │
│ fecha       │
└─────────────┘
```

---

# ✅ MODELO 2: ULTRA-SIMPLE (Minimalista)

## 🎯 Filosofía

- Lo mínimo indispensable
- Sin redundancias
- Máxima simplicidad
- Ideal para prototipos

---

## 📊 Tablas del Modelo 2

### **usuarios** (5 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
nombre          VARCHAR(255)    NOT NULL
email           VARCHAR(255)    UNIQUE NOT NULL
password        VARCHAR(255)    NOT NULL
tipo            VARCHAR(20)     NOT NULL (admin, proveedor, cliente)
```

**Ejemplo:**
```
1 | Juan Pérez | juan@email.com | $2a$10$... | cliente
2 | Epic Games | epic@games.com | $2a$10$... | proveedor
```

---

### **productos** (6 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
nombre          VARCHAR(255)    NOT NULL
info            TEXT            (descripción)
precio          DECIMAL(10,2)   NOT NULL
vendedor_id     BIGINT          FOREIGN KEY → usuarios(id)
foto            VARCHAR(500)    (URL)
```

**Ejemplo:**
```
1 | Cyberpunk 2077 | RPG futurista... | 59.99 | 2 | url...
2 | FIFA 24        | Simulador...     | 69.99 | 2 | url...
```

---

### **transacciones** (7 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
producto_id     BIGINT          FOREIGN KEY → productos(id)
comprador_id    BIGINT          FOREIGN KEY → usuarios(id)
vendedor_id     BIGINT          FOREIGN KEY → usuarios(id)
total           DECIMAL(10,2)   (precio completo)
ganancia        DECIMAL(10,2)   (85% para vendedor)
fecha           DATETIME        DEFAULT NOW()
```

**Ejemplo:**
```
1 | 1 (Cyberpunk) | 1 (Juan) | 2 (Epic) | 59.99 | 50.99 | 2025-11-21
```

**¿Qué representa?**
- En UNA sola fila:
  - Juan compró Cyberpunk de Epic Games
  - Precio: 59.99€
  - Epic Games ganó: 50.99€ (85%)

---

### **mis_productos** (3 campos)
```
id              BIGINT          PRIMARY KEY AUTO_INCREMENT
usuario_id      BIGINT          FOREIGN KEY → usuarios(id)
producto_id     BIGINT          FOREIGN KEY → productos(id)
```

**Ejemplo:**
```
1 | 1 (Juan) | 1 (Cyberpunk)
2 | 1 (Juan) | 2 (FIFA)
```

**¿Qué representa?**
- Juan tiene estos productos

---

## 🔍 Comparación: Modelo Actual vs Modelo 2

| Modelo Actual | Modelo 2 | Cambio |
|--------------|----------|--------|
| 6 tablas | 4 tablas | ✅ -33% |
| `movimiento_proveedor` + `compras` | `transacciones` | ✅ TODO en 1 |
| `biblioteca_usuario` | `mis_productos` | ✅ Más corto |
| `juegos` | `productos` | ✅ Más genérico |

---

## 💡 Ventajas del Modelo 2

1. ✅ **Solo 4 tablas** - Mínimo indispensable
2. ✅ **Una transacción = Una fila** - Todo en un registro
3. ✅ **Nombres cortísimos** - `usuarios`, `productos`, `transacciones`
4. ✅ **Sin tablas intermedias** - Menos joins
5. ✅ **Perfecta para MVP** - Lanzas rápido

---

## 📈 Ejemplo de Flujo: Cliente compra

```sql
-- TODO en UNA sola query:
INSERT INTO transacciones 
    (producto_id, comprador_id, vendedor_id, total, ganancia)
VALUES 
    (1, 5, 2, 59.99, 50.99);

-- Añadir a biblioteca:
INSERT INTO mis_productos (usuario_id, producto_id)
VALUES (5, 1);
```

**2 queries vs 5 del modelo actual** ✅

---

## 📊 Calcular Ganancias (Modelo 2)

### Ganancias del vendedor:
```sql
SELECT SUM(ganancia) 
FROM transacciones 
WHERE vendedor_id = 2;
```

### Ganancias de la plataforma (15%):
```sql
SELECT SUM(total - ganancia) 
FROM transacciones;
```

**Súper simple** ✅

---

## 🗂️ Diagrama del Modelo 2

```
┌─────────────┐
│  usuarios   │
├─────────────┤
│ id          │
│ nombre      │
│ email       │
│ password    │
│ tipo        │
└──────┬──────┘
       │
       │ vendedor_id
       │
┌──────▼──────────┐
│   productos     │
├─────────────────┤
│ id              │
│ nombre          │
│ info            │
│ precio          │
│ vendedor_id     │
│ foto            │
└──────┬──────────┘
       │
       │ producto_id
       │
┌──────▼──────────┐
│ transacciones   │
├─────────────────┤
│ id              │
│ producto_id     │
│ comprador_id    │
│ vendedor_id     │
│ total           │
│ ganancia        │
│ fecha           │
└─────────────────┘
       │
       │
┌──────▼──────────┐
│ mis_productos   │
├─────────────────┤
│ id              │
│ usuario_id      │
│ producto_id     │
└─────────────────┘
```

---

# 📊 COMPARACIÓN GENERAL

## Número de Tablas

| Modelo | Tablas | Reducción |
|--------|--------|-----------|
| **Actual** | 6 | - |
| **Modelo 1 (Simplificado)** | 6 | 0% (pero nombres más claros) |
| **Modelo 2 (Ultra-simple)** | 4 | -33% |

---

## Complejidad de Queries

### Obtener ventas de un proveedor:

**Modelo Actual:**
```sql
SELECT * FROM compras c
JOIN movimiento_proveedor m ON c.id = m.compra_id
WHERE m.proveedor_id = 2;
```

**Modelo 1:**
```sql
SELECT * FROM ventas v
JOIN ingresos i ON v.id = i.venta_id
WHERE i.proveedor_id = 2;
```

**Modelo 2:**
```sql
SELECT * FROM transacciones
WHERE vendedor_id = 2;
```

**Ganador: Modelo 2** 🏆 (1 tabla, sin joins)

---

## Claridad de Nombres

| Concepto | Actual | Modelo 1 | Modelo 2 |
|----------|--------|----------|----------|
| Usuario compra | `compras` | `ventas` | `transacciones` |
| Dinero proveedor | `movimiento_proveedor` ❌ | `ingresos` ✅ | `ganancia` (columna) ✅ |
| Productos usuario | `biblioteca_usuario` | `biblioteca` | `mis_productos` ✅ |
| Publicar juego | `publicacion_juego` | `publicaciones` | (no necesita tabla) |

---

# 🎯 RECOMENDACIONES

## Para Desarrollo Rápido (MVP)
👉 **Usar Modelo 2**
- 4 tablas
- Queries sencillas
- Lanzas en días

## Para Producción Completa
👉 **Usar Modelo 1**
- 6 tablas
- Nombres claros
- Escalable
- Fácil de mantener

## Para Aplicaciones Complejas
👉 **Modelo Actual**
- Mayor control
- Separación de responsabilidades
- Ideal para auditorías

---

# 📝 EJEMPLOS DE DATOS

## Modelo 1: Escenario Completo

### Usuarios:
```
1 | Juan Pérez | juan@email.com | ... | CLIENTE | true
2 | Epic Games | epic@games.com | ... | PROVEEDOR | true
```

### Juegos:
```
1 | Cyberpunk 2077 | RPG... | 59.99 | RPG | url | 2 | true
```

### Publicaciones:
```
1 | 1 | 2 | 25.00 | 2025-11-20
```
Epic Games pagó 25€ para publicar Cyberpunk

### Ventas:
```
1 | 1 | 1 | 59.99 | 2025-11-21 | false
```
Juan compró Cyberpunk por 59.99€

### Ingresos:
```
1 | 1 | 2 | 50.99 | 2025-11-21 | false
```
Epic Games ganó 50.99€ (85% de 59.99€)

### Biblioteca:
```
1 | 1 | 1 | 2025-11-21
```
Juan tiene Cyberpunk en su biblioteca

---

## Modelo 2: Escenario Completo

### Usuarios:
```
1 | Juan Pérez | juan@email.com | ... | cliente
2 | Epic Games | epic@games.com | ... | proveedor
```

### Productos:
```
1 | Cyberpunk 2077 | RPG... | 59.99 | 2 | url
```

### Transacciones:
```
1 | 1 | 1 | 2 | 59.99 | 50.99 | 2025-11-21
```
TODO en una fila:
- Producto: Cyberpunk
- Comprador: Juan
- Vendedor: Epic Games
- Total: 59.99€
- Ganancia vendedor: 50.99€

### Mis Productos:
```
1 | 1 | 1
```
Juan tiene Cyberpunk

---

# ✅ CONCLUSIÓN

## Mejor Opción para PixelShop:

### 🥇 **Modelo 1 (Simplificado)** - RECOMENDADO

**Por qué:**
- ✅ Nombres **súper claros**: `ventas`, `ingresos`, `publicaciones`
- ✅ Misma estructura que el actual (6 tablas)
- ✅ Fácil de migrar desde el código actual
- ✅ Balance perfecto: simple pero completo
- ✅ Nombres en español = más natural

**Cambios mínimos:**
- Renombrar tablas
- Renombrar columnas
- **El código casi no cambia**

---

### 🥈 **Modelo 2 (Ultra-simple)** - Para Prototipos

**Por qué:**
- ✅ Solo 4 tablas
- ✅ Queries ultra-simples
- ✅ Perfecto para MVP
- ❌ Menos flexible para auditorías
- ❌ Mezcla conceptos en `transacciones`

---

### 🥉 **Modelo Actual** - Mantenerlo si ya funciona

**Por qué:**
- ✅ Ya está implementado
- ✅ Funciona correctamente
- ❌ Nombres confusos (`movimiento_proveedor`)
- ❌ Difícil de explicar a otros devs

---

## 🎓 Aprendizaje

La mejor base de datos es la que:
1. ✅ **Se entiende fácilmente** - Nombres claros
2. ✅ **Resuelve el problema** - Funcional
3. ✅ **Es mantenible** - Otros devs la entienden
4. ✅ **Escala bien** - Crece con el proyecto

**No siempre "más tablas" = mejor**  
**No siempre "menos tablas" = mejor**

**Lo importante: CLARIDAD** 💡

---

**Fecha:** 21 Noviembre 2025  
**Versión:** 1.0
