package com.pedidos360.ms_facturacion.controller;

import com.pedidos360.ms_facturacion.entity.Factura;
import com.pedidos360.ms_facturacion.service.FacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService service;

    public FacturaController(FacturaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Factura> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> getById(
            @PathVariable Long id
    ) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<Factura> getByOrden(
            @PathVariable Long ordenId
    ) {
        return service.findByOrdenId(ordenId)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Factura> getByUsuario(
            @PathVariable String usuarioId
    ) {
        return service.findByUsuario(usuarioId);
    }
}