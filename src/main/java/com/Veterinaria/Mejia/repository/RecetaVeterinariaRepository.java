package com.Veterinaria.Mejia.repository;
import com.Veterinaria.Mejia.models.RecetaVeterinaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface RecetaVeterinariaRepository extends JpaRepository<RecetaVeterinaria, Integer> {
    Optional<RecetaVeterinaria> findByNumeroReceta(String numero);
    List<RecetaVeterinaria> findByPacienteIdOrderByFechaEmisionDesc(Integer pacienteId);
    @Query("SELECT MAX(r.numeroReceta) FROM RecetaVeterinaria r WHERE r.numeroReceta LIKE 'RV-%'")
    Optional<String> findMaxNumero();
}
