# 📋 REVISIÓN Y FEEDBACK - PUNTOS 1-3
## Proyecto: PÍXEL SHOP
**Autor:** Juan Antonio Peñaranda Madrid  
**Revisor:** GitHub Copilot  
**Fecha de revisión:** Diciembre 2024

---

## 🎯 RESUMEN EJECUTIVO

El documento presenta un proyecto sólido y bien fundamentado. Los puntos 1-3 están **globalmente bien desarrollados**, con un análisis de mercado detallado, planificación temporal realista y estructura metodológica correcta. A continuación, se detallan los aspectos positivos y las áreas de mejora identificadas.

---

## 📊 PUNTO 1: INFORMACIÓN DEL PROYECTO

### ✅ ASPECTOS POSITIVOS

#### 1.1 Descripción del Proyecto
- **Excelente** definición clara del concepto: tienda digital de videojuegos indie
- **Muy buena** diferenciación del público objetivo (desarrolladores indie vs AAA)
- **Correcto** enfoque en los tres pilares: simplicidad, economía y accesibilidad

#### Beneficios para jugadores
- **Bien argumentados** con comparaciones concretas vs Steam
- **Excelente** énfasis en transparencia y políticas claras
- **Buena** propuesta de valor diferenciada

#### Beneficios para desarrolladores
- **Excelente** tabla comparativa de comisiones (muy visual y clara)
- **Muy buena** justificación económica: 15% vs 30% de Steam
- **Excelente** análisis de ahorro en cuota de entrada: 25€ vs 93€
- **Brillante** ejemplo práctico del ahorro de 68€ y sus usos alternativos (marketing, assets, hosting)

#### Público objetivo
- **Correcto** rango de edad: 12-50 años
- **Bien identificados** los segmentos: usuarios finales, early adopters, desarrolladores
- **Buena** segmentación de desarrolladores (indie, noveles, insatisfechos con comisiones)

#### Estudio de mercado
- **Excepcional** análisis de 6 plataformas competidoras
- **Muy completo** desglose de cada plataforma:
  - Steam: Líder del mercado (75% cuota)
  - Epic Games: Modelo más generoso (0% primer millón, 12% después)
  - GOG: Sin DRM, cuota gratuita pero curaduría estricta
  - Itch.io: Comisión variable (0-100%)
  - Microsoft Store: 12% para juegos
  - Pixel Shop: 15%, cuota 25€

- **Excelente** tabla resumen comparativa final
- **Muy buena** identificación de ventajas/desventajas de cada competidor
- **Correcta** inclusión de fuentes y referencias (notas al pie)

### 💡 ÁREAS DE MEJORA

#### 1.1 Descripción del Proyecto
1. **Inconsistencia terminológica**:
   - A veces dices "aplicación de escritorio", otras veces "aplicación web"
   - **Recomendación**: Clarifica desde el inicio que es una **aplicación de escritorio (JavaFX) con backend web (Spring Boot)**

2. **Falta diagrama de arquitectura**:
   - No se explica cómo se relacionan JavaFX + Thymeleaf + Spring Boot
   - **Recomendación**: Añade un **diagrama simple** mostrando:
     ```
     Usuario → Aplicación .exe (JavaFX WebView) → Spring Boot (localhost) → MySQL (Azure)
     ```

3. **Beneficios técnicos poco desarrollados**:
   - Te enfocas mucho en economía, poco en aspectos técnicos
   - **Recomendación**: Añade una subsección "**Beneficios técnicos**":
     - Velocidad de respuesta (app local vs navegador)
     - Menor consumo de recursos
     - Acceso offline a biblioteca

#### Estudio de mercado
4. **Faltan métricas de validación**:
   - No se menciona si has hecho encuestas, entrevistas o pruebas de concepto
   - **Recomendación**: Añade una subsección "**Validación de mercado**":
     - ¿Has preguntado a desarrolladores indie españoles?
     - ¿Hay comunidades interesadas (Discord, foros)?
     - ¿Has contactado con algún estudio indie local?

