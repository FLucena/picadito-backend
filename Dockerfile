# Usar imagen base de OpenJDK 21
FROM eclipse-temurin:21-jdk-alpine

# Establecer directorio de trabajo
WORKDIR /app

# Copiar Maven wrapper y pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dar permisos de ejecución al Maven wrapper
RUN chmod +x ./mvnw

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Variable de entorno para el puerto (Render lo asigna automáticamente)
ENV PORT=8080

# Copiar script de inicio
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Comando para ejecutar la aplicación usando el script
CMD ["/app/start.sh"]

