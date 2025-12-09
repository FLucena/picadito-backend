# Guía de Despliegue en Render (Gratis)

## 🎯 ¿Por qué Render?

- ✅ **750 horas/mes gratis** (suficiente para una app pequeña/mediana)
- ✅ **Base de datos MySQL/PostgreSQL gratis**
- ✅ **Despliegue automático desde GitHub**
- ✅ **Sin necesidad de CLI**
- ✅ **SSL automático**
- ✅ **URL personalizada**

## 📋 Prerequisitos

1. Cuenta en Render: https://render.com (gratis)
2. Repositorio en GitHub con el código

## 🚀 Pasos para Desplegar

### Opción 1: Usando render.yaml (Recomendado - Más Fácil)

1. **Crear cuenta en Render**
   - Ve a https://render.com
   - Regístrate con GitHub (más fácil)

2. **Conectar repositorio**
   - En el dashboard de Render, haz clic en "New +"
   - Selecciona "Blueprint"
   - Conecta tu repositorio de GitHub
   - Render detectará automáticamente el archivo `render.yaml`

3. **Aplicar Blueprint**
   - Render creará automáticamente:
     - Servicio web (Spring Boot)
     - Base de datos MySQL
     - Variables de entorno necesarias

4. **Configurar variables de entorno**
   - Ve a tu servicio web en Render
   - En "Environment", actualiza estas variables:
     ```
     JWT_SECRET=tu_clave_secreta_super_segura_minimo_32_caracteres
     CORS_ALLOWED_ORIGINS=https://tu-frontend.vercel.app,https://www.tu-frontend.com
     ```

5. **Desplegar**
   - Render desplegará automáticamente
   - Espera a que termine el build (puede tardar 5-10 minutos la primera vez)

### Opción 2: Configuración Manual

Si prefieres configurar manualmente:

#### 1. Crear Base de Datos MySQL

1. En Render Dashboard, haz clic en "New +"
2. Selecciona "PostgreSQL" o "MySQL" (MySQL está disponible)
3. Configura:
   - **Name**: `picadito-db`
   - **Database Name**: `picadito_db`
   - **User**: `picadito_user`
   - **Plan**: Free
4. Haz clic en "Create Database"
5. **IMPORTANTE**: Copia las credenciales de conexión (las necesitarás después)

#### 2. Crear Servicio Web

1. En Render Dashboard, haz clic en "New +"
2. Selecciona "Web Service"
3. Conecta tu repositorio de GitHub
4. Configura:
   - **Name**: `picadito-backend`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/picadito-backend-0.0.1-SNAPSHOT.jar`
   - **Plan**: Free

#### 3. Configurar Variables de Entorno

En la sección "Environment" del servicio web, agrega:

```bash
# Perfil
SPRING_PROFILES_ACTIVE=prod

# Puerto (Render lo asigna automáticamente, pero lo configuramos por si acaso)
PORT=8080

# Java Version
JAVA_VERSION=21

# Base de datos (usa las credenciales de la base de datos que creaste)
SPRING_DATASOURCE_URL=jdbc:mysql://dpg-xxxxx-a.oregon-postgres.render.com:5432/picadito_db
SPRING_DATASOURCE_USERNAME=picadito_user
SPRING_DATASOURCE_PASSWORD=tu_password_aqui
SPRING_DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver

# JPA
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# JWT (IMPORTANTE - cambia este valor)
JWT_SECRET=tu_clave_secreta_super_segura_minimo_32_caracteres_aleatorios

# CORS (reemplaza con tu URL de frontend)
CORS_ALLOWED_ORIGINS=https://tu-frontend.vercel.app,https://www.tu-frontend.com
```

#### 4. Conectar Base de Datos al Servicio

1. En tu servicio web, ve a "Connections"
2. Haz clic en "Connect Database"
3. Selecciona la base de datos `picadito-db` que creaste
4. Render configurará automáticamente las variables de entorno de conexión

#### 5. Desplegar

1. Haz clic en "Manual Deploy" → "Deploy latest commit"
2. O simplemente haz push a tu repositorio (si tienes auto-deploy activado)

## 🔧 Configuración de Variables de Entorno

### Variables Obligatorias

```bash
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=tu_clave_secreta_super_segura_minimo_32_caracteres
CORS_ALLOWED_ORIGINS=https://tu-frontend.vercel.app
```

### Variables de Base de Datos (se configuran automáticamente si conectas la DB)

Si las configuras manualmente:
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://host:puerto/picadito_db
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=password
SPRING_DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

## 📝 Notas Importantes

### Límites del Plan Gratuito

- **750 horas/mes** de tiempo de ejecución
- El servicio se "duerme" después de 15 minutos de inactividad
- La primera petición después de dormir puede tardar ~30 segundos (cold start)
- Base de datos: 1 GB de almacenamiento gratis

### Cold Starts

Si tu app está inactiva, Render la "duerme" para ahorrar recursos. La primera petición después puede tardar un poco. Esto es normal en el plan gratuito.

### Base de Datos

Render ofrece **PostgreSQL gratis** por defecto. El archivo `render.yaml` está configurado para MySQL, pero puedes cambiarlo a PostgreSQL si prefieres (solo necesitas cambiar el driver y dialect en las variables de entorno).

**Nota**: Si Render no tiene MySQL disponible en el plan gratuito, puedes:
1. Usar PostgreSQL (recomendado para plan gratuito)
2. O usar una base de datos MySQL externa gratuita (como PlanetScale, Aiven, etc.)

### SSL/HTTPS

Render proporciona SSL automático. Tu app estará disponible en `https://picadito-backend.onrender.com`

### Despliegues Automáticos

Por defecto, Render despliega automáticamente cuando haces push a la rama principal. Puedes desactivarlo en "Settings" → "Auto-Deploy".

## 🐛 Solución de Problemas

### Error: "Build failed"

- Verifica que `JAVA_VERSION=21` esté configurado
- Revisa los logs de build en Render
- Asegúrate de que `mvnw` tenga permisos de ejecución (Render lo maneja automáticamente)

### Error: "Cannot connect to database"

- Verifica que la base de datos esté conectada al servicio web
- Revisa las variables de entorno de conexión
- Asegúrate de que `SPRING_PROFILES_ACTIVE=prod` esté configurado

### Error: "Port already in use"

- Render asigna el puerto automáticamente a través de `PORT`
- Ya está configurado en `application.properties`: `server.port=${PORT:8080}`

### La app se "duerme" frecuentemente

- Esto es normal en el plan gratuito
- Considera usar un servicio de "ping" para mantenerla activa (hay servicios gratuitos para esto)
- O actualiza a un plan de pago si necesitas que esté siempre activa

## 🔐 Seguridad

1. **JWT_SECRET**: Cambia el valor por defecto. Usa un generador de claves seguras.
2. **Base de datos**: Las credenciales se manejan automáticamente por Render.
3. **CORS**: Actualiza `CORS_ALLOWED_ORIGINS` con la URL real de tu frontend.

## 📊 Monitoreo

Render proporciona:
- Logs en tiempo real
- Métricas básicas
- Health checks automáticos

Accede a estos desde el dashboard de tu servicio.

## 🎉 ¡Listo!

Una vez desplegado, tu app estará disponible en:
`https://picadito-backend.onrender.com`

Puedes cambiar el nombre de dominio en "Settings" → "Custom Domain".

