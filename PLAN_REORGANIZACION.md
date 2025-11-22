# 🔧 Plan de Reorganización del Proyecto - Pixel Shop

## 📋 Análisis del Estado Actual

### Problemas Identificados

#### 1. **Arquitectura Desorganizada**
- ❌ Lógica de negocio mezclada en Controllers
- ❌ Capa de Service inconsistente (algunos existen, otros no)
- ❌ Controllers acceden directamente a múltiples Repositories
- ❌ Código duplicado entre Controllers

#### 2. **Modelo de Datos Redundante**
- ❌ `biblioteca_usuario` duplica información de `compra`
- ❌ `publicacion_juego` solo almacena un dato (25€)
- ❌ `movimiento_proveedor` podría calcularse dinámicamente
- ❌ Imágenes como columnas separadas (`imagen_url_1/2/3/4`)

#### 3. **Falta de Separación de Responsabilidades**
- ❌ Un Controller hace demasiado (validar, procesar, enviar email, etc.)
- ❌ No hay DTOs consistentes (algunos existen, no se usan)
- ❌ Validaciones dispersas (BD, Backend, Frontend)

#### 4. **Sin Auditoría ni Trazabilidad**
- ❌ No se registra quién creó/modificó registros
- ❌ No hay timestamps de actualización
- ❌ Eliminación física (pérdida de datos históricos)

#### 5. **Escalabilidad Limitada**
- ❌ Sin caché implementado
- ❌ Consultas sin optimizar (N+1 queries posibles)
- ❌ Sin paginación en listados grandes

---

## 🏗️ Propuesta de Reorganización

### Nueva Arquitectura en Capas

```
┌─────────────────────────────────────┐
│          PRESENTATION LAYER         │
│  (Controllers + Thymeleaf Views)    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         APPLICATION LAYER           │
│     (Services - Lógica de Negocio)  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│          PERSISTENCE LAYER          │
│   (Repositories + Entities/Models)  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│          DATABASE (MySQL)           │
└─────────────────────────────────────┘
```

---

## 📦 Reorganización de Paquetes

### Estructura Actual
```
com.example/
├── controller/
├── model/
├── repository/
├── service/
├── config/
└── dto/
```

### Estructura Propuesta
```
com.example.pixelshop/
│
├── application/                    # Capa de Aplicación
│   ├── controller/                 # Controllers REST/MVC
│   │   ├── web/                    # Controllers para vistas (Thymeleaf)
│   │   │   ├── CatalogoController
│   │   │   ├── PerfilController
│   │   │   └── ...
│   │   └── api/                    # Controllers REST (futuro API)
│   │       └── ...
│   │
│   └── dto/                        # Data Transfer Objects
│       ├── request/
│       │   ├── RegistroUsuarioRequest
│       │   ├── PublicarJuegoRequest
│       │   └── ...
│       └── response/
│           ├── JuegoDetalleResponse
│           ├── CompraResponse
│           └── ...
│
├── domain/                         # Capa de Dominio
│   ├── model/                      # Entidades del negocio
│   │   ├── Usuario
│   │   ├── Juego
│   │   ├── Compra
│   │   └── ...
│   │
│   ├── repository/                 # Interfaces de Repositorios
│   │   ├── IUsuarioRepository
│   │   ├── IJuegoRepository
│   │   └── ...
│   │
│   ├── service/                    # Interfaces de Servicios
│   │   ├── IUsuarioService
│   │   ├── ICompraService
│   │   ├── IJuegoService
│   │   └── ...
│   │
│   └── exception/                  # Excepciones personalizadas
│       ├── UsuarioNoEncontradoException
│       ├── CompraDuplicadaException
│       └── ...
│
├── infrastructure/                 # Capa de Infraestructura
│   ├── service/impl/               # Implementaciones de Servicios
│   │   ├── UsuarioServiceImpl
│   │   ├── CompraServiceImpl
│   │   ├── JuegoServiceImpl
│   │   └── ...
│   │
│   ├── external/                   # Servicios externos
│   │   ├── paypal/
│   │   │   ├── PayPalService
│   │   │   └── PayPalPayoutService
│   │   ├── cloudinary/
│   │   │   └── CloudinaryService
│   │   ├── email/
│   │   │   └── EmailService
│   │   └── oauth/
│   │       └── CustomOAuth2UserService
│   │
│   └── config/                     # Configuraciones
│       ├── SecurityConfig
│       ├── PayPalConfig
│       ├── CloudinaryConfig
│       └── ...
│
└── shared/                         # Código compartido
    ├── util/                       # Utilidades
    │   ├── DateUtil
    │   ├── PriceCalculator
    │   └── ...
    │
    ├── constant/                   # Constantes
    │   ├── Roles
    │   ├── MetodoPago
    │   └── ...
    │
    └── validator/                  # Validadores custom
        ├── EmailValidator
        ├── PrecioValidator
        └── ...
```