5. **Competencia directa itch.io infraestimada**:
   - Itch.io es MUY similar a tu propuesta (comisión flexible, enfoque indie)
   - **Recomendación**: Desarrolla más la **diferenciación** vs itch.io:
     - Tu aplicación es escritorio (más profesional)
     - Cuota fija + comisión (modelo híbrido único)
     - Enfoque en mercado español

6. **Falta análisis de barreras de entrada**:
   - No mencionas los **desafíos reales**:
     - ¿Cómo atraer los primeros desarrolladores? (problema del huevo y la gallina)
     - ¿Cómo conseguir los primeros 1.000 usuarios?
   - **Recomendación**: Añade sección "**Estrategia de adopción inicial**":
     - Contactar escuelas de desarrollo de videojuegos (FP, universidades)
     - Ofrecer primeros 6 meses sin comisión
     - Gamejams patrocinadas

7. **Error en tabla de Epic Games**:
   - Dices "0% en el primer millón desde junio 2025"
   - **Corrección**: Estamos en 2024, debería ser "desde junio 2024" o "a partir de 2025"

---

## 📅 PUNTO 2: PLANIFICACIÓN DEL PROYECTO

### ✅ ASPECTOS POSITIVOS

#### 2.1 Fases
- **Excelente** diagrama de Gantt con 10 semanas
- **Muy buena** distribución temporal:
  - Semanas 1-3: Análisis, diseño, configuración
  - Semanas 4-5: Backend
  - Semanas 5-6: Frontend
  - Semanas 6-7: Funcionalidades core
  - Semanas 7-8: Testing y ajustes
  - Semana 9: Optimización
  - Semana 10: Documentación final

- **Correcto** incluir fases post-desarrollo:
  - Preparar presentación
  - Hacer PowerPoint
  - Práctica de defensa
  - Correcciones post-evaluación

#### 2.2 Coste económico
- **Excepcional** análisis de costes:
  - Inversión inicial: 4.706€
  - Desglose detallado por categorías (desarrollo, licencias, infraestructura, marketing)
  - **Muy realista** inclusión de horas de trabajo (386h × 7€/h = 2.702€)

- **Excelente** planificación de amortización:
  - 3 escenarios: pesimista, realista, optimista
  - Cálculos correctos de ROI
  - **Brillante** tabla evolutiva trimestral (escenario realista)
  - **Muy bueno** cálculo de recuperación de inversión (3,1 meses en escenario realista)

- **Excelente** inclusión de beneficios no económicos:
  - Experiencia técnica
  - Portfolio profesional
  - Impacto social
  - Aprendizaje empresarial

#### 2.3 Reflexión
- **Muy buena** autocrítica sobre viabilidad temporal
- **Correcto** análisis de riesgos (JavaFX + Spring Boot, Azure en producción)
- **Buena** estrategia de mitigación (priorizar funcionalidades básicas)
- **Excelente** definición de alcance limitado (versión 1.0)
- **Muy buena** identificación de limitaciones (solo Windows, sin descarga de juegos)
- **Correcta** lista de mejoras futuras

### 💡 ÁREAS DE MEJORA

#### 2.1 Fases
1. **Gantt poco visual**:
   - En el documento solo se ve como tabla de texto
   - **Recomendación**: Genera una **imagen real** del diagrama de Gantt con colores

2. **Falta asignación de recursos por fase**:
   - No especificas cuántas horas por semana trabajarás en cada fase
   - **Recomendación**: Añade columna "**Horas estimadas**" en el Gantt:
     - Semana 1-3: 40h/semana (120h total)
     - Semana 4-5: 50h/semana (100h total)
     - Etc.

3. **No hay plan de contingencia**:
   - ¿Qué pasa si una fase se retrasa?
   - **Recomendación**: Añade sección "**Plan de contingencia**":
     - Si Backend se retrasa → Reducir funcionalidades SHOULD/COULD
     - Si Testing falla → Semana 11 de buffer
     - Si Azure falla → Tener MySQL local de respaldo

