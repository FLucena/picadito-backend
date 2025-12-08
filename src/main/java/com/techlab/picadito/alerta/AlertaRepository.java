package com.techlab.picadito.alerta;

import com.techlab.picadito.model.Alerta;
import com.techlab.picadito.model.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    
    List<Alerta> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    
    List<Alerta> findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(Long usuarioId);
    
    List<Alerta> findByTipoAndLeidaFalse(TipoAlerta tipo);
    
    @Modifying
    @Query("UPDATE Alerta a SET a.leida = true WHERE a.usuario.id = :usuarioId")
    void marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT a FROM Alerta a WHERE a.partido.id = :partidoId AND a.tipo = :tipo")
    List<Alerta> findByPartidoIdAndTipo(@Param("partidoId") Long partidoId, @Param("tipo") TipoAlerta tipo);
    
    @Query("SELECT a FROM Alerta a WHERE a.fechaCreacion < :fechaLimite")
    List<Alerta> findAlertasAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);
}

