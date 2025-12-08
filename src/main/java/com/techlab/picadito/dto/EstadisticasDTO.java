package com.techlab.picadito.dto;

import java.util.List;
import java.util.Map;

public class EstadisticasDTO {

    private Long totalPartidos;
    private Long totalReservas;
    private Long totalUsuarios;
    private Double ingresosTotales;
    private Double ingresosPorPeriodo;
    private List<PartidoPopularDTO> partidosPopulares;
    private List<UsuarioActivoDTO> usuariosActivos;
    private List<SedeUtilizadaDTO> sedesUtilizadas;
    private Map<String, Long> partidosPorCategoria;
    private Double tasaOcupacionPromedio;

    public Long getTotalPartidos() {
        return totalPartidos;
    }

    public void setTotalPartidos(Long totalPartidos) {
        this.totalPartidos = totalPartidos;
    }

    public Long getTotalReservas() {
        return totalReservas;
    }

    public void setTotalReservas(Long totalReservas) {
        this.totalReservas = totalReservas;
    }

    public Long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(Long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public Double getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(Double ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public Double getIngresosPorPeriodo() {
        return ingresosPorPeriodo;
    }

    public void setIngresosPorPeriodo(Double ingresosPorPeriodo) {
        this.ingresosPorPeriodo = ingresosPorPeriodo;
    }

    public List<PartidoPopularDTO> getPartidosPopulares() {
        return partidosPopulares;
    }

    public void setPartidosPopulares(List<PartidoPopularDTO> partidosPopulares) {
        this.partidosPopulares = partidosPopulares;
    }

    public List<UsuarioActivoDTO> getUsuariosActivos() {
        return usuariosActivos;
    }

    public void setUsuariosActivos(List<UsuarioActivoDTO> usuariosActivos) {
        this.usuariosActivos = usuariosActivos;
    }

    public List<SedeUtilizadaDTO> getSedesUtilizadas() {
        return sedesUtilizadas;
    }

    public void setSedesUtilizadas(List<SedeUtilizadaDTO> sedesUtilizadas) {
        this.sedesUtilizadas = sedesUtilizadas;
    }

    public Map<String, Long> getPartidosPorCategoria() {
        return partidosPorCategoria;
    }

    public void setPartidosPorCategoria(Map<String, Long> partidosPorCategoria) {
        this.partidosPorCategoria = partidosPorCategoria;
    }

    public Double getTasaOcupacionPromedio() {
        return tasaOcupacionPromedio;
    }

    public void setTasaOcupacionPromedio(Double tasaOcupacionPromedio) {
        this.tasaOcupacionPromedio = tasaOcupacionPromedio;
    }

    // Clases internas para DTOs anidados
    public static class PartidoPopularDTO {
        private Long partidoId;
        private String titulo;
        private Integer cantidadParticipantes;
        private Integer maxJugadores;
        private Double porcentajeOcupacion;

        public Long getPartidoId() {
            return partidoId;
        }

        public void setPartidoId(Long partidoId) {
            this.partidoId = partidoId;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public Integer getCantidadParticipantes() {
            return cantidadParticipantes;
        }

        public void setCantidadParticipantes(Integer cantidadParticipantes) {
            this.cantidadParticipantes = cantidadParticipantes;
        }

        public Integer getMaxJugadores() {
            return maxJugadores;
        }

        public void setMaxJugadores(Integer maxJugadores) {
            this.maxJugadores = maxJugadores;
        }

        public Double getPorcentajeOcupacion() {
            return porcentajeOcupacion;
        }

        public void setPorcentajeOcupacion(Double porcentajeOcupacion) {
            this.porcentajeOcupacion = porcentajeOcupacion;
        }
    }

    public static class UsuarioActivoDTO {
        private Long usuarioId;
        private String nombre;
        private Long cantidadReservas;
        private Double totalGastado;

        public Long getUsuarioId() {
            return usuarioId;
        }

        public void setUsuarioId(Long usuarioId) {
            this.usuarioId = usuarioId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Long getCantidadReservas() {
            return cantidadReservas;
        }

        public void setCantidadReservas(Long cantidadReservas) {
            this.cantidadReservas = cantidadReservas;
        }

        public Double getTotalGastado() {
            return totalGastado;
        }

        public void setTotalGastado(Double totalGastado) {
            this.totalGastado = totalGastado;
        }
    }

    public static class SedeUtilizadaDTO {
        private Long sedeId;
        private String nombre;
        private Long cantidadPartidos;

        public Long getSedeId() {
            return sedeId;
        }

        public void setSedeId(Long sedeId) {
            this.sedeId = sedeId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Long getCantidadPartidos() {
            return cantidadPartidos;
        }

        public void setCantidadPartidos(Long cantidadPartidos) {
            this.cantidadPartidos = cantidadPartidos;
        }
    }
}

