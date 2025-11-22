# Funcionalidades Futuras - Pixel Shop

## 📦 1. Subida y Descarga de Juegos (Archivos ZIP)

### Descripción
Permitir que los proveedores suban archivos ZIP de sus juegos y que los compradores autorizados puedan descargarlos después de la compra.

### Requisitos Técnicos

#### Almacenamiento
- **Opción A: Cloudinary** (ya implementado en el proyecto)
  - Ventaja: Ya está configurado
  - Desventaja: Límite de tamaño de archivo (100MB en plan gratuito)
  - Coste: Plan Pro ($89/mes) permite hasta 5GB por archivo
  
- **Opción B: Amazon S3** (Recomendado)
  - Sin límite de tamaño de archivo
  - Pago por uso ($0.023 por GB/mes)
  - Velocidad de descarga superior
  - Integración con Spring Boot mediante AWS SDK
  
- **Opción C: Azure Blob Storage**
  - Similar a S3, bien integrado con Spring Boot
  - Buena opción si ya usas servicios Azure (como tu MySQL)
  
- **Opción D: Servidor propio/NAS**
  - Control total
  - Requiere gestión de infraestructura
  - Problemas de escalabilidad

#### Cambios en Base de Datos

**Nueva tabla: `archivos_juego`**
```
- id (PK)
- juego_id (FK -> juego)
- url_descarga (String)
- nombre_archivo (String)
- tamanio_bytes (Long)
- hash_md5 (String) // Para verificar integridad
- version (String)
- fecha_subida (DateTime)
- activo (Boolean)
```

**Modificación en tabla `juego`**
```
+ tiene_archivo_descargable (Boolean)
+ version_actual (String)
```

#### Backend - Nuevos Componentes

**1. Service: `ArchivoJuegoService`**
- Métodos necesarios:
  - `subirArchivo(MultipartFile, Long juegoId)` → Sube archivo a S3/Cloudinary
  - `generarUrlDescarga(Long juegoId, Long usuarioId)` → URL temporal firmada
  - `verificarAccesoDescarga(Long juegoId, Long usuarioId)` → Valida compra
  - `eliminarArchivo(Long archivoId)` → Borra archivo antiguo
  - `obtenerEstadisticasDescarga(Long juegoId)` → Para proveedor

**2. Controller: Modificar `ProveedorController`**
- Agregar endpoint POST `/proveedor/subir-archivo/{juegoId}`
- Validar tamaño máximo (ej: 10GB)
- Progreso de subida con WebSocket (opcional)

**3. Controller: Nuevo `DescargaController`**
- Endpoint GET `/descargar/{juegoId}`
- Validaciones:
  - Usuario autenticado
  - Usuario compró el juego O es el proveedor
  - Archivo existe y está activo
- Retornar redirect a URL firmada con expiración (1 hora)

**4. Security**
- Generar URLs firmadas temporales (evitar descarga directa)
- Token JWT o firma HMAC en la URL
- Expiración de 1-2 horas

#### Frontend - Nuevas Vistas

**1. Formulario de subida (Proveedor)**
- Input file con validación de formato (.zip)
- Barra de progreso de subida
- Límite de tamaño visible (ej: "Máximo 10GB")
- Vista previa del archivo seleccionado

**2. Sección de descarga (Usuario/Proveedor)**
- Botón "Descargar Juego" en biblioteca
- Mostrar tamaño del archivo
- Versión del juego
- Fecha de última actualización

**3. Gestión de versiones (Proveedor)**
- Lista de versiones subidas
- Marcar versión como actual
- Eliminar versiones antiguas

#### Configuraciones Adicionales

**application.properties**
```properties
# AWS S3
aws.s3.access-key=${AWS_ACCESS_KEY}
aws.s3.secret-key=${AWS_SECRET_KEY}
aws.s3.bucket-name=pixelshop-games
aws.s3.region=eu-west-1

# Tamaño máximo de archivo
spring.servlet.multipart.max-file-size=10GB
spring.servlet.multipart.max-request-size=10GB
```

**pom.xml - Nuevas dependencias**
```xml
<!-- AWS SDK para S3 -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.x</version>
</dependency>
```

#### Consideraciones de Seguridad
- ✅ Validar extensión del archivo (.zip únicamente)
- ✅ Escaneo antivirus (ClamAV o AWS Macie)
- ✅ Límite de tamaño por proveedor
- ✅ URLs firmadas con expiración
- ✅ Registro de descargas (auditoría)
- ✅ Rate limiting (evitar abuso de descargas)

#### Flujo de Usuario

**Proveedor:**
1. Va a "Mis Juegos"
2. Selecciona juego publicado
3. Click en "Subir Archivo del Juego"
4. Selecciona archivo ZIP (máximo 10GB)
5. Espera progreso de subida
6. Confirma versión y activa descarga

**Comprador:**
1. Compra juego
2. Va a "Mi Biblioteca"
3. Ve botón "Descargar Juego" (si disponible)
4. Click genera URL temporal
5. Descarga archivo ZIP
6. Puede descargar cuantas veces quiera (con límite de rate)

---

## 📸 2. Foto de Perfil de Usuario

### Descripción
Permitir que los usuarios suban una imagen como foto de perfil personalizada.

### Requisitos Técnicos

#### Almacenamiento
- **Cloudinary** (ya implementado)
  - Perfecto para imágenes
  - Transformaciones automáticas (resize, crop, optimización)
  - CDN integrado para carga rápida
  - Ya tienes la configuración en el proyecto

