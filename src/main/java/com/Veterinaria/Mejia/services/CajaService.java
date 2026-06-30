package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.AperturaCierreCaja;
import com.Veterinaria.Mejia.models.MovimientoCaja;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.AperturaCierreCajaRepository;
import com.Veterinaria.Mejia.repository.MovimientoCajaRepository;

import lombok.RequiredArgsConstructor;

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
    public AperturaCierreCaja abrirCaja(Usuario usuario, BigDecimal montoInicial) {
        if (hayCajaAbierta()) {
            throw new IllegalStateException("Ya existe una caja abierta. No se puede abrir otra.");
        }
        AperturaCierreCaja nuevaCaja = AperturaCierreCaja.builder()
                .usuario(usuario)
                .fechaApertura(LocalDateTime.now())
                .montoInicial(montoInicial)
                .totalIngresos(BigDecimal.ZERO)
                .totalEgresos(BigDecimal.ZERO)
                .estado(AperturaCierreCaja.EstadoCaja.Abierta)
                .build();
        log.info("Abriendo caja para usuario {} con monto inicial S/ {}", usuario.getNombreUsuario(), montoInicial);
        return cajaRepo.save(nuevaCaja);
    }

    public List<AperturaCierreCaja> obtenerHistorial() {
        return cajaRepo.findTop10ByOrderByFechaAperturaDesc();
    }

    /**
     * Cierra la caja actualmente abierta. Calcula automáticamente el saldo final
     * basándose en los movimientos registrados.
     * @return La entidad de caja actualizada y cerrada.
     */
    @Transactional
    public AperturaCierreCaja cerrarCaja() {
        AperturaCierreCaja caja = getCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("No hay ninguna caja abierta para cerrar."));

        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado(AperturaCierreCaja.EstadoCaja.Cerrada);
        
        // El monto final ahora es el saldo calculado automáticamente.
        caja.setMontoFinal(caja.getSaldoActual());
        log.info("Cerrando caja #{}. Monto inicial: {}, Saldo final calculado: {}",
                caja.getId(), caja.getMontoInicial(), caja.getMontoFinal());

        return cajaRepo.save(caja);
    }

    @Transactional
    public void registrarIngreso(BigDecimal monto, String concepto, Usuario usuario) {
        AperturaCierreCaja caja = getCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("No hay caja abierta para registrar el ingreso."));

        caja.setTotalIngresos(caja.getTotalIngresos().add(monto));
        cajaRepo.save(caja);

        MovimientoCaja mov = MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.TipoMovimiento.INGRESO)
                .monto(monto)
                .concepto(concepto)
                .usuario(usuario)
                .build();
        movimientoRepo.save(mov);
    }

    @Transactional
    public void registrarEgreso(BigDecimal monto, String concepto, Usuario usuario) {
        AperturaCierreCaja caja = getCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("No hay caja abierta para registrar el egreso."));

        caja.setTotalEgresos(caja.getTotalEgresos().add(monto));
        cajaRepo.save(caja);

        MovimientoCaja mov = MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.TipoMovimiento.EGRESO)
                .monto(monto)
                .concepto(concepto)
                .usuario(usuario)
                .build();
        movimientoRepo.save(mov);
    }

    public List<MovimientoCaja> obtenerMovimientos(Integer cajaId) {
        return movimientoRepo.findByCajaIdOrderByFechaHoraDesc(cajaId);
    }
}