---

## 🗃️ Rediseño de Base de Datos

### Tabla: `usuario` (Simplificada)
**ANTES:**
```
- id
- nombre
- email
- password
- rol
- datos_fiscales       ← Solo proveedores
- email_paypal         ← Solo proveedores
- fecha_registro
```

**DESPUÉS:**
```
- id
- nombre
- email
- password
- rol
- url_foto_perfil
- activo
- created_at
- updated_at
```

**Justificación:**
- Datos específicos de proveedor van a tabla separada
- Se agrega campo `activo` para soft delete
- Se agregan timestamps de auditoría

---

### Nueva Tabla: `proveedor_datos` (One-to-One)
```
- id
- usuario_id (FK, UNIQUE)
- cif_nif
- direccion_fiscal
- telefono_contacto
- email_paypal
- cuenta_bancaria_iban
- verificado (Boolean)
- fecha_verificacion
- created_at
- updated_at
```

**Justificación:**
- Separar datos específicos de proveedores
- Facilitar auditoría de datos fiscales
- Permitir verificación de proveedor (futuro)

---

### Tabla: `juego` (Normalizada)
**ANTES:**
```
- id
- titulo
- descripcion
- precio
- genero
- imagen_url_1         ← Columnas separadas
- imagen_url_2
- imagen_url_3
- imagen_url_4
- video_youtube_url
- proveedor_id
- fecha_publicacion
```

**DESPUÉS:**
```
- id
- titulo
- descripcion
- precio_actual
- genero
- proveedor_id (FK)
- video_url
- fecha_publicacion
- fecha_pago_publicacion  ← Nuevo
- monto_pago_publicacion  ← Nuevo (siempre 25€)
- activo
- destacado (Boolean)
- created_at
- updated_at
- deleted_at (nullable)
```

**Justificación:**
- Imágenes van a tabla separada
- Se incorpora info de `publicacion_juego`
- Se agrega soft delete
- Campo `destacado` para featured games

---

### Nueva Tabla: `imagen_juego` (One-to-Many)
```
- id
- juego_id (FK)
- url
- public_id_cloudinary
- orden (Integer: 1=portada, 2-4=capturas)
- tipo (Enum: PORTADA, CAPTURA, LOGO)
- created_at
```

**Justificación:**
- Flexibilidad para agregar más imágenes
- Orden personalizable
- Identificar tipo de imagen

---

### Tabla: `compra` (Optimizada)
**ANTES:**
```
- id
- usuario_id
- juego_id
- precio_pagado
- fecha_compra
- metodo_pago
```

**DESPUÉS:**
```
- id
- usuario_id (FK)
- juego_id (FK)
- precio_pagado
- metodo_pago (Enum: PAYPAL, TARJETA, REGALO)
- estado (Enum: PENDIENTE, COMPLETADA, REEMBOLSADA)
- order_id_paypal
- email_comprador  ← Para auditoría
- fecha_compra
- created_at
- updated_at
```

**Justificación:**
- Se agrega `estado` para gestión de reembolsos
- Se guarda `order_id_paypal` para tracking
- Email del comprador (puede cambiar su email de usuario)

---

### ❌ ELIMINAR: Tabla `biblioteca_usuario`
**Razón:** Duplica información de `compra`

**Alternativa:** Query para obtener biblioteca
```sql
SELECT j.* FROM juego j
INNER JOIN compra c ON j.id = c.juego_id
WHERE c.usuario_id = ? AND c.estado = 'COMPLETADA'
```

**¿Cuándo mantenerla?**
- Si se planea implementar regalos
- Si se planea implementar suscripciones
- Si se quiere cache de biblioteca (rendimiento)

**Decisión:** Eliminar por ahora, agregar después si es necesario

---

### ❌ ELIMINAR: Tabla `publicacion_juego`
**Razón:** Solo almacena 1 dato (25€)

