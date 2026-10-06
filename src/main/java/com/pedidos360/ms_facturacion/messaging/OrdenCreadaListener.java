package com.pedidos360.ms_facturacion.messaging;

import com.pedidos360.ms_facturacion.config.RabbitMQConfig;
import com.pedidos360.ms_facturacion.event.OrdenCreadaEvent;
import com.pedidos360.ms_facturacion.service.FacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrdenCreadaListener {

    private static final Logger log =
            LoggerFactory.getLogger(OrdenCreadaListener.class);

    private final FacturaService service;

    public OrdenCreadaListener(FacturaService service) {
        this.service = service;
    }

    @RabbitListener(
            queues = RabbitMQConfig.QUEUE_FACTURACION
    )
    public void recibirOrdenCreada(
            OrdenCreadaEvent evento
    ) {

        log.info(
                "Evento orden.creada recibido -> orden {}, usuario {}",
                evento.getOrdenId(),
                evento.getUsuarioId()
        );

        service.generarFactura(evento);
    }
}