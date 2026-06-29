package com.Veterinaria.Mejia.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.MedicamentoControlado;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.MedicamentoControladoRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicamentoControladoService {

    private final MedicamentoControladoRepository mcRepo;
    private final ProductoRepository productoRepo;

    @Transactional
    public MedicamentoControlado registrar(Integer productoId,
            MedicamentoControlado.CategoriaMedicamento categoria,
            String principioActivo, String registro, String laboratorio, String obs) {

        Producto prod = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        MedicamentoControlado mc = mcRepo.findByProductoId(productoId)
                .orElse(new MedicamentoControlado());
        mc.setProducto(prod);
        mc.setCategoria(categoria);
        mc.setPrincipioActivo(principioActivo);
        mc.setRegistroSanitario(registro);
        mc.setLaboratorioFabricante(laboratorio);
        mc.setRequiereReceta(categoria != MedicamentoControlado.CategoriaMedicamento.LIBRE);
        mc.setObservacionesDigemid(obs);
        return mcRepo.save(mc);
    }

    public List<MedicamentoControlado> listarTodos() { return mcRepo.findAll(); }
    public List<MedicamentoControlado> listarQueRequierenReceta() { return mcRepo.findByRequiereRecetaTrue(); }
    public MedicamentoControlado obtener(Integer id) { return mcRepo.findById(id).orElseThrow(); }

    @Transactional
    public void eliminar(Integer id) { mcRepo.deleteById(id); }
}