**Alternativa:** Campos en tabla `juego`
- `fecha_pago_publicacion`
- `monto_pago_publicacion`

---

### Tabla: `movimiento_proveedor` (Renombrada y Optimizada)
**ANTES:**
```
- id
- proveedor_id
- juego_id
- compra_id
- monto
- estado (PENDIENTE, PAGADO)
- fecha_venta
- fecha_pago
```

**DESPUÉS:** `transaccion_proveedor`
```
- id
- proveedor_id (FK)
- juego_id (FK)
- compra_id (FK)
- monto_bruto (100%)
- comision_plataforma (15%)
- monto_neto (85%)
- estado (Enum: PENDIENTE, PROCESANDO, PAGADO, RECHAZADO)
- metodo_pago (Enum: PAYPAL_PAYOUT, TRANSFERENCIA)
- referencia_pago (String: ID de PayPal Payout)
- fecha_venta
- fecha_procesamiento
- fecha_pago
- created_at
- updated_at
```

**Justificación:**
- Más descriptivo (`transaccion_proveedor`)
- Separar montos: bruto, comisión, neto
- Agregar estados intermedios
- Tracking de referencia de pago

---

### Nueva Tabla: `transaccion_plataforma`
```
- id
- tipo (Enum: COMISION_VENTA, PAGO_PUBLICACION, PAGO_PROVEEDOR)
- monto
- juego_id (FK, nullable)
- compra_id (FK, nullable)
- transaccion_proveedor_id (FK, nullable)
- descripcion
- fecha
- created_at
```

**Justificación:**
- Centralizar todos los ingresos/egresos de la plataforma
- Facilitar cálculo de ganancias
- Auditoría completa

---

### Nueva Tabla: `valoracion_juego` (Futuro)
```
- id
- usuario_id (FK)
- juego_id (FK)
- puntuacion (Integer 1-5)
- comentario (Text, nullable)
- fecha_valoracion
- created_at
- updated_at
- UNIQUE(usuario_id, juego_id)
```

**Justificación:**
- Sistema de reseñas como Steam
- Solo usuarios que compraron pueden valorar

---

### Nueva Tabla: `descarga_juego` (Futuro)
```
- id
- usuario_id (FK)
- juego_id (FK)
- version_archivo
- fecha_descarga
- ip_address
- user_agent
```

**Justificación:**
- Auditoría de descargas
- Estadísticas para proveedor
- Detectar abuso

---

## 🏛️ Arquitectura de Servicios

### Principio: Interface + Implementación

Cada servicio tendrá:
1. **Interface** (`domain/service/IXxxService.java`)
2. **Implementación** (`infrastructure/service/impl/XxxServiceImpl.java`)

---

### Servicio: Usuario

#### Interface: `IUsuarioService`
```java
public interface IUsuarioService {
    UsuarioDTO registrar(RegistroUsuarioRequest request);
    UsuarioDTO buscarPorEmail(String email);
    UsuarioDTO buscarPorId(Long id);
    UsuarioDTO actualizarPerfil(Long id, ActualizarPerfilRequest request);
    void eliminar(Long id);
    List<UsuarioDTO> listarTodos();
    boolean existeEmail(String email);
}
```

#### Implementación: `UsuarioServiceImpl`
```java
@Service
public class UsuarioServiceImpl implements IUsuarioService {
    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private IEmailService emailService;
    
    @Override
    @Transactional
    public UsuarioDTO registrar(RegistroUsuarioRequest request) {
        // Validar email único
        // Encriptar password
        // Guardar usuario
        // Enviar email de bienvenida
        // Retornar DTO
    }
    
    // ... implementaciones de otros métodos
}
```

---

### Servicio: Juego

#### Interface: `IJuegoService`
```java
public interface IJuegoService {
    JuegoDTO publicar(PublicarJuegoRequest request, Long proveedorId);
    JuegoDTO buscarPorId(Long id);
    List<JuegoDTO> listarTodos();
    List<JuegoDTO> listarPorProveedor(Long proveedorId);
    List<JuegoDTO> buscarPorTitulo(String titulo);
    JuegoDTO actualizar(Long id, ActualizarJuegoRequest request);
    void eliminar(Long id);
    List<ImagenJuegoDTO> agregarImagenes(Long juegoId, List<MultipartFile> imagenes);
}
```

---

### Servicio: Compra

