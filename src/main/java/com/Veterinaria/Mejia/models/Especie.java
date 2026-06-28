package com.Veterinaria.Mejia.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "especies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Especie {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "nombre_especie", nullable = false, length = 50)
    private String nombreEspecie; // "Canino", "Felino", "Bovino", "General"
    
    // Relación actualizada: ahora las especies categorizan a los productos, ya no a las mascotas
    @OneToMany(mappedBy = "especie", fetch = FetchType.LAZY)
    private List<Producto> productos;
}