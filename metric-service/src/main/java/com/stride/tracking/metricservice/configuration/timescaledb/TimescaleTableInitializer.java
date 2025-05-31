package com.stride.tracking.metricservice.configuration.timescaledb;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimescaleTableInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final EntityManager entityManager;
    private boolean initialized = false;

    private void createHypertable(String tableName, String timeColumnName) {
        entityManager
                .createNativeQuery(String.format(
                        "SELECT create_hypertable('metric.%s','%s', if_not_exists => TRUE);",
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