#### Interface: `ICompraService`
```java
public interface ICompraService {
    // Validaciones pre-compra
    void validarCompra(Long usuarioId, Long juegoId);
    
    // Proceso de compra
    CompraDTO procesarCompra(Long usuarioId, Long juegoId, String orderIdPayPal);
    
    // Consultas
    List<CompraDTO> obtenerComprasUsuario(Long usuarioId);
    boolean usuarioComproJuego(Long usuarioId, Long juegoId);
    
    // Biblioteca (reemplaza BibliotecaUsuarioService)
    List<JuegoDTO> obtenerBibliotecaUsuario(Long usuarioId);
}
```

#### Implementación: `CompraServiceImpl`
```java
@Service
public class CompraServiceImpl implements ICompraService {
    @Autowired private ICompraRepository compraRepository;
    @Autowired private IJuegoRepository juegoRepository;
    @Autowired private ITransaccionProveedorService transaccionProveedorService;
    @Autowired private ITransaccionPlataformaService transaccionPlataformaService;
    @Autowired private IEmailService emailService;
    @Autowired private IPayPalService paypalService;
    
    @Override
    @Transactional
    public CompraDTO procesarCompra(Long usuarioId, Long juegoId, String orderIdPayPal) {
        // 1. Validar compra (no duplicada, juego existe, etc.)
        validarCompra(usuarioId, juegoId);
        
        // 2. Capturar pago en PayPal
        PayPalCaptureResponse paypalResponse = paypalService.captureOrder(orderIdPayPal);
        
        // 3. Verificar pago completado
        if (!paypalResponse.getStatus().equals("COMPLETED")) {
            throw new PagoNoCompletadoException();
        }
        
        // 4. Crear compra
        Compra compra = new Compra();
        compra.setUsuarioId(usuarioId);
        compra.setJuegoId(juegoId);
        compra.setPrecioPagado(paypalResponse.getAmount());
        compra.setEstado(EstadoCompra.COMPLETADA);
        compra.setOrderIdPaypal(orderIdPayPal);
        compra = compraRepository.save(compra);
        
        // 5. Registrar transacción del proveedor (85%)
        transaccionProveedorService.registrarVenta(compra);
        
        // 6. Registrar comisión de plataforma (15%)
        transaccionPlataformaService.registrarComision(compra);
        
        // 7. Enviar emails
        emailService.enviarConfirmacionCompra(usuarioId, juegoId);
        emailService.notificarVentaProveedor(compra.getProveedorId(), juegoId);
        
        // 8. Retornar DTO
        return convertirADTO(compra);
    }
    
    // ... otros métodos
}
```

---

### Servicio: Transacción Proveedor

#### Interface: `ITransaccionProveedorService`
```java
public interface ITransaccionProveedorService {
    TransaccionProveedorDTO registrarVenta(Compra compra);
    List<TransaccionProveedorDTO> obtenerPendientes(Long proveedorId);
    List<TransaccionProveedorDTO> obtenerPagadas(Long proveedorId);
    void procesarPago(Long transaccionId);
    BigDecimal calcularTotalPendiente(Long proveedorId);
    BigDecimal calcularTotalPagado(Long proveedorId);
}
```

---

### Servicio: Transacción Plataforma

#### Interface: `ITransaccionPlataformaService`
```java
public interface ITransaccionPlataformaService {
    void registrarComision(Compra compra);
    void registrarPagoPublicacion(Long juegoId, BigDecimal monto);
    void registrarPagoProveedor(TransaccionProveedor transaccion);
    BigDecimal calcularGananciasTotales();
    BigDecimal calcularGananciasPorPeriodo(LocalDate inicio, LocalDate fin);
}
```

---

### Servicio: PayPal (Externo)

#### Interface: `IPayPalService`
```java
public interface IPayPalService {
    String createOrder(BigDecimal amount, String currency);
    PayPalCaptureResponse captureOrder(String orderId);
    PayPalPayoutResponse sendPayout(String emailPaypal, BigDecimal amount, String note);
}
```

---

### Servicio: Email (Externo)

#### Interface: `IEmailService`
```java
public interface IEmailService {
    void enviarBienvenida(String email, String nombre, Rol rol);
    void enviarConfirmacionCompra(Long usuarioId, Long juegoId);
    void notificarVentaProveedor(Long proveedorId, Long juegoId);
    void enviarRecuperacionPassword(String email, String token);
}
```

