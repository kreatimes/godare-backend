package com.godare.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Componente de diagnostico que lista los indices de MongoDB al arrancar.
// Solo se usa en desarrollo para verificar que los indices se crearon bien.
@Component
public class IndexChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexChecker.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Verificando indices de MongoDB...");

        log.info("--- Users ---");
        mongoTemplate.getCollection("users").listIndexes()
                .forEach(index -> log.info(index.toString()));

        log.info("--- Posts ---");
        mongoTemplate.getCollection("posts").listIndexes()
                .forEach(index -> log.info(index.toString()));

        log.info("Comprobacion de indices completada.");
    }
}
