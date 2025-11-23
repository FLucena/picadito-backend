# ⚽ Picadito Backend

API REST desarrollada con Spring Boot para gestionar partidos de fútbol. Permite crear partidos, gestionar inscripciones, buscar partidos y administrar participantes.

## 📋 Tabla de Contenidos

- [Características Principales](#-características-principales)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Requisitos](#-requisitos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Arquitectura](#-arquitectura)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Modelos de Datos](#-modelos-de-datos)
- [Validaciones y Reglas de Negocio](#-validaciones-y-reglas-de-negocio)
- [Manejo de Errores](#-manejo-de-errores)
- [Prácticas de Desarrollo](#-prácticas-de-desarrollo)
- [Testing](#-testing)
- [Configuración](#-configuración)

## 🚀 Características Principales

- ✅ **Gestión de Partidos**: Crear, editar, eliminar y buscar partidos
- ✅ **Gestión de Sedes**: Crear y administrar sedes (lugares donde se juegan los partidos)
- ✅ **Sistema de Partidos Seleccionados**: Agregar partidos a una lista temporal antes de confirmar
- ✅ **Sistema de Reservas**: Confirmar múltiples reservas a partidos a la vez
- ✅ **Búsqueda Avanzada**: Filtrar partidos por múltiples criterios
- ✅ **Gestión de Participantes**: Inscribirse y desinscribirse de partidos
- ✅ **Precios y Costos**: Gestión de precios por partido y cálculo de costo por jugador
- ✅ **Validaciones de Negocio**: Estado del partido, capacidad máxima, fechas futuras, validación de partidos completos
- ✅ **Manejo Centralizado de Excepciones**: Errores consistentes y claros
- ✅ **Bloqueo Optimista**: Previene race conditions en inscripciones
- ✅ **Documentación Swagger**: API documentada automáticamente

## 🔧 Tecnologías Utilizadas

- **Spring Boot 3.5.7** - Framework principal
- **Spring Data JPA** - Abstracción de acceso a datos
- **Hibernate** - ORM para mapeo objeto-relacional
- **H2 Database** - Base de datos en memoria para desarrollo
- **Lombok** - Reducción de boilerplate code
- **Swagger/OpenAPI** - Documentación automática de la API
- **Java 21** - Lenguaje de programación
- **Maven** - Gestión de dependencias y build

## 📋 Requisitos

- **Java 21** o superior
- **Maven 3.6+** (incluido wrapper Maven en el proyecto)

## 🛠️ Instalación y Ejecución

### Opción 1: Usando Maven Wrapper (Recomendado)

```bash
# Windows
cd picadito-backend
.\mvnw.cmd spring-boot:run

# Linux/Mac
cd picadito-backend
./mvnw spring-boot:run
```

### Opción 2: Usando Maven instalado

```bash
cd picadito-backend
mvn spring-boot:run
```

### Verificar que el backend está corriendo

- Espera a ver el mensaje: `Started PicaditoApplication`
- El backend estará disponible en: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Usuario: `sa`
  - Password: (vacío)

## 📁 Estructura del Proyecto

```
src/main/java/com/techlab/picadito/
├── controller/          # Controladores REST (API endpoints)
│   ├── PartidoController.java
│   ├── ParticipanteController.java
│   ├── PartidosSeleccionadosController.java
│   ├── ReservaController.java
│   ├── SedeController.java
│   └── AdminController.java
├── service/             # Lógica de negocio
│   ├── PartidoService.java
│   ├── ParticipanteService.java
│   ├── PartidosSeleccionadosService.java
│   ├── ReservaService.java
│   └── SedeService.java
├── repository/          # Acceso a datos (JPA)
│   ├── PartidoRepository.java
│   ├── ParticipanteRepository.java
│   ├── PartidosSeleccionadosRepository.java
│   ├── ReservaRepository.java
│   └── SedeRepository.java
├── model/               # Entidades JPA
│   ├── Partido.java
│   ├── Participante.java
│   ├── PartidosSeleccionados.java
│   ├── Reserva.java
│   └── Sede.java
├── dto/                 # Objetos de transferencia
│   ├── PartidoDTO.java
│   ├── BusquedaPartidoDTO.java
│   └── ...
├── exception/           # Excepciones personalizadas
│   ├── GlobalExceptionHandler.java
│   └── ...
├── config/              # Configuraciones
│   ├── CorsConfig.java
│   ├── SwaggerConfig.java
│   └── DataInitializer.java
└── util/                # Utilidades
    └── MapperUtil.java
```

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura en capas** (Layered Architecture) con separación clara de responsabilidades:

### Capas de la Aplicación

1. **Controller Layer** (`controller/`)
   - Maneja las peticiones HTTP
   - Valida parámetros de entrada
   - Delega la lógica de negocio a los servicios
   - Retorna respuestas HTTP apropiadas

2. **Service Layer** (`service/`)
   - Contiene la lógica de negocio
   - Valida reglas de negocio
   - Coordina transacciones
   - Maneja la comunicación entre repositorios

3. **Repository Layer** (`repository/`)
   - Abstrae el acceso a datos
   - Extiende `JpaRepository` para operaciones CRUD
   - Define queries personalizadas cuando es necesario

4. **Model Layer** (`model/`)
   - Define las entidades JPA
   - Representa la estructura de la base de datos
   - Contiene anotaciones de validación

5. **DTO Layer** (`dto/`)
   - Objetos de transferencia de datos
   - Separación entre modelo interno y API externa
   - Previene exposición de entidades internas

### Principios de Diseño Aplicados

- **Separación de Responsabilidades (SRP)**: Cada clase tiene una única responsabilidad
- **Inversión de Dependencias (DIP)**: Los controladores dependen de abstracciones (servicios)
- **Principio Abierto/Cerrado (OCP)**: Extensible mediante herencia e interfaces
- **DRY (Don't Repeat Yourself)**: Reutilización de código mediante servicios y utilidades

## 📚 Endpoints de la API

### Partidos

- `GET /api/partidos` - Listar todos los partidos
- `GET /api/partidos/disponibles` - Listar partidos disponibles
- `POST /api/partidos/buscar` - Búsqueda avanzada
- `GET /api/partidos/{id}` - Obtener partido por ID
- `POST /api/partidos` - Crear nuevo partido
- `PUT /api/partidos/{id}` - Actualizar partido
- `DELETE /api/partidos/{id}` - Eliminar partido
- `GET /api/partidos/{id}/costo-por-jugador` - Obtener costo por jugador

### Partidos Seleccionados

- `GET /api/partidos-seleccionados/usuario/{usuarioId}` - Obtener partidos seleccionados
- `POST /api/partidos-seleccionados/usuario/{usuarioId}/agregar` - Agregar partido
- `PUT /api/partidos-seleccionados/usuario/{usuarioId}/item/{lineaId}` - Actualizar cantidad
- `DELETE /api/partidos-seleccionados/usuario/{usuarioId}/item/{lineaId}` - Eliminar item
- `DELETE /api/partidos-seleccionados/usuario/{usuarioId}` - Vaciar selección

### Reservas

- `GET /api/reservas` - Listar todas las reservas
- `GET /api/reservas/{id}` - Obtener reserva por ID
- `GET /api/reservas/usuario/{usuarioId}` - Obtener reservas de usuario
- `GET /api/reservas/usuario/{usuarioId}/total-gastado` - Total gastado
- `POST /api/reservas/desde-partidos-seleccionados/{usuarioId}` - Crear reserva
- `PUT /api/reservas/{id}/estado` - Actualizar estado
- `PUT /api/reservas/{id}/cancelar` - Cancelar reserva

### Participantes

- `POST /api/partidos/{partidoId}/participantes` - Inscribirse a partido
- `GET /api/partidos/{partidoId}/participantes` - Ver participantes
- `DELETE /api/partidos/{partidoId}/participantes/{participanteId}` - Desinscribirse

### Sedes

- `GET /api/sedes` - Listar todas las sedes
- `GET /api/sedes/{id}` - Obtener sede por ID
- `POST /api/sedes` - Crear nueva sede
- `PUT /api/sedes/{id}` - Actualizar sede
- `DELETE /api/sedes/{id}` - Eliminar sede
- `POST /api/sedes/migrar` - Migrar ubicaciones a sedes

## 📖 Modelos de Datos

### Partido
- `id`: Identificador único (auto-generado)
- `titulo`: Título del partido (máx. 200 caracteres)
- `descripcion`: Descripción opcional (máx. 1000 caracteres)
- `fechaHora`: Fecha y hora del partido (debe ser futura - formato ISO 8601)
- `ubicacion`: Ubicación del partido (máx. 300 caracteres) - Deprecated: Usar `sedeId`
- `sedeId`: ID de la sede donde se juega el partido (opcional)
- `sede`: Objeto Sede completo (incluido en respuesta)
- `maxJugadores`: Número máximo de jugadores (1-50, default: 22)
- `estado`: Estado del partido (DISPONIBLE, COMPLETO, FINALIZADO, CANCELADO)
- `creadorNombre`: Nombre del creador (máx. 100 caracteres)
- `fechaCreacion`: Fecha de creación (auto-generada)
- `cantidadParticipantes`: Cantidad actual de participantes
- `precio`: Precio total del partido (opcional)
- `imagenUrl`: URL de imagen del partido (opcional, máx. 500 caracteres)

### Sede
- `id`: Identificador único (auto-generado)
- `nombre`: Nombre de la sede (máx. 200 caracteres, opcional)
- `direccion`: Dirección completa (máx. 300 caracteres, opcional)
- `descripcion`: Descripción de la sede (máx. 1000 caracteres, opcional)
- `telefono`: Teléfono de contacto (máx. 50 caracteres, opcional)
- `coordenadas`: Coordenadas GPS (máx. 100 caracteres, opcional)
- `fechaCreacion`: Fecha de creación (auto-generada)
- `fechaActualizacion`: Fecha de última actualización (auto-generada)

### Participante
- `id`: Identificador único (auto-generado)
- `nombre`: Nombre del participante (máx. 100 caracteres, requerido)
- `apodo`: Apodo opcional (máx. 100 caracteres, puede ser null)
- `posicion`: Posición preferida (PORTERO, DEFENSA, MEDIOCAMPISTA, DELANTERO, opcional)
- `nivel`: Nivel de juego (PRINCIPIANTE, INTERMEDIO, AVANZADO, EXPERTO, opcional)
- `fechaInscripcion`: Fecha de inscripción (auto-generada)
- `partido`: Relación con el partido

## ⚠️ Validaciones y Reglas de Negocio

### Partidos
- El título, ubicación y nombre del creador son obligatorios
- **La fecha y hora (`fechaHora`) DEBE ser una fecha futura**
- El número máximo de jugadores debe estar entre 1 y 50
- No se puede actualizar un partido finalizado o cancelado
- No se puede reducir el máximo de jugadores por debajo de la cantidad actual de participantes

### Participantes
- El nombre es obligatorio
- No se puede inscribir a un partido que no está disponible
- No se puede inscribir a un partido completo
- No puede haber dos participantes con el mismo nombre en el mismo partido
- El apodo, la posición preferida y el nivel son completamente opcionales

### Partidos Seleccionados y Reservas
- Al agregar partidos a la selección, se valida que el partido esté disponible y tenga cupos
- Al confirmar reservas, se valida que todos los partidos sigan disponibles
- **Validación importante**: Solo se pueden confirmar partidos que estén completos (cantidadParticipantes === maxJugadores)
- Se verifica que haya cupos disponibles en todos los partidos
- Se crean los participantes automáticamente al confirmar la reserva
- Los partidos se marcan como COMPLETO si se llenan
- Se calcula el total de la reserva basado en los precios de los partidos

### Reservas
- Solo se permiten transiciones de estado válidas según el ciclo de vida
- No se puede retroceder estados (ej: EN_PROCESO → CONFIRMADO)
- CANCELADO y FINALIZADO son estados terminales
- Los estados se actualizan automáticamente basándose en las fechas de los partidos
- Se calcula el total gastado por usuario sumando todas las reservas confirmadas

### Sedes
- Las sedes pueden tener nombre, dirección, descripción, teléfono y coordenadas
- Los partidos pueden estar asociados a una sede mediante `sedeId`
- La migración automática crea sedes únicas basadas en las ubicaciones existentes de los partidos
- No se puede eliminar una sede si hay partidos asociados (validación de integridad referencial)

## 🛡️ Manejo de Errores

La API utiliza un `GlobalExceptionHandler` que maneja todos los errores de forma centralizada:

- **404 Not Found**: Recurso no encontrado
- **400 Bad Request**: Errores de validación o negocio
- **409 Conflict**: Conflictos de concurrencia (bloqueo optimista)
- **500 Internal Server Error**: Errores inesperados

### Excepciones Personalizadas

- `ResourceNotFoundException`: Recurso no encontrado
- `BusinessException`: Error de lógica de negocio
- `ValidationException`: Error de validación
- `CuposInsuficientesException`: No hay cupos disponibles
- `PartidoNoDisponibleException`: Partido no disponible para inscripciones

Ejemplo de respuesta de error:
```json
{
  "timestamp": "2024-11-04T20:30:00",
  "status": 400,
  "error": "Business Error",
  "message": "El partido ya está completo. Máximo de jugadores: 22",
  "path": "/api/partidos/1/participantes"
}
```

## 💻 Prácticas de Desarrollo

### Convenciones de Código

- **Nombres en español**: Todas las clases, métodos y variables usan nombres descriptivos en español
- **Comentarios en español**: Todos los comentarios están en español
- **CamelCase**: Para nombres de clases y métodos
- **camelCase**: Para variables y parámetros
- **UPPER_SNAKE_CASE**: Para constantes

### Patrones Utilizados

1. **DTO Pattern**: Separación entre entidades de dominio y objetos de transferencia
2. **Repository Pattern**: Abstracción del acceso a datos
3. **Service Layer Pattern**: Encapsulación de lógica de negocio
4. **Exception Handler Pattern**: Manejo centralizado de excepciones
5. **Builder Pattern**: Construcción de objetos complejos (mediante Lombok)

### Mejores Prácticas

- ✅ Validación en múltiples capas (DTO, Service, Model)
- ✅ Uso de transacciones para operaciones críticas
- ✅ Bloqueo optimista para prevenir race conditions
- ✅ Separación de responsabilidades
- ✅ Código limpio y mantenible
- ✅ Documentación con Swagger
- ✅ Manejo de errores consistente

## 🧪 Testing

El proyecto incluye tests unitarios y de integración:

### Ejecutar Tests

```bash
# Todos los tests
./mvnw test

# Test específico
./mvnw test -Dtest=PartidoControllerTest

# Con coverage
./mvnw test jacoco:report
```

### Estructura de Tests

```
src/test/java/com/techlab/picadito/
├── controller/          # Tests de controladores
├── service/             # Tests de servicios
└── integration/        # Tests de integración
```

### Tipos de Tests

- **Controller Tests**: Usan `@WebMvcTest` para probar endpoints REST
- **Service Tests**: Tests unitarios con mocks usando Mockito
- **Integration Tests**: Tests end-to-end con `@SpringBootTest`

## ⚙️ Configuración

### CORS
Configurado para permitir orígenes específicos:
- `http://localhost:3000`
- `http://localhost:8080`
- `http://localhost:5173`

### Base de Datos
- **Motor**: H2 Database (en memoria para desarrollo)
- **Consola H2**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Usuario: `sa`
  - Password: (vacío)

**⚠️ Nota**: Los datos se pierden al reiniciar la aplicación. Para producción, configurar una base de datos persistente (PostgreSQL, MySQL, etc.)

### Swagger/OpenAPI
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- Documentación automática de todos los endpoints
- Interfaz interactiva para probar la API

## 📝 Datos de Prueba

El backend crea automáticamente usuarios de ejemplo al iniciar (ver `DataInitializer.java`):
- **Usuario Demo**: ID 1

## 🐛 Solución de Problemas

### Puerto 8080 en uso
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Errores de compilación
```bash
cd picadito-backend
mvn clean install
mvn spring-boot:run
```

### Error de conexión con base de datos
- Verifica que H2 esté configurado correctamente en `application.properties`
- Para producción, configura MySQL en `application.properties`

### Error de CORS
- Verifica que el origen del frontend esté en `CorsConfig.java`
- Asegúrate de que el frontend esté usando el puerto correcto

---

**¡Disfruta organizando tus partidos de fútbol! ⚽**

