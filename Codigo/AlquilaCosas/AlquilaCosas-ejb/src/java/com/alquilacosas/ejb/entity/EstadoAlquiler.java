/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.ejb.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author damiancardozo
 */
@Entity
@Table(name = "ESTADO_ALQUILER")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EstadoAlquiler.findAll", query = "SELECT e FROM EstadoAlquiler e"),
    @NamedQuery(name = "EstadoAlquiler.findByEstadoAlquilerId", query = "SELECT e FROM EstadoAlquiler e WHERE e.estadoAlquilerId = :estadoAlquilerId"),
    @NamedQuery(name = "EstadoAlquiler.findByNombre", query = "SELECT e FROM EstadoAlquiler e WHERE e.nombre = :nombre")})
public class EstadoAlquiler implements Serializable {
    
    public enum NombreEstadoAlquiler {
        PEDIDO("Pedido"), 
        CONFIRMADO("Confirmado"), 
        ACTIVO("Activo"), 
        FINALIZADO("Finalizado"), 
        PEDIDO_CANCELADO("Pedido Cancelado"), 
        PEDIDO_RECHAZADO("Pedido Rechazado"), 
        CANCELADO_ALQUILADOR("Cancelado"), 
        CANCELADO("Cancelado");
        String label;

        private NombreEstadoAlquiler(String label) {
            this.label = label;
        }
        @Override
        public String toString() {
            return label;
        }
    }
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ESTADO_ALQUILER_ID")
    private Integer estadoAlquilerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "NOMBRE")
    private NombreEstadoAlquiler nombre;
    
    @Size(max = 150)
    @Column(name = "DESCRIPCION")
    private String descripcion;
    
    //@OneToMany(cascade = CascadeType.ALL, mappedBy = "estadoAlquilerFk")
    //private List<AlquilerXEstado> alquilerXEstadoList;

    public EstadoAlquiler() {
        //alquilerXEstadoList = new ArrayList<AlquilerXEstado>();
    }

    public EstadoAlquiler(Integer estadoAlquilerId) {
        this.estadoAlquilerId = estadoAlquilerId;
    }

    public EstadoAlquiler(Integer estadoAlquilerId, NombreEstadoAlquiler nombre) {
        this.estadoAlquilerId = estadoAlquilerId;
        this.nombre = nombre;
    }
    
//    public void agregarAlquilerXEstado( AlquilerXEstado axe ){
//        this.alquilerXEstadoList.add(axe);
//        axe.setEstadoAlquilerFk(this);
//    }
//    
//    public void removerAlquilerXEstado( AlquilerXEstado axe ){
//        this.alquilerXEstadoList.remove(axe);
//        axe.setEstadoAlquilerFk(null);
//    }

    public Integer getEstadoAlquilerId() {
        return estadoAlquilerId;
    }

    public void setEstadoAlquilerId(Integer estadoAlquilerId) {
        this.estadoAlquilerId = estadoAlquilerId;
    }

    public NombreEstadoAlquiler getNombre() {
        return nombre;
    }

    public void setNombre(NombreEstadoAlquiler nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

//    @XmlTransient
//    public List<AlquilerXEstado> getAlquilerXEstadoList() {
//        return alquilerXEstadoList;
//    }
//
//    public void setAlquilerXEstadoList(List<AlquilerXEstado> alquilerXEstadoList) {
//        this.alquilerXEstadoList = alquilerXEstadoList;
//    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (estadoAlquilerId != null ? estadoAlquilerId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EstadoAlquiler)) {
            return false;
        }
        EstadoAlquiler other = (EstadoAlquiler) object;
        if ((this.estadoAlquilerId == null && other.estadoAlquilerId != null) || (this.estadoAlquilerId != null && !this.estadoAlquilerId.equals(other.estadoAlquilerId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.alquilacosas.ejb.entity.EstadoAlquiler[ estadoAlquilerId=" + estadoAlquilerId + " ]";
    }
    
}
