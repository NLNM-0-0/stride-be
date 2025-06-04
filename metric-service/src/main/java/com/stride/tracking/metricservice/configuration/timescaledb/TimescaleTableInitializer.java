package com.stride.tracking.metricservice.configuration.timescaledb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TimescaleTableInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    private final EntityManager entityManager;
    private boolean initialized = false;

    private void createHypertable(String tableName, String timeColumnName) {
        entityManager
                .createNativeQuery(String.format(
                        "SELECT create_hypertable('%s.%s','%s', if_not_exists => TRUE);",
                        schema,
                        tableName,
                        timeColumnName))
                .getResultList();
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        if (initialized) {
            return;
        }

        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

        for (EntityType<?> entity : entities) {
            Class<?> javaType = entity.getJavaType();

            if (javaType.isAnnotationPresent(TimescaleTable.class)) {
                TimescaleTable annotation = javaType.getAnnotation(TimescaleTable.class);
                String tableName = annotation.tableName();
                String timeColumnName = annotation.timeColumnName();

                createHypertable(tableName, timeColumnName);
            }
        }

        initialized = true;
    }
}