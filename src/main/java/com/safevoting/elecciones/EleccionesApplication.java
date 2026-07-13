package com.safevoting.elecciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EleccionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(EleccionesApplication.class, args);
    }
}
