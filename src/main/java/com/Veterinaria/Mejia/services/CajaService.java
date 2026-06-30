package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.AperturaCierreCaja;
import com.Veterinaria.Mejia.models.IngresoStock;
import com.Veterinaria.Mejia.models.MovimientoCaja;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.AperturaCierreCajaRepository;
import com.Veterinaria.Mejia.repository.MovimientoCajaRepository;

import lombok.RequiredArgsConstructor;

/**
 * FASE 7 — Rediseño del Control de Caja.
 *
 * Ya NO existen endpoints de "ingreso manual" ni "egreso manual": el saldo
 * de caja se alimenta SOLO de dos fuentes reales del sistema:
 *   - INGRESO: una venta del POS pagada en efectivo (VentaCreacionService).
 *   - EGRESO:  una compra a proveedor (ingreso de mercadería) pagada al
 *              contado (IngresoStockService).
 *
 * Cualquier otro método de pago (Yape, Plin, Tarjeta, Crédito a proveedor)
 * NUNCA toca el saldo de caja, porque ese dinero no pasa físicamente por el
 * cajón de efectivo.
 */
@Service
@RequiredArgsConstructor
public class CajaService {

    private static final Logger log = LoggerFactory.getLogger(CajaService.class);

    private final AperturaCierreCajaRepository cajaRepo;
    private final MovimientoCajaRepository movimientoRepo;

    public boolean hayCajaAbierta() {
        return cajaRepo.findFirstByEstado(AperturaCierreCaja.EstadoCaja.Abierta).isPresent();
    }

    public Optional<AperturaCierreCaja> getCajaAbierta() {
        return cajaRepo.findFirstByEstado(AperturaCierreCaja.EstadoCaja.Abierta);
    }

    @Transactional
    public AperturaCierreCaja abrirCaja(Usuario usuario, BigDecimal montoInicial, LocalTime horaCierreProgramada) {
        if (hayCajaAbierta()) {
            throw new IllegalStateException("Ya existe una caja abierta. No se puede abrir otra.");
        }
        AperturaCierreCaja nuevaCaja = AperturaCierreCaja.builder()
                .usuario(usuario)
                .fechaApertura(LocalDateTime.now())
                .montoInicial(montoInicial)
                .horaCierreProgramada(horaCierreProgramada)
                .totalIngresos(BigDecimal.ZERO)
                .totalEgresos(BigDecimal.ZERO)
                .estado(AperturaCierreCaja.EstadoCaja.Abierta)
                .build();
        log.info("Abriendo caja para usuario {} con monto inicial S/ {} | cierre programado: {}",
                usuario.getNombreUsuario(), montoInicial, horaCierreProgramada);
        return cajaRepo.save(nuevaCaja);
    }

    public List<AperturaCierreCaja> obtenerHistorial() {
        return cajaRepo.findTop10ByOrderByFechaAperturaDesc();
    }

    /**
     * Cierre manual (botón "Cerrar Caja"). El monto final SIEMPRE es el saldo
     * calculado a partir de movimientos reales — ya no se le pide "monto
     * contado" ni "observaciones" al cajero, porque eso invitaba a maquillar
     * diferencias en vez de auditarlas.
     */
    @Transactional
    public AperturaCierreCaja cerrarCaja() {
        return cerrarCajaInterno(false);
    }

    /**
     * Cierre disparado por el scheduler al llegar la hora programada.
     */
    @Transactional
    public AperturaCierreCaja cerrarCajaAutomaticamente() {
        return cerrarCajaInterno(true);
    }

    private AperturaCierreCaja cerrarCajaInterno(boolean automatico) {
        AperturaCierreCaja caja = getCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("No hay ninguna caja abierta para cerrar."));

        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado(AperturaCierreCaja.EstadoCaja.Cerrada);
        caja.setCierreAutomatico(automatico);
        caja.setMontoFinal(caja.getSaldoActual());

        log.info("Cerrando caja #{} ({}). Monto inicial: {}, Saldo final calculado: {}",
                caja.getId(), automatico ? "AUTOMÁTICO" : "MANUAL", caja.getMontoInicial(), caja.getMontoFinal());

