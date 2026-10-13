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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaService {

    private static final Logger log =
            LoggerFactory.getLogger(FacturaService.class);

    // IVA Chile: 19%. El precio al consumidor ya lo incluye.
    private static final BigDecimal FACTOR_IVA = new BigDecimal("1.19");

    private static final String TIPO_DOCUMENTO = "BOLETA ELECTRÓNICA";

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

        factura.setTipoDocumento(TIPO_DOCUMENTO);
        factura.setUsuarioId(evento.getUsuarioId());
        factura.setNombreCliente(evento.getNombreCliente());
        factura.setEmail(evento.getEmail());

        // Desglose tributario: Total = Neto + IVA
        BigDecimal total = evento.getTotal();
        BigDecimal neto = total.divide(FACTOR_IVA, 0, RoundingMode.HALF_UP);
        BigDecimal iva = total.subtract(neto);

        factura.setNeto(neto);
        factura.setIva(iva);
        factura.setTotal(total);

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
                "{} {} generada para la orden {} (neto {}, IVA {}, total {})",
                TIPO_DOCUMENTO,
                guardada.getNumeroFactura(),
                guardada.getOrdenId(),
                neto,
                iva,
                total
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