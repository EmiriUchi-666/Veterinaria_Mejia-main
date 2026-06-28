package com.Veterinaria.Mejia.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

/**
 * M4 — Cache en memoria con Caffeine para consultas frecuentes del dashboard.
 * Evita queries repetidos a BD cada vez que se carga el panel principal.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager(
            "topProductos",     // Top productos más vendidos
            "metricasCRM",      // Dashboard CRM
            "stockCritico",     // Productos stock bajo
            "rucValidados",     // Resultados validación RUC
            "reporteDashboard"  // KPIs del dashboard
        );
        mgr.setCaffeine(Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES) // Refrescar cada 10 min
            .recordStats());
        return mgr;
    }
}
