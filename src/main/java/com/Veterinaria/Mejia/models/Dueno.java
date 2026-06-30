package com.Veterinaria.Mejia.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Dueño/Tutor de la mascota. Entidad independiente de Cliente (facturación).
 * Contiene datos de contacto completos del responsable del animal.
 */
@Entity
@Table(name = "duenos")
public class Dueno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, length = 15)
    private String dni;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento = "DNI"; // DNI, CE, Pasaporte

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean estado = true;

    @OneToMany(mappedBy = "dueno", fetch = FetchType.LAZY)
    private List<Paciente> mascotas;

    // Relación uno a uno con el perfil de facturación del cliente.
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", referencedColumnName = "id")
    private Cliente cliente;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public List<Paciente> getMascotas() { return mascotas; }
    public void setMascotas(List<Paciente> mascotas) { this.mascotas = mascotas; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}
