package com.Veterinaria.Mejia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.NotaCredito;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.NotaCreditoRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaAnulacionService {

    private static final Logger log = LoggerFactory.getLogger(VentaAnulacionService.class);

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final VentaConsultaService ventaConsultaService;

    @Transactional(rollbackFor = Exception.class)
    public void anularVenta(Integer id) {
        Venta venta = ventaConsultaService.buscarPorId(id);

        if (!venta.getEstado()) {
            throw new RuntimeException("La venta #" + id + " ya se encuentra anulada.");
        }

        venta.setEstado(false);
        ventaRepository.save(venta);

        NotaCredito notaCredito = new NotaCredito();
        // ... (logic to build the credit note)
        notaCreditoRepository.save(notaCredito);

        venta.getDetallesVentas().forEach(detalle -> {
            if (detalle.getProducto() != null) {
                Producto producto = productoRepository.findById(detalle.getProducto().getId()).orElseThrow();
                producto.setStockAbierto(producto.getStockAbierto().add(detalle.getCantidad()));
                productoRepository.save(producto);
            }
        });

        log.info("[ANULACIÓN] Venta #{} anulada. Generada Nota de Crédito {} y stock devuelto.",
                id, notaCredito.getSerie() + "-" + notaCredito.getCorrelativo());
    }
}