#### Cambios en Base de Datos

**Modificación en tabla `usuario`**
```
+ url_foto_perfil (String, nullable)
+ public_id_foto_perfil (String, nullable) // Para eliminar de Cloudinary
```

#### Backend - Nuevos Componentes

**1. Service: Modificar `CloudinaryService`**
- Método existente `uploadImage()` ya sirve
- Agregar método específico:
  - `subirFotoPerfil(MultipartFile, Long usuarioId)` 
    - Valida formato (jpg, png, gif, webp)
    - Valida tamaño (máximo 5MB)
    - Redimensiona a 400x400px (circular)
    - Sube a carpeta `perfiles/`
    - Elimina foto anterior si existe
  - `eliminarFotoPerfil(String publicId)`

**2. Controller: Modificar `PerfilController`**
- Endpoint POST `/perfil/subir-foto`
  - Recibe MultipartFile
  - Valida extensión e tamaño
  - Llama a CloudinaryService
  - Actualiza usuario en BD
  - Retorna URL de la nueva foto
  
- Endpoint DELETE `/perfil/eliminar-foto`
  - Elimina de Cloudinary
  - Pone campo `url_foto_perfil` a null
  - Muestra imagen por defecto

#### Frontend - Modificaciones

**1. Vista de perfil (`perfil.html`)**
- Agregar sección "Foto de Perfil" arriba del formulario
- Imagen circular actual (o placeholder si no tiene)
- Botón "Cambiar Foto"
- Modal o input file con preview
- Botón "Eliminar Foto" (si tiene foto)

**2. Header/Navbar (`header.html`)**
- Mostrar foto de perfil en el menú dropdown del usuario
- Si no tiene foto, mostrar icono por defecto `fas fa-user-circle`

**3. Vista de proveedor en juegos**
- Mostrar foto de perfil del proveedor en detalles del juego (opcional)

#### Validaciones Frontend
```javascript
// Ejemplo de validaciones
- Formato: jpg, jpeg, png, gif, webp
- Tamaño: máximo 5MB
- Dimensiones mínimas: 200x200px
- Preview antes de subir
```

#### Consideraciones de Seguridad
- ✅ Validar MIME type real (no solo extensión)
- ✅ Limitar tamaño (5MB máximo)
- ✅ Sanitizar nombre de archivo
- ✅ Usar public_id aleatorio en Cloudinary
- ✅ Transformaciones automáticas (evitar archivos gigantes)
- ✅ Rate limiting (máximo 5 cambios por día)

#### Flujo de Usuario

**Subir foto:**
1. Usuario va a "Mi Perfil"
2. Click en "Cambiar Foto de Perfil"
3. Selecciona imagen de su PC
4. Preview de cómo se verá (circular, 400x400)
5. Click en "Guardar"
6. Foto se sube a Cloudinary
7. Se actualiza en BD y navbar

**Eliminar foto:**
1. Usuario va a "Mi Perfil"
2. Click en "Eliminar Foto de Perfil"
3. Confirmación
4. Se elimina de Cloudinary
5. Vuelve a icono por defecto

#### Mejoras Opcionales
- **Recorte de imagen**: Usar librería JavaScript como Cropper.js para que el usuario recorte su foto antes de subir
- **Foto desde cámara**: Permitir tomar foto con webcam (navigator.mediaDevices API)
- **Compresión cliente**: Comprimir imagen en el navegador antes de subir (reduce tiempo)
- **Foto desde URL**: Permitir pegar URL de imagen externa
- **Avatar por defecto**: Generar avatar con iniciales del nombre si no tiene foto

---

## 📊 Resumen de Implementación

### Prioridad Alta
1. ✅ Foto de perfil (más simple, usa infraestructura existente)

### Prioridad Media
2. ⚠️ Descarga de juegos (requiere S3/Azure, más complejo)

### Tiempo Estimado
- **Foto de perfil**: 4-6 horas
  - Backend: 2 horas
  - Frontend: 2 horas
  - Testing: 1 hora
  - Deploy: 1 hora

- **Descarga de juegos**: 15-20 horas
  - Configuración S3: 2 horas
  - Backend: 8 horas
  - Frontend: 4 horas
  - Testing: 3 horas
  - Seguridad: 2 horas
  - Deploy: 1 hora

### Costos Estimados

**Foto de Perfil:**
- $0 adicional (usa Cloudinary actual)

**Descarga de Juegos:**
- AWS S3: ~$0.50/mes por 100 juegos (50MB c/u)
- Transferencia: $0.09/GB descargado
- Estimado: $5-20/mes dependiendo del tráfico

---

## 🚀 Orden Recomendado de Implementación

1. **Fase 1**: Foto de perfil
   - Más rápido de implementar
   - Mejora UX inmediatamente
   - Sin costos adicionales
   - Sirve para familiarizarse con Cloudinary

2. **Fase 2**: Infraestructura de descarga
   - Configurar AWS S3 o Azure Blob
   - Crear servicio de gestión de archivos
   - Implementar seguridad (URLs firmadas)

3. **Fase 3**: Subida de juegos (Proveedor)
   - Formulario con progreso
   - Validaciones robustas
   - Gestión de versiones

4. **Fase 4**: Descarga de juegos (Usuario)
   - Botón en biblioteca
   - Validación de compra
   - Sistema de auditoría

5. **Fase 5**: Mejoras
   - Rate limiting
   - Estadísticas de descarga
   - Notificaciones de nuevas versiones
   - Sistema de actualizaciones automáticas
