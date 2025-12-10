-- Migración: Agregar campos de seguridad a la tabla usuarios
-- Fecha: 2024
-- Descripción: Agrega campos para bloqueo de cuenta y seguimiento de intentos fallidos

-- Agregar columna de intentos fallidos
ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS intentos_fallidos INTEGER DEFAULT 0 NOT NULL;

-- Agregar columna de cuenta bloqueada
ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS cuenta_bloqueada BOOLEAN DEFAULT FALSE NOT NULL;

-- Agregar columna de fecha de bloqueo
ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS fecha_bloqueo TIMESTAMP;

-- Actualizar valores existentes
UPDATE usuarios 
SET intentos_fallidos = 0 
WHERE intentos_fallidos IS NULL;

UPDATE usuarios 
SET cuenta_bloqueada = FALSE 
WHERE cuenta_bloqueada IS NULL;

-- Comentarios para documentación
COMMENT ON COLUMN usuarios.intentos_fallidos IS 'Número de intentos de login fallidos consecutivos';
COMMENT ON COLUMN usuarios.cuenta_bloqueada IS 'Indica si la cuenta está bloqueada por múltiples intentos fallidos';
COMMENT ON COLUMN usuarios.fecha_bloqueo IS 'Fecha y hora en que la cuenta fue bloqueada';

