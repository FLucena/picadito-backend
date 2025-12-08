package com.techlab.picadito.partido;

import com.techlab.picadito.dto.BusquedaPartidoDTO;
import com.techlab.picadito.dto.CategoriaResponseDTO;
import com.techlab.picadito.dto.EquiposResponseDTO;
import com.techlab.picadito.dto.PageResponseDTO;
import com.techlab.picadito.dto.PartidoDTO;
import com.techlab.picadito.dto.PartidoResponseDTO;
import com.techlab.picadito.dto.PartidosResponseDTO;
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
import com.techlab.picadito.sede.SedeRepository;
import com.techlab.picadito.categoria.CategoriaService;
import com.techlab.picadito.alerta.AlertaService;
import com.techlab.picadito.calificacion.CalificacionService;
import com.techlab.picadito.equipo.EquipoService;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    public PageResponseDTO<PartidoResponseDTO> obtenerTodosLosPartidos(Pageable pageable) {
        logger.debug("Obteniendo todos los partidos paginados - página: {}, tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Partido> partidosPage = partidoRepository.findAll(pageable);
        
        List<PartidoResponseDTO> partidosDTO = partidosPage.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        
        return PageResponseDTO.of(
                partidosDTO,
                partidosPage.getNumber(),
                partidosPage.getSize(),
                partidosPage.getTotalElements()
        );
    }

    public PageResponseDTO<PartidoResponseDTO> obtenerPartidosDisponibles(Pageable pageable) {
        try {
            logger.debug("Obteniendo partidos disponibles paginados - página: {}, tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
            Specification<Partido> spec = (root, query, cb) -> 
                cb.equal(root.get("estado"), EstadoPartido.DISPONIBLE);
            
            Page<Partido> partidosPage = partidoRepository.findAll(spec, pageable);
            logger.debug("Se encontraron {} partidos disponibles (total: {})", partidosPage.getNumberOfElements(), partidosPage.getTotalElements());
            
            List<PartidoResponseDTO> partidosDTO = partidosPage.getContent().stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            
            return PageResponseDTO.of(
                    partidosDTO,
                    partidosPage.getNumber(),
                    partidosPage.getSize(),
                    partidosPage.getTotalElements()
            );
        } catch (Exception e) {
            logger.error("Error al obtener partidos disponibles", e);
            throw e;
        }
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
        try {
            logger.info("Creando nuevo partido: {} por {}", partidoDTO.getTitulo(), partidoDTO.getCreadorNombre());
            logger.debug("PartidoDTO recibido - categoriaIds: {}", partidoDTO.getCategoriaIds());
            validarFechaFutura(partidoDTO.getFechaHora());

            Partido partido = crearEntidadPartido(partidoDTO);
            asignarSedeSiExiste(partido, partidoDTO.getSedeId());
            asignarCategorias(partido, partidoDTO);
            
            logger.debug("Partido antes de guardar - categorias size: {}", 
                    partido.getCategorias() != null ? partido.getCategorias().size() : 0);

            partido = partidoRepository.save(partido);
            logger.debug("Partido guardado con id: {}", partido.getId());
            
            // Recargar el partido para asegurar que las relaciones estén cargadas
            partido = partidoRepository.findById(partido.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado después de guardar"));
            logger.debug("Partido recargado - categorias size: {}", 
                    partido.getCategorias() != null ? partido.getCategorias().size() : 0);
            
            // Generar alerta si hay cupos bajos
            try {
                alertaService.crearAlertaCuposBajos(partido);
            } catch (Exception e) {
                logger.warn("No se pudo crear alerta de cupos bajos para partido {}: {}", 
                        partido.getId(), e.getMessage());
            }
            
            logger.info("Partido creado exitosamente con id: {}", partido.getId());
            return convertirADTO(partido);
        } catch (Exception e) {
            logger.error("Error al crear partido: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private Partido crearEntidadPartido(PartidoDTO partidoDTO) {
        Partido partido = new Partido();
        partido.setTitulo(partidoDTO.getTitulo());
        partido.setDescripcion(partidoDTO.getDescripcion());
        partido.setFechaHora(partidoDTO.getFechaHora());
        partido.setUbicacion(partidoDTO.getUbicacion());
        partido.setMaxJugadores(normalizarMaxJugadores(partidoDTO.getMaxJugadores()));
        partido.setCreadorNombre(partidoDTO.getCreadorNombre());
        partido.setPrecio(partidoDTO.getPrecio());
        partido.setImagenUrl(partidoDTO.getImagenUrl());
        partido.setEstado(EstadoPartido.DISPONIBLE);
        return partido;
    }
    
    /**
     * Normaliza el número máximo de jugadores para partidos aleatorios:
     * - Debe estar entre 10 y 22 (inclusive)
     * - Debe ser un número par
     * 
     * @param maxJugadores Número de jugadores a normalizar
     * @return Número normalizado (par entre 10 y 22)
     */
    private Integer normalizarMaxJugadores(Integer maxJugadores) {
        if (maxJugadores == null) {
            return 22; // Valor por defecto
        }
        
        // Asegurar que esté en el rango 10-22
        int normalizado = Math.max(10, Math.min(22, maxJugadores));
        
        // Asegurar que sea par
        if (normalizado % 2 != 0) {
            // Si es impar, redondear hacia abajo al par más cercano
            normalizado = normalizado - 1;
            // Si al restar 1 queda fuera del rango mínimo, usar el mínimo par
            if (normalizado < 10) {
                normalizado = 10;
            }
        }
        
        return normalizado;
    }
    
    private void asignarSedeSiExiste(Partido partido, Long sedeId) {
        if (sedeId != null) {
            Sede sede = sedeRepository.findById(sedeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + sedeId));
            partido.setSede(sede);
        }
    }

    private void asignarCategorias(Partido partido, PartidoDTO partidoDTO) {
        try {
            List<Long> categoriaIds = partidoDTO.getCategoriaIds();
            logger.debug("Asignando categorías - categoriaIds recibidos: {}", categoriaIds);
            
            if (categoriaIds != null && !categoriaIds.isEmpty()) {
                Set<Categoria> categorias = new HashSet<>();
                for (Long categoriaId : categoriaIds) {
                    if (categoriaId == null) {
                        logger.warn("Se encontró un categoriaId null, omitiendo");
                        continue;
                    }
                    try {
                        Categoria categoria = categoriaService.obtenerCategoriaEntity(categoriaId);
                        categorias.add(categoria);
                        logger.debug("Categoría {} agregada al partido", categoriaId);
                    } catch (Exception e) {
                        logger.error("Error al obtener categoría con id {}: {}", categoriaId, e.getMessage());
                        throw new ResourceNotFoundException("Categoría no encontrada con id: " + categoriaId);
                    }
                }
                partido.setCategorias(categorias);
                logger.debug("{} categorías asignadas al partido", categorias.size());
            } else {
                partido.setCategorias(new HashSet<>());
                logger.debug("No se asignaron categorías (lista vacía o null)");
            }
        } catch (Exception e) {
            logger.error("Error al asignar categorías: {}", e.getMessage(), e);
            throw e;
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
            partido.setMaxJugadores(normalizarMaxJugadores(partidoDTO.getMaxJugadores()));
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
            partido.setCategorias(new HashSet<>());
        } else {
            Set<Categoria> categorias = new HashSet<>();
            for (Long categoriaId : categoriaIds) {
                if (categoriaId != null) {
                    Categoria categoria = categoriaService.obtenerCategoriaEntity(categoriaId);
                    categorias.add(categoria);
                }
            }
            partido.setCategorias(categorias);
        }
    }

    public void eliminarPartido(@NonNull Long id) {
        logger.info("Eliminando partido con id: {}", id);
        if (!partidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + id);
        }
        
        try {
            partidoRepository.deleteById(id);
            logger.info("Partido eliminado exitosamente");
        } catch (DataIntegrityViolationException e) {
            logger.warn("No se puede eliminar el partido {} debido a restricciones de integridad referencial: {}", 
                    id, e.getMessage());
            throw new BusinessException(
                "No se puede eliminar el partido. Puede tener participantes inscritos, reservas asociadas, " +
                "equipos generados, calificaciones, partidos guardados o seleccionados que impiden su eliminación."
            );
        }
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

    public PageResponseDTO<PartidoResponseDTO> buscarPartidos(BusquedaPartidoDTO busqueda, Pageable pageable) {
        logger.info("Buscando partidos con criterios: {} - página: {}, tamaño: {}", busqueda, pageable.getPageNumber(), pageable.getPageSize());
        
        Specification<Partido> spec = crearSpecification(busqueda);
        Page<Partido> partidosPage = partidoRepository.findAll(spec, pageable);
        
        logger.info("Se encontraron {} partidos (total: {})", partidosPage.getNumberOfElements(), partidosPage.getTotalElements());
        List<PartidoResponseDTO> partidosDTO = partidosPage.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        
        return PageResponseDTO.of(
                partidosDTO,
                partidosPage.getNumber(),
                partidosPage.getSize(),
                partidosPage.getTotalElements()
        );
    }
    
    // Método legacy para compatibilidad (sin paginación)
    public PartidosResponseDTO buscarPartidos(BusquedaPartidoDTO busqueda) {
        logger.info("Buscando partidos con criterios (sin paginación): {}", busqueda);
        Pageable pageable = PageRequest.of(0, 1000); // Límite alto para compatibilidad
        PageResponseDTO<PartidoResponseDTO> pageResult = buscarPartidos(busqueda, pageable);
        return new PartidosResponseDTO(pageResult.getContent());
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
        try {
            PartidoResponseDTO dto = mapearCamposBasicos(partido);
            asignarSedeADTO(dto, partido);
            asignarCategoriasADTO(dto, partido);
            asignarParticipantesADTO(dto, partido);
            asignarPromedioCalificacion(dto, partido);
            asignarEquiposADTO(dto, partido);
            return dto;
        } catch (Exception e) {
            logger.error("Error al convertir partido {} a DTO", partido.getId(), e);
            throw new RuntimeException("Error al convertir partido a DTO: " + e.getMessage(), e);
        }
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
        try {
            Set<Categoria> categorias = partido.getCategorias();
            if (categorias != null && !categorias.isEmpty()) {
                List<CategoriaResponseDTO> categoriasDTO = new ArrayList<>();
                List<Long> categoriaIds = new ArrayList<>();
                
                for (Categoria categoria : categorias) {
                    if (categoria != null) {
                        categoriasDTO.add(convertirCategoriaADTO(categoria));
                        categoriaIds.add(categoria.getId());
                    }
                }
                
                dto.setCategorias(categoriasDTO);
                dto.setCategoriaIds(categoriaIds);
            } else {
                dto.setCategorias(new ArrayList<>());
                dto.setCategoriaIds(new ArrayList<>());
            }
        } catch (Exception e) {
            logger.warn("Error al asignar categorías al DTO para partido {}: {}", 
                    partido.getId(), e.getMessage());
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
            EquiposResponseDTO equiposResponse = equipoService.obtenerEquiposPorPartido(partidoId);
            dto.setEquipos(equiposResponse != null && equiposResponse.getEquipos() != null 
                    ? equiposResponse.getEquipos() : new ArrayList<>());
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

