package com.Veterinaria.Mejia.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.RequiredArgsConstructor;

/**
 * Reparación automática de integridad de datos al arrancar la aplicación.
 *
 * MOTIVO:
 * Algunas columnas booleanas de la tabla "productos" (permite_fraccionamiento,
 * es_alimento) se mapean en la entidad JPA. Si por datos antiguos/importados
 * esa columna contiene NULL en la base de datos y el campo Java fuera un tipo
 * primitivo, Hibernate no podría convertir NULL y lanzaría:
 *   "Null value was assigned to a property [...] of primitive type 'boolean'"
 * Este error rompe CUALQUIER consulta que traiga un Producto completo:
 * catálogo de ventas, almacén/inventario, historial de ventas, etc.
 *
 * NOTA: el campo "uso_clinico" fue retirado por completo de la entidad
 * Producto (ya no se usa en el sistema, era de un flujo de cotización
 * quirúrgica descartado). La columna puede seguir existiendo físicamente
 * en bases de datos antiguas, pero al no estar mapeada en la entidad,
 * Hibernate la ignora por completo y no hay forma de que vuelva a causar
 * este error.
 *
 * SOLUCIÓN:
 * Antes de que el resto del contexto de Spring (y por tanto Hibernate)
 * empiece a atender peticiones, este runner ejecuta una limpieza con JDBC
 * puro (sin pasar por JPA), normalizando cualquier NULL heredado a un
 * valor por defecto seguro (false / activo). Así la aplicación se
 * autorrepara sola en cada arranque, sin necesidad de ejecutar scripts
 * SQL manuales.
 *
 * Se ejecuta con prioridad muy alta (@Order) para correr antes que
 * cualquier otro runner que pudiera tocar la tabla productos.
 */
@Component
@Order(Integer.MIN_VALUE)
@RequiredArgsConstructor
public class IntegridadDatosRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IntegridadDatosRunner.class);

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        repararColumnasBooleanasDeProductos();
    }

    private void repararColumnasBooleanasDeProductos() {
        // Mapa de columna -> valor por defecto a aplicar cuando es NULL.
        // permite_fraccionamiento=0, es_alimento=0, estado=1 (activo por defecto).
        String[][] reparaciones = {
                {"permite_fraccionamiento", "0"},
                {"es_alimento", "0"},
                {"estado", "1"}
        };

        try (Connection conn = dataSource.getConnection()) {
            if (!existeTabla(conn, "productos")) {
                // La tabla aún no existe (primer arranque con BD vacía); nada que reparar.
                return;
            }

            int totalFilasReparadas = 0;
            try (Statement stmt = conn.createStatement()) {
                for (String[] reparacion : reparaciones) {
                    String columna = reparacion[0];
                    String valorPorDefecto = reparacion[1];

                    if (!existeColumna(conn, "productos", columna)) {
                        continue; // Columna no existe en este esquema; se omite con seguridad.
                    }

                    String sql = "UPDATE productos SET " + columna + " = " + valorPorDefecto
                            + " WHERE " + columna + " IS NULL";
                    int filasAfectadas = stmt.executeUpdate(sql);
                    if (filasAfectadas > 0) {
                        totalFilasReparadas += filasAfectadas;
                        log.warn("[INTEGRIDAD-DATOS] Se repararon {} fila(s) con '{}' NULL en la tabla productos (valor aplicado: {}).",
                                filasAfectadas, columna, valorPorDefecto);
                    }
                }
            }

            if (totalFilasReparadas == 0) {
                log.info("[INTEGRIDAD-DATOS] Verificación de productos completada: no se encontraron valores NULL pendientes.");
            }

        } catch (SQLException e) {
            // No se detiene el arranque de la aplicación por esto: se deja constancia
            // en el log para que el administrador lo revise, pero la app sigue iniciando.
            log.error("[INTEGRIDAD-DATOS] No se pudo verificar/reparar la tabla productos al arrancar: {}", e.getMessage());
        }
    }

    private boolean existeTabla(Connection conn, String tabla) throws SQLException {
        var rs = conn.getMetaData().getTables(conn.getCatalog(), null, tabla, new String[] {"TABLE"});
        return rs.next();
    }

    private boolean existeColumna(Connection conn, String tabla, String columna) throws SQLException {
        var rs = conn.getMetaData().getColumns(conn.getCatalog(), null, tabla, columna);
        return rs.next();
    }
}
