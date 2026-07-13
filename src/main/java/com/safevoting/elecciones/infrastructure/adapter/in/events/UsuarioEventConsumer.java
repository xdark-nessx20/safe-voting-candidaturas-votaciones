package com.safevoting.elecciones.infrastructure.adapter.in.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safevoting.elecciones.application.miembro.ActualizarSnapshotUseCase;
import com.safevoting.elecciones.application.miembro.DesactivarMiembroUseCase;
import com.safevoting.elecciones.application.miembro.ReactivarMiembroUseCase;
import com.safevoting.elecciones.application.miembro.event.UsuarioActualizadoEvent;
import com.safevoting.elecciones.application.miembro.event.UsuarioHabilitadoEvent;
import com.safevoting.elecciones.application.miembro.event.UsuarioInhabilitadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UsuarioEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UsuarioEventConsumer.class);

    private final ReactivarMiembroUseCase reactivarMiembroUseCase;
    private final DesactivarMiembroUseCase desactivarMiembroUseCase;
    private final ActualizarSnapshotUseCase actualizarSnapshotUseCase;
    private final ObjectMapper objectMapper;

    public UsuarioEventConsumer(ReactivarMiembroUseCase reactivarMiembroUseCase,
                                 DesactivarMiembroUseCase desactivarMiembroUseCase,
                                 ActualizarSnapshotUseCase actualizarSnapshotUseCase,
                                 ObjectMapper objectMapper) {
        this.reactivarMiembroUseCase = reactivarMiembroUseCase;
        this.desactivarMiembroUseCase = desactivarMiembroUseCase;
        this.actualizarSnapshotUseCase = actualizarSnapshotUseCase;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "m2.miembros.usuario-sync")
    public void handleEvent(Message message, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        try {
            byte[] body = message.getBody();
            switch (routingKey) {
                case "usuario.habilitado" -> {
                    UsuarioHabilitadoEvent event = objectMapper.readValue(body, UsuarioHabilitadoEvent.class);
                    log.info("Procesando evento usuario.habilitado: {}", event.eventId());
                    reactivarMiembroUseCase.ejecutar(event)
                            .doOnError(e -> log.error("Error procesando evento usuario.habilitado: {}", e.getMessage(), e))
                            .subscribe();
                }
                case "usuario.inhabilitado" -> {
                    UsuarioInhabilitadoEvent event = objectMapper.readValue(body, UsuarioInhabilitadoEvent.class);
                    log.info("Procesando evento usuario.inhabilitado: {}", event.eventId());
                    desactivarMiembroUseCase.ejecutar(event)
                            .doOnError(e -> log.error("Error procesando evento usuario.inhabilitado: {}", e.getMessage(), e))
                            .subscribe();
                }
                case "usuario.actualizado" -> {
                    UsuarioActualizadoEvent event = objectMapper.readValue(body, UsuarioActualizadoEvent.class);
                    log.info("Procesando evento usuario.actualizado: {}", event.eventId());
                    actualizarSnapshotUseCase.ejecutar(event)
                            .doOnError(e -> log.error("Error procesando evento usuario.actualizado: {}", e.getMessage(), e))
                            .subscribe();
                }
                default -> log.warn("Routing key no reconocida: {}", routingKey);
            }
        } catch (IOException e) {
            log.error("Error deserializando evento: {}", e.getMessage(), e);
        }
    }
}
