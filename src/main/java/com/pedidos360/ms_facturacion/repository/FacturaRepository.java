package com.pedidos360.ms_facturacion.repository;

import com.pedidos360.ms_facturacion.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturaRepository
        extends JpaRepository<Factura, Long> {

    Optional<Factura> findByOrdenId(Long ordenId);

    boolean existsByOrdenId(Long ordenId);

    List<Factura> findByUsuarioId(String usuarioId);
}