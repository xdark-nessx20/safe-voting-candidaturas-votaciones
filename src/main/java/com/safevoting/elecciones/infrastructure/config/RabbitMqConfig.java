package com.safevoting.elecciones.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "safevoting.usuarios.events";
    public static final String QUEUE = "m2.miembros.usuario-sync";
    public static final String RK_HABILITADO = "usuario.habilitado";
    public static final String RK_INHABILITADO = "usuario.inhabilitado";
    public static final String RK_ACTUALIZADO = "usuario.actualizado";

    @Bean
    public TopicExchange usuarioExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue miembroUsuarioSyncQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding habilitadoBinding() {
        return BindingBuilder.bind(miembroUsuarioSyncQueue()).to(usuarioExchange()).with(RK_HABILITADO);
    }

    @Bean
    public Binding inhabilitadoBinding() {
        return BindingBuilder.bind(miembroUsuarioSyncQueue()).to(usuarioExchange()).with(RK_INHABILITADO);
    }

    @Bean
    public Binding actualizadoBinding() {
        return BindingBuilder.bind(miembroUsuarioSyncQueue()).to(usuarioExchange()).with(RK_ACTUALIZADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
