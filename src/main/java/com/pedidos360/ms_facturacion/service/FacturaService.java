package com.pedidos360.ms_facturacion.service;

import com.pedidos360.ms_facturacion.entity.Factura;
import com.pedidos360.ms_facturacion.entity.FacturaDetalle;
import com.pedidos360.ms_facturacion.event.OrdenCreadaEvent;
import com.pedidos360.ms_facturacion.event.OrdenItemEvent;
import com.pedidos360.ms_facturacion.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaService {

    private static final Logger log =
            LoggerFactory.getLogger(FacturaService.class);

    private final FacturaRepository repository;

    public FacturaService(FacturaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Factura generarFactura(OrdenCreadaEvent evento) {

        // Evita generar facturas duplicadas si RabbitMQ
        // vuelve a entregar el mismo mensaje.
        Optional<Factura> existente =
                repository.findByOrdenId(evento.getOrdenId());

        if (existente.isPresent()) {

            log.info(
                    "La orden {} ya posee la factura {}",
                    evento.getOrdenId(),
                    existente.get().getNumeroFactura()
            );

            return existente.get();
        }

        Factura factura = new Factura();

        factura.setOrdenId(evento.getOrdenId());

        factura.setNumeroFactura(
                String.format(
                        "FAC-%08d",
                        evento.getOrdenId()
                )
        );

        factura.setUsuarioId(evento.getUsuarioId());
        factura.setEmail(evento.getEmail());
        factura.setTotal(evento.getTotal());
        factura.setEstado("EMITIDA");
        factura.setFechaEmision(LocalDateTime.now());

        for (OrdenItemEvent item : evento.getItems()) {

            FacturaDetalle detalle =
                    new FacturaDetalle(
                            item.getProductoId(),
                            item.getNombreProducto(),
                            item.getPrecioUnitario(),
                            item.getCantidad()
                    );

            factura.agregarDetalle(detalle);
        }

        Factura guardada = repository.save(factura);

        log.info(
                "Factura {} generada para la orden {}",
                guardada.getNumeroFactura(),
                guardada.getOrdenId()
        );

        return guardada;
    }

    public List<Factura> findAll() {
        return repository.findAll();
    }

    public Optional<Factura> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Factura> findByOrdenId(Long ordenId) {
        return repository.findByOrdenId(ordenId);
    }

    public List<Factura> findByUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }
}