#### 2.2 Coste económico
4. **Contradicción en cuota de publicación**:
   - En el punto 1 dices **25€** por juego publicado
   - En la tabla de escenarios dices **50€** ("Cada juego subido/publicado cuesta 50€")
   - **Corrección**: **Elige uno y mantenlo consistente** (recomiendo 25€)

5. **Escenario "optimista" demasiado optimista**:
   - 5.000 usuarios y 60 juegos en el primer mes parece **irreal** para una plataforma nueva
   - **Recomendación**: Ajusta a algo más realista:
     - Optimista: 2.000 usuarios, 30 juegos, 1.000€/mes

6. **Falta análisis de costes recurrentes**:
   - Solo mencionas Azure (14€/mes)
   - **Faltan**:
     - Dominio web (.com): ~12€/año
     - Certificado SSL: Gratis con Let's Encrypt
     - Marketing continuo: ~50€/mes (redes sociales)
     - Soporte al cliente: Tu tiempo
   - **Recomendación**: Añade sección "**Costes recurrentes mensuales**"

7. **No consideras costes de escalabilidad**:
   - Con 5.000 usuarios, Azure (14€/mes) será insuficiente
   - **Recomendación**: Añade nota: "A partir de X usuarios, escalar a Azure (tier superior)"

#### 2.3 Reflexión
8. **Falta cronograma detallado de hitos**:
   - No hay fechas concretas de entregables
   - **Recomendación**: Añade tabla "**Hitos y entregables**":
     | Semana | Hito | Entregable |
     |--------|------|------------|
     | 3 | Entorno configurado | BD Azure funcional + proyecto GitHub |
     | 5 | Backend completo | API REST documentada |
     | 7 | MVP funcional | Demo ejecutable |
     | 10 | Versión final | .exe instalable + documentación |

9. **"Voy a intentar que se descarguen" es vago**:
   - No defines si los juegos se descargarán o no
   - **Recomendación**: **Decide YA**:
     - **Opción A**: NO descargar juegos (versión 1.0 solo catálogo y compra)
     - **Opción B**: SÍ descargar (requiere sistema de DRM básico)

---

## 📚 PUNTO 3: INFORMACIÓN Y DESARROLLO

### ✅ ASPECTOS POSITIVOS

#### 3.1 Información
- **Muy buena** tabla de "Cuándo se obtiene la información" (por fases)
- **Excelente** tabla de "Dónde se obtiene" con enlaces concretos:
  - Documentación oficial de Spring Boot, MySQL, Java
  - Análisis de competencia (Steam, Epic, GOG)

- **Buena** tabla de "Cómo se obtiene la información":
  - Encuestas online
  - Análisis competitivo
  - Cursos y tutoriales
  - Consulta a expertos
  - Pruebas de usabilidad
  - Análisis de código abierto

#### 3.2 Lista de apartados a desarrollar
- **Excelente** uso del método MoSCoW:
  - **MUST**: Funcionalidades obligatorias bien definidas
  - **SHOULD**: Mejoras importantes pero no críticas
  - **COULD**: Funcionalidades extra (wishlist, dashboard estadísticas)
  - **WON'T**: Versión futura (amigos, foros, logros, mods)

- **Muy buena** distribución por semanas con prioridades
- **Correcto** enfoque en lo esencial primero

### 💡 ÁREAS DE MEJORA

#### 3.1 Información
1. **Tabla "Cuándo se obtiene" incompleta**:
   - No mencionas información sobre **seguridad** (Spring Security, BCrypt)
   - No mencionas información sobre **testing** (JUnit, Mockito)
   - **Recomendación**: Añade filas:
     - "Configuración de Spring Security" → Semana 4
     - "Estrategias de testing" → Semana 7