---

### Servicio: Cloudinary (Externo)

#### Interface: `ICloudinaryService`
```java
public interface ICloudinaryService {
    String subirImagen(MultipartFile file, String carpeta);
    void eliminarImagen(String publicId);
    String subirArchivo(MultipartFile file, String carpeta);
}
```

---

## 🔄 Flujo de una Compra (Reorganizado)

### Antes (Desorganizado)
```
CompraController:
├── Valida usuario autenticado
├── Busca juego en BD
├── Verifica compra duplicada
├── Verifica no es su propio juego
├── Crea orden en PayPal
├── Captura pago
├── Crea compra
├── Añade a biblioteca
├── Calcula comisiones
├── Crea movimiento proveedor
├── Envía email cliente
└── Envía email proveedor
```
**Problema:** 200+ líneas de código en un solo método

---

### Después (Limpio y Organizado)
```
CompraController (15 líneas):
├── Recibe request
├── Valida parámetros básicos
└── Llama a CompraService.procesarCompra()

CompraServiceImpl.procesarCompra():
├── Valida lógica de negocio
├── Llama a PayPalService.captureOrder()
├── Guarda compra en BD
├── Llama a TransaccionProveedorService.registrarVenta()
├── Llama a TransaccionPlataformaService.registrarComision()
├── Llama a EmailService.enviarConfirmacionCompra()
└── Retorna CompraDTO

Cada servicio hace SU responsabilidad únicamente
```

---

## 📊 Comparación: Antes vs Después

### Cantidad de Archivos

**Antes:**
```
Controllers: 10
Services: 6 (incompletos)
Repositories: 6
Models: 7
DTOs: 3 (no se usan)
Config: 5
Total: 37 clases
```

**Después:**
```
Controllers: 12 (separados web/api)
Service Interfaces: 12
Service Implementations: 12
Repositories: 10
Models: 10 (optimizados)
DTOs: 20 (request/response)
Config: 5
Utils: 5
Validators: 3
Exceptions: 5
Total: ~94 clases
```

**Sí, más archivos pero:**
- ✅ Cada clase hace UNA cosa
- ✅ Fácil de testear
- ✅ Fácil de mantener
- ✅ Código reutilizable
- ✅ Nuevos devs entienden rápido

---

## 🎯 Prioridades de Refactoring

### Fase 1: Base (2-3 días)
1. ✅ Crear estructura de carpetas
2. ✅ Crear interfaces de servicios
3. ✅ Mover lógica de controllers a services
4. ✅ Crear DTOs request/response
5. ✅ Implementar excepciones custom

### Fase 2: Base de Datos (1-2 días)
1. ✅ Crear tabla `proveedor_datos`
2. ✅ Crear tabla `imagen_juego`
3. ✅ Renombrar tabla `movimiento_proveedor` → `transaccion_proveedor`
4. ✅ Crear tabla `transaccion_plataforma`
5. ✅ Migrar datos existentes
6. ✅ Eliminar tablas obsoletas

### Fase 3: Servicios Críticos (2-3 días)
1. ✅ `UsuarioServiceImpl`
2. ✅ `CompraServiceImpl`
3. ✅ `JuegoServiceImpl`
4. ✅ `TransaccionProveedorServiceImpl`
5. ✅ `TransaccionPlataformaServiceImpl`

### Fase 4: Testing (2-3 días)
1. ✅ Unit tests para services
2. ✅ Integration tests para flujos completos
3. ✅ Tests de seguridad

### Fase 5: Frontend (1-2 días)
1. ✅ Actualizar controllers para usar nuevos services
2. ✅ Actualizar vistas Thymeleaf
3. ✅ Testing manual completo

---

## 💡 Conclusión

La reorganización propuesta transforma Pixel Shop de un proyecto funcional pero desorganizado a una aplicación empresarial escalable y mantenible.

**Beneficios clave:**
✅ Separación clara de responsabilidades
✅ Código testeable y reutilizable
✅ Base de datos normalizada
✅ Fácil agregar nuevas funcionalidades
✅ Fácil onboarding de nuevos desarrolladores
✅ Preparado para escalar

**Esfuerzo estimado:**
- **Total:** 10-13 días de desarrollo
- **Recompensa:** Base sólida para los próximos años

🚀 **Un proyecto bien organizado es un proyecto que crece sin dolor.**
