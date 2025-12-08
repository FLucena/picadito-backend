-- Índices para optimizar consultas frecuentes
-- Este archivo es para referencia. En producción, ejecutar estos índices manualmente
-- o usar Flyway/Liquibase para migraciones automáticas

-- Índice para búsqueda de partidos por estado
CREATE INDEX IF NOT EXISTS idx_partidos_estado ON partidos(estado);

-- Índice para búsqueda de partidos por fecha
CREATE INDEX IF NOT EXISTS idx_partidos_fecha_hora ON partidos(fecha_hora);

-- Índice compuesto para partidos disponibles ordenados por fecha
CREATE INDEX IF NOT EXISTS idx_partidos_estado_fecha ON partidos(estado, fecha_hora);

-- Índice para búsqueda por sede
CREATE INDEX IF NOT EXISTS idx_partidos_sede_id ON partidos(sede_id);

-- Índice para búsqueda de reservas por usuario
CREATE INDEX IF NOT EXISTS idx_reservas_usuario_id ON reservas(usuario_id);

-- Índice para búsqueda de reservas por estado
CREATE INDEX IF NOT EXISTS idx_reservas_estado ON reservas(estado);

-- Índice para búsqueda de participantes por partido
CREATE INDEX IF NOT EXISTS idx_participantes_partido_id ON participantes(partido_id);

-- Índice para búsqueda de calificaciones por partido
CREATE INDEX IF NOT EXISTS idx_calificaciones_partido_id ON calificaciones(partido_id);

-- Índice para búsqueda de alertas por usuario
CREATE INDEX IF NOT EXISTS idx_alertas_usuario_id ON alertas(usuario_id);

-- Índice para búsqueda de alertas no leídas
CREATE INDEX IF NOT EXISTS idx_alertas_usuario_leida ON alertas(usuario_id, leida);

-- Índice único para email de usuario (ya debería existir, pero por si acaso)
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

