package com.example.repository;

import com.example.model.Compra;
import com.example.model.Usuario;
import com.example.model.Juego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioOrderByFechaCompraDesc(Usuario usuario);
    List<Compra> findByJuego(Juego juego);
    boolean existsByUsuarioAndJuego(Usuario usuario, Juego juego);
    
    @Query("SELECT SUM(c.precioPagado) FROM Compra c")
    BigDecimal calcularTotalVentas();
    
    @Transactional
    void deleteByUsuario(Usuario usuario);
}
