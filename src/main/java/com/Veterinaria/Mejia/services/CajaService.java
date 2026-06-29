package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    private final AperturaCierreCajaRepository cajaRepo;
    private final MovimientoCajaRepository movRepo;

    @Transactional
    public AperturaCierreCaja abrirCaja(Usuario usuario, BigDecimal montoInicial) {
        if (hayCajaAbierta()) throw new IllegalStateException("Ya existe una caja abierta.");
        AperturaCierreCaja c = new AperturaCierreCaja();
        c.setUsuario(usuario);
        c.setMontoInicial(montoInicial != null ? montoInicial : BigDecimal.ZERO);
        c.setFechaApertura(LocalDateTime.now());
        c.setEstado(AperturaCierreCaja.EstadoCaja.Abierta);
        c.setTotalIngresos(BigDecimal.ZERO);
        c.setTotalEgresos(BigDecimal.ZERO);
        return cajaRepo.save(c);
    }

    @Transactional
    public AperturaCierreCaja cerrarCaja(BigDecimal montoFinal, String observaciones) {
        AperturaCierreCaja c = getCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("No hay caja abierta."));
        c.setMontoFinal(montoFinal);
        c.setFechaCierre(LocalDateTime.now());
        c.setEstado(AperturaCierreCaja.EstadoCaja.Cerrada);
        c.setObservaciones(observaciones);
        return cajaRepo.save(c);
    }

    @Transactional
    public void registrarIngreso(BigDecimal monto, String concepto, Usuario usuario) {
        AperturaCierreCaja c = getCajaAbierta().orElseThrow(() -> new IllegalStateException("No hay caja abierta."));
        c.setTotalIngresos(c.getTotalIngresos().add(monto));
        cajaRepo.save(c);
        // Log individual
        MovimientoCaja mov = new MovimientoCaja();
        mov.setCaja(c); mov.setTipo(MovimientoCaja.TipoMovimiento.INGRESO);
        mov.setMonto(monto); mov.setConcepto(concepto); mov.setUsuario(usuario);
        movRepo.save(mov);
    }

    @Transactional
    public void registrarEgreso(BigDecimal monto, String concepto, Usuario usuario) {
        AperturaCierreCaja c = getCajaAbierta().orElseThrow(() -> new IllegalStateException("No hay caja abierta."));
        BigDecimal saldo = c.getSaldoActual();
        if (monto.compareTo(saldo) > 0) throw new IllegalArgumentException("El egreso (S/ " + monto + ") supera el saldo disponible (S/ " + String.format("%.2f", saldo) + ").");
        c.setTotalEgresos(c.getTotalEgresos().add(monto));
        cajaRepo.save(c);
        MovimientoCaja mov = new MovimientoCaja();
        mov.setCaja(c); mov.setTipo(MovimientoCaja.TipoMovimiento.EGRESO);
        mov.setMonto(monto); mov.setConcepto(concepto); mov.setUsuario(usuario);
        movRepo.save(mov);
    }

    public Optional<AperturaCierreCaja> getCajaAbierta() {
        return cajaRepo.findFirstByEstado(AperturaCierreCaja.EstadoCaja.Abierta);
    }
    public boolean hayCajaAbierta() { return getCajaAbierta().isPresent(); }
    public List<AperturaCierreCaja> obtenerHistorial() { return cajaRepo.findTop10ByOrderByFechaAperturaDesc(); }
    public List<MovimientoCaja> obtenerMovimientos(Integer cajaId) { return movRepo.findByCajaIdOrderByFechaHoraDesc(cajaId); }
}