2. **Falta validación de fuentes**:
   - Pones enlaces pero no explicas **por qué** son las mejores fuentes
   - **Recomendación**: Añade columna "**Criterio de selección**":
     - Spring Boot docs → Documentación oficial (más actualizada)
     - GitHub repos → Ejemplos reales de producción

3. **"Consulta a expertos" sin concretar**:
   - ¿A quién consultarás específicamente?
   - **Recomendación**: Concreta:
     - "Desarrolladores indie españoles (comunidad Discord XYZ)"
     - "Profesor tutor (Jesús Ibáñez López)"
     - "Foros especializados (Stack Overflow, Reddit /r/gamedev)"

#### 3.2 Lista de apartados a desarrollar
4. **Falta estimación de esfuerzo por prioridad**:
   - No sabes cuántas horas tomará cada bloque
   - **Recomendación**: Añade columna "**Horas estimadas**":
     - MUST (Backend): 60h
     - MUST (Frontend): 50h
     - MUST (Autenticación): 20h
     - Etc.

5. **"Sistema de autenticación" muy genérico**:
   - No defines alcance: ¿BCrypt? ¿OAuth? ¿Roles?
   - **Recomendación**: Desglosa en sub-tareas:
     - Login/logout con Spring Security
     - Encriptación BCrypt
     - Roles: ADMIN, PROVEEDOR, USUARIO
     - Session management

6. **Faltan criterios de aceptación**:
   - ¿Cómo sabrás que "Biblioteca personal" está completa?
   - **Recomendación**: Añade columna "**Criterios de aceptación**":
     - "Biblioteca personal":
       - Usuario puede ver juegos comprados
       - Juegos ordenados por fecha
       - Búsqueda por nombre funcional

7. **WON'T muy ambicioso para "versión futura"**:
   - Foros, logros, mods son **características AAA** que Steam tardó años en desarrollar
   - **Recomendación**: Sé más realista: "WON'T: Fuera de alcance del TFG, posibles versiones 2.0+"

---

## 📝 ERRORES Y CORRECCIONES ESPECÍFICAS

### Errores tipográficos y de formato

1. **Título**: "INFORMACIÓN" tiene tilde, "INFORMACION" no
   - ✏️ **Punto 1**: "1.INFORMACION DEL PROYECTO" → "1. INFORMACIÓN DEL PROYECTO"
   - ✏️ **Punto 3**: "3. INFORMACIÃ"N Y DESARROLLO" → "3. INFORMACIÓN Y DESARROLLO"

2. **Espacios inconsistentes**:
   - ✏️ "1.INFORMACION" → "1. INFORMACIÓN" (añadir espacio después del número)
   - ✏️ "2.PLANIFICACION" → "2. PLANIFICACIÓN"
   - ✏️ "3.INFORMACION" → "3. INFORMACIÓN"

3. **"AlquerÃ­as, 4 de diciembre de 2025"**:
   - Error de codificación: "AlquerÃ­as" → "Alquerías"
   - **Fecha futura**: Estamos en 2024, ¿será 2025 cuando presentes?
   - ✏️ Si presentas en diciembre 2024 → Cambiar a "4 de diciembre de 2024"

4. **Notas al pie mal formateadas**:
   - "Juegos indies¹" → Usar superíndice correctamente
   - "Early adopters³" → Usar superíndice correctamente

5. **Símbolos de moneda**:
   - "â‚¬" → "€" (error de codificación UTF-8)
   - Revisar TODO el documento y reemplazar

6. **Porcentajes sin espacio**:
   - ✏️ "15%" → "15 %" (en español se pone espacio)
   - ✏️ "30%" → "30 %"

7. **Tabla de Gantt**:
   - "S1, S2, S3..." → Mejor usar "Sem. 1, Sem. 2, Sem. 3" (más claro)

### Errores de contenido

8. **Contradicción en la tecnología**:
   - **Punto 1.1**: "aplicación de **escritorio**"
   - **Tecnologías**: "desarrollar aplicaciones **web** robustas"
   - ✏️ **Corrección**: "aplicación de escritorio con arquitectura web"

