package com.godare.app.service;

import com.mongodb.client.model.IndexOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

// Crea los indices de MongoDB al arrancar la aplicacion.
// Es importante tenerlos para que las consultas del feed y el perfil vayan rapidas.
@Component
@RequiredArgsConstructor
public class MongoIndexManager {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {

        // Usuarios — nick y email unicos para evitar duplicados en el registro
        mongoTemplate.indexOps("users")
                .createIndex(new Index().on("nick", Sort.Direction.ASC).unique());

        mongoTemplate.indexOps("users")
                .createIndex(new Index().on("email", Sort.Direction.ASC).unique());

        mongoTemplate.indexOps("users")
                .createIndex(new Index().on("temporaryHash", Sort.Direction.ASC));

        // Retos asignados — indices para filtrar por propietario y estado
        mongoTemplate.indexOps("user_challenges")
                .createIndex(new Index().on("ownerId", Sort.Direction.ASC));

        mongoTemplate.indexOps("user_challenges")
                .createIndex(new CompoundIndexDefinition(
                        new Document("ownerId", 1).append("state", 1)
                ));

        mongoTemplate.indexOps("user_challenges")
                .createIndex(new Index().on("transferredTo", Sort.Direction.ASC));

        mongoTemplate.indexOps("user_challenges")
                .createIndex(new Index().on("expiresAt", Sort.Direction.ASC));

        // Catalogo de retos — indice en active para cargar solo los disponibles
        mongoTemplate.indexOps("challenges")
                .createIndex(new Index().on("officialChallengeStartDate", Sort.Direction.ASC));

        mongoTemplate.indexOps("challenges")
                .createIndex(new Index().on("officialChallengeEndDate", Sort.Direction.ASC));

        mongoTemplate.indexOps("challenges")
                .createIndex(new Index().on("active", Sort.Direction.ASC));

        // Posts del feed — ordenados por fecha descendente para el cursor de paginacion
        mongoTemplate.indexOps("posts")
                .createIndex(new CompoundIndexDefinition(
                        new Document("userId", 1).append("createdAt", -1)
                ));

        mongoTemplate.indexOps("posts")
                .createIndex(new Index().on("createdAt", Sort.Direction.DESC));

        // Interacciones de posts — para saber la reaccion de cada usuario
        mongoTemplate.indexOps("post_interactions")
                .createIndex(new Index().on("postId", Sort.Direction.ASC));

        mongoTemplate.indexOps("post_interactions")
                .createIndex(new Index().on("userId", Sort.Direction.ASC));

        mongoTemplate.indexOps("post_interactions")
                .createIndex(new CompoundIndexDefinition(
                        new Document("postId", 1)
                                .append("userId", 1)
                                .append("type", 1)
                ));

        // Notificaciones — para cargar rapidamente las no leidas del usuario
        mongoTemplate.indexOps("notifications")
                .createIndex(new Index().on("userId", Sort.Direction.ASC));

        mongoTemplate.indexOps("notifications")
                .createIndex(new Index().on("createdAt", Sort.Direction.DESC));

        mongoTemplate.indexOps("notifications")
                .createIndex(new CompoundIndexDefinition(
                        new Document("userId", 1)
                                .append("read", 1)
                                .append("createdAt", -1)
                ));

        // Historial de puntos — para auditar los puntos ganados por cada usuario
        mongoTemplate.indexOps("points_history")
                .createIndex(new Index().on("userId", Sort.Direction.ASC));

        mongoTemplate.indexOps("points_history")
                .createIndex(new Index().on("referenceId", Sort.Direction.ASC));

        mongoTemplate.indexOps("points_history")
                .createIndex(new Index().on("date", Sort.Direction.DESC));
    }
}
