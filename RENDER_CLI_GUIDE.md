# Guía de Render CLI

## Instalación Manual (Windows)

1. **Descargar el CLI**:
   - Ve a: https://github.com/render-oss/render-cli/releases/latest
   - Descarga: `render-windows-amd64.exe`
   - Renómbralo a `render.exe`

2. **Agregar al PATH** (opción 1 - Sistema):
   - Copia `render.exe` a `C:\Windows\System32`
   - O crea una carpeta `C:\tools` y agrégalo al PATH del sistema

3. **Agregar al PATH** (opción 2 - Usuario):
   - Crea una carpeta: `C:\Users\%USERNAME%\bin`
   - Copia `render.exe` ahí
   - Agrega `C:\Users\%USERNAME%\bin` al PATH del usuario

4. **Verificar instalación**:
   ```bash
   render --version
   ```

## Autenticación

```bash
render login
```

Esto abrirá tu navegador para autenticarte con tu cuenta de Render.

## Comandos Principales

### Listar Servicios
```bash
render services
```

### Crear Servicio desde Blueprint
```bash
# Si tienes render.yaml en el directorio actual
render blueprints apply
```

### Crear Servicio Manualmente
```bash
render services create \
  --name picadito-backend \
  --type web \
  --runtime java \
  --build-command "./mvnw clean package -DskipTests" \
  --start-command "java -jar target/picadito-backend-0.0.1-SNAPSHOT.jar"
```

### Crear Base de Datos
```bash
render databases create \
  --name picadito-db \
  --database-name picadito_db \
  --database-user picadito_user \
  --plan free
```

### Configurar Variables de Entorno
```bash
# Para un servicio específico
render env set SPRING_PROFILES_ACTIVE=prod --service picadito-backend
render env set JWT_SECRET=tu_clave_secreta --service picadito-backend
render env set CORS_ALLOWED_ORIGINS=https://tu-frontend.com --service picadito-backend
```

### Ver Logs
```bash
render logs --service picadito-backend --follow
```

### Crear Despliegue
```bash
render deploys create --service picadito-backend
```

### Ver Estado del Servicio
```bash
render services show picadito-backend
```

## Flujo Completo de Despliegue con CLI

### 1. Autenticarse
```bash
render login
```

### 2. Crear Base de Datos
```bash
render databases create \
  --name picadito-db \
  --database-name picadito_db \
  --database-user picadito_user \
  --plan free
```

### 3. Obtener ID de la Base de Datos
```bash
render databases list
# Copia el ID de picadito-db
```

### 4. Crear Servicio Web
```bash
render services create \
  --name picadito-backend \
  --type web \
  --runtime java \
  --plan free \
  --build-command "./mvnw clean package -DskipTests" \
  --start-command "java -jar target/picadito-backend-0.0.1-SNAPSHOT.jar" \
  --repo https://github.com/tu-usuario/picadito-backend \
  --branch main
```

### 5. Conectar Base de Datos al Servicio
```bash
# Obtener el ID del servicio
render services list

# Conectar (esto configura automáticamente las variables de entorno)
render services connect-database picadito-backend picadito-db
```

### 6. Configurar Variables de Entorno
```bash
render env set SPRING_PROFILES_ACTIVE=prod --service picadito-backend
render env set JAVA_VERSION=21 --service picadito-backend
render env set JWT_SECRET=tu_clave_secreta_super_segura --service picadito-backend
render env set CORS_ALLOWED_ORIGINS=https://tu-frontend.com --service picadito-backend
```

### 7. Desplegar
```bash
render deploys create --service picadito-backend
```

## Usar Blueprint (render.yaml)

Si tienes `render.yaml` en tu repositorio:

```bash
# Aplicar blueprint (crea todo automáticamente)
render blueprints apply

# O desde un repositorio específico
render blueprints apply --repo https://github.com/tu-usuario/picadito-backend
```

## Comandos Útiles

```bash
# Ver ayuda
render --help

# Ver versión
render --version

# Listar todos los servicios
render services

# Listar todas las bases de datos
render databases

# Ver logs en tiempo real
render logs --service picadito-backend --follow

# Reiniciar servicio
render services restart picadito-backend

# Ver detalles de un servicio
render services show picadito-backend

# Ver variables de entorno
render env list --service picadito-backend
```

## Notas

- El CLI requiere autenticación: `render login`
- Los comandos son interactivos por defecto
- Puedes usar `--non-interactive` para scripts
- Las variables de entorno de base de datos se configuran automáticamente al conectar

