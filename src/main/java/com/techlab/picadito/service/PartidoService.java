package com.techlab.picadito.service;

import com.techlab.picadito.dto.BusquedaPartidoDTO;
import com.techlab.picadito.dto.CategoriaResponseDTO;
import com.techlab.picadito.dto.EquipoResponseDTO;
import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.dto.ParticipanteResponseDTO;
import com.techlab.picadito.dto.SedeResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.exception.ValidationException;
import com.techlab.picadito.model.Categoria;
import com.techlab.picadito.model.EstadoPartido;
import com.techlab.picadito.model.Partido;
import com.techlab.picadito.model.Participante;
import com.techlab.picadito.model.Sede;
import com.techlab.picadito.repository.PartidoRepository;
import com.techlab.picadito.repository.SedeRepository;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class PartidoService {

    private static final Logger logger = LoggerFactory.getLogger(PartidoService.class);

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    @Lazy
    private AlertaService alertaService;

    @Autowired
    @Lazy
    private CalificacionService calificacionService;

    @Autowired
    @Lazy
    private EquipoService equipoService;

    public List<PartidoResponseDTO> obtenerTodosLosPartidos() {
        return partidoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<PartidoResponseDTO> obtenerPartidosDisponibles() {
        return partidoRepository.findByEstadoOrderByFechaHoraAsc(EstadoPartido.DISPONIBLE).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public PartidoResponseDTO obtenerPartidoPorId(@NonNull Long id) {
        logger.debug("Buscando partido con id: {}", id);
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
        logger.debug("Partido encontrado: {}", partido.getTitulo());
        return convertirADTO(partido);
    }

    @SuppressWarnings("null")
    public PartidoResponseDTO crearPartido(PartidoDTO partidoDTO) {
        logger.info("Creando nuevo partido: {} por {}", partidoDTO.getTitulo(), partidoDTO.getCreadorNombre());
        validarFechaFutura(partidoDTO.getFechaHora());

        Partido partido = crearEntidadPartido(partidoDTO);
        asignarSedeSiExiste(partido, partidoDTO.getSedeId());
        asignarCategorias(partido, partidoDTO);

        partido = partidoRepository.save(partido);
        
        // Generar alerta si hay cupos bajos
        alertaService.crearAlertaCuposBajos(partido);
        
        logger.info("Partido creado exitosamente con id: {}", partido.getId());
        return convertirADTO(partido);
    }
    
    private Partido crearEntidadPartido(PartidoDTO partidoDTO) {
        Partido partido = new Partido();
        partido.setTitulo(partidoDTO.getTitulo());
        partido.setDescripcion(partidoDTO.getDescripcion());
        partido.setFechaHora(partidoDTO.getFechaHora());
        partido.setUbicacion(partidoDTO.getUbicacion());
        partido.setMaxJugadores(partidoDTO.getMaxJugadores());
        partido.setCreadorNombre(partidoDTO.getCreadorNombre());
        partido.setPrecio(partidoDTO.getPrecio());
        partido.setImagenUrl(partidoDTO.getImagenUrl());
        partido.setEstado(EstadoPartido.DISPONIBLE);
        return partido;
    }
    
    private void asignarSedeSiExiste(Partido partido, Long sedeId) {
        if (sedeId != null) {
            Sede sede = sedeRepository.findById(sedeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + sedeId));
            partido.setSede(sede);
        }
    }

    private void asignarCategorias(Partido partido, PartidoDTO partidoDTO) {
        List<Long> categoriaIds = partidoDTO.getCategoriaIds();
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            List<Categoria> categorias = categoriaIds.stream()
                    .map(categoriaService::obtenerCategoriaEntity)
                    .collect(Collectors.toList());
            partido.setCategorias(categorias);
        } else {
            partido.setCategorias(new ArrayList<>());
        }
    }

    @SuppressWarnings("null")
    public PartidoResponseDTO actualizarPartido(@NonNull Long id, PartidoDTO partidoDTO) {
        logger.info("Actualizando partido con id: {}", id);
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));

        validarActualizacionPartido(partido, partidoDTO);
        aplicarActualizaciones(partido, partidoDTO);

        partido = partidoRepository.save(partido);
        actualizarEstadoSegunParticipantes(partido);
        
        // Generar alerta si hay cupos bajos después de actualizar
        alertaService.crearAlertaCuposBajos(partido);
        
        logger.info("Partido actualizado exitosamente");
        return convertirADTO(partido);
    }
    
    private void validarActualizacionPartido(Partido partido, PartidoDTO partidoDTO) {
        // Validar que el partido no esté finalizado o cancelado
        if (partido.getEstado() == EstadoPartido.FINALIZADO || partido.getEstado() == EstadoPartido.CANCELADO) {
            throw new BusinessException("No se puede actualizar un partido que está " + partido.getEstado().name().toLowerCase());
        }

        if (partidoDTO.getFechaHora() != null) {
            validarFechaFutura(partidoDTO.getFechaHora());
        }
        
        // Validar que no se reduzca el número máximo de jugadores por debajo de la cantidad actual
        if (partidoDTO.getMaxJugadores() != null && 
            partidoDTO.getMaxJugadores() < partido.getCantidadParticipantes()) {
            throw new BusinessException("No se puede reducir el número máximo de jugadores por debajo de la cantidad actual de participantes");
        }
    }
    
    private void aplicarActualizaciones(Partido partido, PartidoDTO partidoDTO) {
        if (partidoDTO.getTitulo() != null) {
            partido.setTitulo(partidoDTO.getTitulo());
        }
        if (partidoDTO.getDescripcion() != null) {
            partido.setDescripcion(partidoDTO.getDescripcion());
        }
        if (partidoDTO.getFechaHora() != null) {
            partido.setFechaHora(partidoDTO.getFechaHora());
        }
        if (partidoDTO.getUbicacion() != null) {
            partido.setUbicacion(partidoDTO.getUbicacion());
        }
        
        actualizarSede(partido, partidoDTO.getSedeId());
        actualizarCategorias(partido, partidoDTO);
        
        if (partidoDTO.getMaxJugadores() != null) {
            partido.setMaxJugadores(partidoDTO.getMaxJugadores());
        }
        if (partidoDTO.getPrecio() != null) {
            partido.setPrecio(partidoDTO.getPrecio());
        }
        if (partidoDTO.getImagenUrl() != null) {
            partido.setImagenUrl(partidoDTO.getImagenUrl());
        }
    }
    
    private void actualizarSede(Partido partido, Long sedeId) {
        if (sedeId != null) {
            Sede sede = sedeRepository.findById(sedeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + sedeId));
            partido.setSede(sede);
        } else if (sedeId == null && partido.getSede() != null) {
            // Si se envía null explícitamente, remover la sede
            partido.setSede(null);
        }
    }

    private void actualizarCategorias(Partido partido, PartidoDTO partidoDTO) {
        List<Long> categoriaIds = partidoDTO.getCategoriaIds();
        if (categoriaIds == null || categoriaIds.isEmpty()) {
            partido.setCategorias(new ArrayList<>());
        } else {
            List<Categoria> categorias = categoriaIds.stream()
                    .map(categoriaService::obtenerCategoriaEntity)
                    .collect(Collectors.toList());
            partido.setCategorias(categorias);
        }
    }

    public void eliminarPartido(@NonNull Long id) {
        logger.info("Eliminando partido con id: {}", id);
        if (!partidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + id);
        }
        partidoRepository.deleteById(id);
        logger.info("Partido eliminado exitosamente");
    }

    public void actualizarEstadoSegunParticipantes(Partido partido) {
        boolean necesitaGuardar = false;
        if (partido.estaCompleto() && partido.getEstado() == EstadoPartido.DISPONIBLE) {
            partido.setEstado(EstadoPartido.COMPLETO);
            necesitaGuardar = true;
        } else if (!partido.estaCompleto() && partido.getEstado() == EstadoPartido.COMPLETO) {
            partido.setEstado(EstadoPartido.DISPONIBLE);
            necesitaGuardar = true;
        }
        
        if (necesitaGuardar) {
            partidoRepository.save(partido);
        }
    }

    public Partido obtenerPartidoEntity(@NonNull Long id) {
        return partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
    }

    /**
     * Calcula el costo por jugador en un partido
     * Si el partido tiene precio, divide el precio entre el número máximo de jugadores
     * @param partidoId ID del partido
     * @return Costo por jugador, o null si el partido no tiene precio
     */
    public Double calcularCostoPorJugador(@NonNull Long partidoId) {
        Partido partido = obtenerPartidoEntity(partidoId);
        
        if (partido.getPrecio() == null) {
            return null;
        }
        
        if (partido.getMaxJugadores() == null || partido.getMaxJugadores() <= 0) {
            throw new BusinessException("El partido no tiene un número válido de jugadores máximos");
        }
        
        return partido.getPrecio() / partido.getMaxJugadores();
    }

    public List<PartidoResponseDTO> buscarPartidos(BusquedaPartidoDTO busqueda) {
        logger.info("Buscando partidos con criterios: {}", busqueda);
        
        Specification<Partido> spec = crearSpecification(busqueda);
        List<Partido> partidos = partidoRepository.findAll(spec);
        
        logger.info("Se encontraron {} partidos", partidos.size());
        return partidos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private Specification<Partido> crearSpecification(BusquedaPartidoDTO busqueda) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            agregarFiltroTitulo(predicates, busqueda, root, cb);
            agregarFiltroUbicacion(predicates, busqueda, root, cb);
            agregarFiltroCreador(predicates, busqueda, root, cb);
            agregarFiltroEstado(predicates, busqueda, root, cb);
            agregarFiltrosFecha(predicates, busqueda, root, cb);
            agregarFiltrosJugadores(predicates, busqueda, root, cb);
            agregarFiltroCuposDisponibles(predicates, busqueda, root, cb);
            agregarFiltroSoloDisponibles(predicates, busqueda, root, cb);
            agregarFiltroCategoria(predicates, busqueda, root, cb, query);
            aplicarOrdenamiento(query, root, cb);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private void agregarFiltroTitulo(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (busqueda.getTitulo() != null && !busqueda.getTitulo().trim().isEmpty()) {
            predicates.add(cb.like(
                cb.lower(root.get("titulo")),
                "%" + busqueda.getTitulo().toLowerCase() + "%"
            ));
        }
    }
    
    private void agregarFiltroUbicacion(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (busqueda.getUbicacion() != null && !busqueda.getUbicacion().trim().isEmpty()) {
            predicates.add(cb.like(
                cb.lower(root.get("ubicacion")),
                "%" + busqueda.getUbicacion().toLowerCase() + "%"
            ));
        }
    }
    
    private void agregarFiltroCreador(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (busqueda.getCreadorNombre() != null && !busqueda.getCreadorNombre().trim().isEmpty()) {
            predicates.add(cb.like(
                cb.lower(root.get("creadorNombre")),
                "%" + busqueda.getCreadorNombre().toLowerCase() + "%"
            ));
        }
    }
    
    private void agregarFiltroEstado(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (busqueda.getEstado() != null) {
            predicates.add(cb.equal(root.get("estado"), busqueda.getEstado()));
        }
    }
    
    private void agregarFiltrosFecha(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (busqueda.getFechaDesde() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), busqueda.getFechaDesde()));
        }
        if (busqueda.getFechaHasta() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), busqueda.getFechaHasta()));
        }
    }
    
    private void agregarFiltrosJugadores(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (busqueda.getMinJugadores() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("maxJugadores"), busqueda.getMinJugadores()));
        }
        if (busqueda.getMaxJugadores() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("maxJugadores"), busqueda.getMaxJugadores()));
        }
    }
    
    private void agregarFiltroCuposDisponibles(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (busqueda.getCuposDisponiblesMin() != null) {
            // Cupos disponibles = maxJugadores - cantidadParticipantes
            predicates.add(cb.greaterThanOrEqualTo(
                cb.diff(root.get("maxJugadores"), 
                    cb.size(root.get("participantes"))),
                busqueda.getCuposDisponiblesMin()
            ));
        }
    }
    
    private void agregarFiltroSoloDisponibles(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb) {
        if (Boolean.TRUE.equals(busqueda.getSoloDisponibles())) {
            predicates.add(cb.equal(root.get("estado"), EstadoPartido.DISPONIBLE));
        }
    }

    private void agregarFiltroCategoria(List<Predicate> predicates, BusquedaPartidoDTO busqueda, Root<Partido> root, CriteriaBuilder cb, CriteriaQuery<?> query) {
        List<Long> categoriaIds = busqueda.getCategoriaIds();
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            // Filtrar partidos que tengan al menos una de las categorías especificadas
            Join<Partido, Categoria> categoriasJoin = root.join("categorias");
            predicates.add(categoriasJoin.get("id").in(categoriaIds));
            // Evitar duplicados cuando hay join con categorias
            query.distinct(true);
        }
    }
    
    private void aplicarOrdenamiento(CriteriaQuery<?> query, Root<Partido> root, CriteriaBuilder cb) {
        if (query != null) {
            query.orderBy(cb.asc(root.get("fechaHora")));
        }
    }

    private void validarFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora != null && fechaHora.isBefore(LocalDateTime.now())) {
            throw new ValidationException("La fecha y hora deben ser en el futuro");
        }
    }

    private PartidoResponseDTO convertirADTO(Partido partido) {
        PartidoResponseDTO dto = mapearCamposBasicos(partido);
        asignarSedeADTO(dto, partido);
        asignarCategoriasADTO(dto, partido);
        asignarParticipantesADTO(dto, partido);
        asignarPromedioCalificacion(dto, partido);
        asignarEquiposADTO(dto, partido);
        return dto;
    }
    
    private PartidoResponseDTO mapearCamposBasicos(Partido partido) {
        PartidoResponseDTO dto = new PartidoResponseDTO();
        dto.setId(partido.getId());
        dto.setTitulo(partido.getTitulo());
        dto.setDescripcion(partido.getDescripcion());
        dto.setFechaHora(partido.getFechaHora());
        dto.setUbicacion(partido.getUbicacion());
        dto.setMaxJugadores(partido.getMaxJugadores());
        dto.setEstado(partido.getEstado());
        dto.setCreadorNombre(partido.getCreadorNombre());
        dto.setFechaCreacion(partido.getFechaCreacion());
        dto.setCantidadParticipantes(partido.getCantidadParticipantes());
        dto.setPrecio(partido.getPrecio());
        dto.setImagenUrl(partido.getImagenUrl());
        return dto;
    }
    
    private void asignarSedeADTO(PartidoResponseDTO dto, Partido partido) {
        if (partido.getSede() != null) {
            dto.setSedeId(partido.getSede().getId());
            dto.setSede(convertirSedeADTO(partido.getSede()));
        }
    }

    private void asignarCategoriasADTO(PartidoResponseDTO dto, Partido partido) {
        if (partido.getCategorias() != null && !partido.getCategorias().isEmpty()) {
            List<CategoriaResponseDTO> categoriasDTO = partido.getCategorias().stream()
                    .map(this::convertirCategoriaADTO)
                    .collect(Collectors.toList());
            dto.setCategorias(categoriasDTO);
            
            List<Long> categoriaIds = partido.getCategorias().stream()
                    .map(Categoria::getId)
                    .collect(Collectors.toList());
            dto.setCategoriaIds(categoriaIds);
        } else {
            dto.setCategorias(new ArrayList<>());
            dto.setCategoriaIds(new ArrayList<>());
        }
    }

    private CategoriaResponseDTO convertirCategoriaADTO(Categoria categoria) {
        CategoriaResponseDTO categoriaDTO = new CategoriaResponseDTO();
        categoriaDTO.setId(categoria.getId());
        categoriaDTO.setNombre(categoria.getNombre());
        categoriaDTO.setDescripcion(categoria.getDescripcion());
        categoriaDTO.setIcono(categoria.getIcono());
        categoriaDTO.setColor(categoria.getColor());
        categoriaDTO.setFechaCreacion(categoria.getFechaCreacion());
        categoriaDTO.setFechaActualizacion(categoria.getFechaActualizacion());
        return categoriaDTO;
    }

    private void asignarPromedioCalificacion(PartidoResponseDTO dto, Partido partido) {
        try {
            Long partidoId = Objects.requireNonNull(partido.getId(), "El partido debe tener un ID");
            Double promedio = calificacionService.obtenerPromedioPorPartido(partidoId);
            dto.setPromedioCalificacion(promedio > 0 ? promedio : null);
        } catch (Exception e) {
            logger.debug("No se pudo obtener el promedio de calificaciones para el partido {}: {}", 
                    partido.getId(), e.getMessage());
            dto.setPromedioCalificacion(null);
        }
    }

    private void asignarEquiposADTO(PartidoResponseDTO dto, Partido partido) {
        try {
            Long partidoId = Objects.requireNonNull(partido.getId(), "El partido debe tener un ID");
            List<EquipoResponseDTO> equipos = equipoService.obtenerEquiposPorPartido(partidoId);
            dto.setEquipos(equipos);
        } catch (Exception e) {
            logger.debug("No se pudieron obtener los equipos para el partido {}: {}", 
                    partido.getId(), e.getMessage());
            dto.setEquipos(new ArrayList<>());
        }
    }
    
    private SedeResponseDTO convertirSedeADTO(Sede sede) {
        SedeResponseDTO sedeDTO = new SedeResponseDTO();
        sedeDTO.setId(sede.getId());
        sedeDTO.setNombre(sede.getNombre());
        sedeDTO.setDireccion(sede.getDireccion());
        sedeDTO.setDescripcion(sede.getDescripcion());
        sedeDTO.setTelefono(sede.getTelefono());
        sedeDTO.setCoordenadas(sede.getCoordenadas());
        sedeDTO.setFechaCreacion(sede.getFechaCreacion());
        sedeDTO.setFechaActualizacion(sede.getFechaActualizacion());
        return sedeDTO;
    }
    
    private void asignarParticipantesADTO(PartidoResponseDTO dto, Partido partido) {
        if (partido.getParticipantes() != null && !partido.getParticipantes().isEmpty()) {
            List<ParticipanteResponseDTO> participantesDTO = partido.getParticipantes().stream()
                    .map(this::convertirParticipanteADTO)
                    .collect(Collectors.toList());
            dto.setParticipantes(participantesDTO);
        } else {
            dto.setParticipantes(new ArrayList<>());
        }
    }

    private ParticipanteResponseDTO convertirParticipanteADTO(Participante participante) {
        ParticipanteResponseDTO dto = new ParticipanteResponseDTO();
        dto.setId(participante.getId());
        dto.setNombre(participante.getNombre());
        dto.setApodo(participante.getApodo());
        dto.setPosicion(participante.getPosicion());
        dto.setNivel(participante.getNivel());
        dto.setFechaInscripcion(participante.getFechaInscripcion());
        return dto;
    }
}