        return cajaRepo.save(caja);
    }

    /**
     * Job que corre cada minuto y cierra automáticamente cualquier caja
     * abierta cuya hora_cierre_programada ya pasó. Sustituye al cierre manual
     * cuando el cajero configuró una hora de cierre al abrir la caja.
     */
    @Scheduled(cron = "0 * * * * *") // cada minuto
    @Transactional
    public void verificarCierresAutomaticosPendientes() {
        Optional<AperturaCierreCaja> abierta = getCajaAbierta();
        if (abierta.isEmpty()) return;

        AperturaCierreCaja caja = abierta.get();
        LocalTime horaProgramada = caja.getHoraCierreProgramada();
        if (horaProgramada == null) return;

        LocalTime ahora = LocalTime.now();
        if (!ahora.isBefore(horaProgramada)) {
            log.info("Hora de cierre automático alcanzada ({}) para la caja #{}. Cerrando...",
                    horaProgramada, caja.getId());
            cerrarCajaAutomaticamente();
        }
    }

    // =========================================================================
    // INGRESO: venta en efectivo desde el punto de venta
    // =========================================================================

    /**
     * Se invoca automáticamente desde VentaCreacionService justo después de
     * persistir una venta cuyo tipoPago == "Efectivo". El dinero SIEMPRE entra
     * (nunca se rechaza una venta por motivos de caja: vender de más no es un
     * problema, comprar de más sí lo es — ver registrarEgresoCompraEfectivo).
     */
    @Transactional
    public void registrarIngresoVentaEfectivo(Venta venta) {
        AperturaCierreCaja caja = getCajaAbierta().orElse(null);
        if (caja == null) {
            // No bloqueamos la venta por esto: solo lo dejamos en el log para
            // que el administrador revise por qué se vendió sin caja abierta.
            log.warn("Venta #{} en efectivo registrada SIN caja abierta. No se actualizó ningún saldo.",
                    venta.getId());
            return;
        }

        caja.setTotalIngresos(caja.getTotalIngresos().add(venta.getTotalVenta()));
        cajaRepo.save(caja);

        MovimientoCaja mov = MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.TipoMovimiento.INGRESO)
                .monto(venta.getTotalVenta())
                .concepto("Venta #" + venta.getId() + " (Terminal de Venta)")
                .usuario(venta.getUsuario())
                .venta(venta)
                .build();
        movimientoRepo.save(mov);
    }

    // =========================================================================
    // EGRESO: compra a proveedor (ingreso de mercadería) pagada al contado
    // =========================================================================


    /**
     * PASO 2 — llamar DESPUÉS de que IngresoStock ya tiene id (ya fue
     * guardado). Aquí sí se descuenta el saldo y se registra el movimiento.
     * Se asume que validarSaldoSuficienteParaCompra() ya se llamó antes,
     * dentro de la misma transacción, así que no se vuelve a rechazar aquí.
     */
    @Transactional
    public void registrarEgresoCompraEfectivo(BigDecimal monto, IngresoStock ingresoStock, Usuario usuario) {
        AperturaCierreCaja caja = getCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("No hay caja abierta."));

        caja.setTotalEgresos(caja.getTotalEgresos().add(monto));
        cajaRepo.save(caja);

        MovimientoCaja mov = MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.TipoMovimiento.EGRESO)
                .monto(monto)
                .concepto("Compra a proveedor (Ingreso de Mercadería #" + ingresoStock.getId() + ")")
                .usuario(usuario)
                .ingresoStock(ingresoStock)
                .build();
        movimientoRepo.save(mov);
    }

    public List<MovimientoCaja> obtenerMovimientos(Integer cajaId) {
        return movimientoRepo.findByCajaIdOrderByFechaHoraDesc(cajaId);
    }

    /**
     * PASO 1 — llamar ANTES de tocar stock o guardar nada. Lanza excepción y
     * detiene toda la operación si el saldo de caja no alcanza. No modifica
     * nada todavía (es de solo lectura), por eso es seguro llamarla primero.
     */
    @Transactional(readOnly = true)
    public void validarSaldoSuficienteParaCompra(BigDecimal monto) {
        AperturaCierreCaja caja = getCajaAbierta()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay caja abierta. No se puede registrar una compra al contado sin una caja activa."));

        if (caja.getSaldoActual().compareTo(monto) < 0) {
            throw new IllegalStateException(
                    "Saldo de caja insuficiente para esta compra en efectivo. Saldo actual: S/ "
                    + caja.getSaldoActual().setScale(2) + ", Monto requerido: S/ " + monto.setScale(2)
                    + ". Elige otro método de pago o reduce el monto de la compra.");
        }
    }
}