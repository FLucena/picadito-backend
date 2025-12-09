#!/bin/sh

# Script de inicio que corrige la URL de la base de datos si no tiene el prefijo "jdbc:"
if [ -n "$DATABASE_URL_RAW" ] && [ -z "$SPRING_DATASOURCE_URL" ]; then
  # Si la URL no comienza con "jdbc:", agregarlo
  if [ "${DATABASE_URL_RAW#jdbc:}" = "$DATABASE_URL_RAW" ]; then
    # La URL no tiene "jdbc:", agregarlo
    if echo "$DATABASE_URL_RAW" | grep -q "^postgresql://"; then
      export SPRING_DATASOURCE_URL="jdbc:${DATABASE_URL_RAW}"
    elif echo "$DATABASE_URL_RAW" | grep -q "^postgres://"; then
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${DATABASE_URL_RAW#postgres://}"
    else
      # Asumir PostgreSQL si no se especifica
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${DATABASE_URL_RAW}"
    fi
  else
    export SPRING_DATASOURCE_URL="$DATABASE_URL_RAW"
  fi
fi

# Si tenemos las propiedades individuales, construir la URL
if [ -n "$DATABASE_HOST" ] && [ -n "$DATABASE_PORT" ] && [ -n "$DATABASE_NAME" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}"
fi

# Ejecutar la aplicación
exec java -jar -Dserver.port=${PORT:-8080} target/picadito-backend-0.0.1-SNAPSHOT.jar