9. **Inconsistencia en modelo de base de datos**:
   - Punto 4.1 muestra entidades: Usuario, Compra, DetalleCompra, Articulo, BibliotecaUsuario, MetodoPago, **Proveedor**
   - Pero dice: "*El método de pago y proveedor todavía no lo he desarrollado puede cambiar"
   - ✏️ **Corrección**: Decide YA si Proveedor será una entidad separada o un atributo de Usuario (rol PROVEEDOR)

10. **Modelo Entidad-Relación desactualizado**:
    - Tu modelo conceptual NO coincide con las entidades reales de tu código
    - Tu código real tiene:
      - `Usuario` (con rol)
      - `Juego` (con proveedor_id)
      - `Compra`
      - `MovimientoProveedor`
      - `PublicacionJuego`
      - `BibliotecaUsuario`
    - ✏️ **Corrección**: Actualiza el diagrama ER con las entidades REALES

---

## 🎯 RECOMENDACIONES PRIORITARIAS

### 🔴 CRÍTICAS (Corregir antes de entregar)

1. **Actualizar modelo de base de datos** (Punto 4.1):
   - Reemplazar diagrama ER antiguo por el modelo REAL implementado

2. **Corregir contradicción de cuota**: 25€ o 50€ (elegir uno)

3. **Aclarar tecnología**: "Aplicación de escritorio con backend web"

4. **Corregir errores de codificación UTF-8** (€, í, ñ, etc.)

### 🟡 IMPORTANTES (Mejorar para profesionalizar)

5. **Añadir diagrama de arquitectura** (Punto 1.1):
   ```
   [Usuario] → [App .exe JavaFX] → [Spring Boot :8080] → [MySQL Azure]
   ```

6. **Añadir sección "Validación de mercado"** (Punto 1.1):
   - ¿Has hablado con desarrolladores indie?
   - ¿Hay interés real?

7. **Ajustar escenario "optimista"** (Punto 2.2):
   - Reducir expectativas a cifras realistas (2.000 usuarios, no 5.000)

8. **Añadir plan de contingencia** (Punto 2.1):
   - ¿Qué hacer si hay retrasos?

### 🟢 OPCIONALES (Nice to have)

9. **Añadir cronograma de hitos** con fechas concretas

10. **Generar imagen del Gantt** (más visual que tabla)

11. **Desglosar "Sistema de autenticación"** en sub-tareas

12. **Añadir costes recurrentes** (dominio, marketing continuo)

---

## ✅ CONCLUSIÓN GENERAL

**Calificación global de los puntos 1-3: 8/10** ⭐⭐⭐⭐⭐⭐⭐⭐☆☆

### Puntos fuertes:
- ✅ Análisis de mercado **excepcional** (6 competidores analizados)
- ✅ Tablas comparativas muy visuales y bien argumentadas
- ✅ Planificación económica **realista y detallada**
- ✅ Uso correcto de metodología MoSCoW
- ✅ Reflexión autocrítica sobre viabilidad

### Áreas de mejora:
- ⚠️ Corregir errores de codificación UTF-8
- ⚠️ Actualizar modelo ER con entidades reales
- ⚠️ Resolver contradicciones (25€ vs 50€, escritorio vs web)
- ⚠️ Añadir validación de mercado real
- ⚠️ Añadir diagrama de arquitectura técnica
- ⚠️ Ajustar expectativas del escenario "optimista"

### Mensaje final:
**¡Muy buen trabajo!** 🎉 La base del proyecto está sólida. Con las correcciones sugeridas, especialmente las CRÍTICAS, el documento estará **listo para entregar**. El análisis de mercado es de nivel profesional y la planificación económica demuestra madurez empresarial. Sigue así y tendrás un proyecto sobresaliente.

---

**Siguiente paso recomendado**: Corregir los errores críticos (UTF-8, modelo ER, contradicciones) antes de continuar con los puntos 5-8.

