/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.dto;

import com.alquilacosas.ejb.entity.EstadoAlquiler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ignaciogiagante
 */
public class AlquilerDTO {
    
    private int idPublicacion;
    private int idUsuario;
    private int idAlquiler;
    private String titulo; //Publicacion
    private Date fechaInicio;
    private Date fechaFin;
    private String fechaDesde;
    private String fechaHasta;
    private Integer imagenId = -1;
    private int cantidad;
    private double monto;
    private boolean calificado;
    private EstadoAlquiler estadoAlquiler;
    
    public AlquilerDTO(){
        
    }
    
    public AlquilerDTO( int idPublicacion, String titulo, int idUsuario, int idAlquiler,
            Date fechaInicioAlquiler, Date fechaFinAlquiler, int cantidad,
            EstadoAlquiler estadoAlquiler, Integer imagenId){
        
        this.idPublicacion = idPublicacion;
        this.idUsuario = idUsuario;
        this.idAlquiler = idAlquiler;
        this.fechaInicio = fechaInicioAlquiler;
        this.fechaFin = fechaFinAlquiler;
        this.cantidad = cantidad;
        this.estadoAlquiler = estadoAlquiler;
        this.imagenId = imagenId;
        this.titulo = titulo;
        
        
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        fechaDesde = formatter.format(fechaInicio);
        fechaHasta = formatter.format(fechaFin);
    }

     public AlquilerDTO(int idPublicacion, int idUsuario, int idAlquiler, Date fechaInicioAlquiler, 
             Date fechaFinAlquiler, double monto, boolean calificado) {
          this.idPublicacion = idPublicacion;
          this.idUsuario = idUsuario;
          this.idAlquiler = idAlquiler;
          this.fechaInicio = fechaInicioAlquiler;
          this.fechaFin = fechaFinAlquiler;
          this.monto = monto;
          this.calificado = calificado;
     }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public EstadoAlquiler getEstadoAlquiler() {
        return estadoAlquiler;
    }

    public void setEstadoAlquiler(EstadoAlquiler estadoAlquiler) {
        this.estadoAlquiler = estadoAlquiler;
    }

    public Date getFechaFinAlquiler() {
        return fechaFin;
    }

    public void setFechaFinAlquiler(Date fechaFinAlquiler) {
        this.fechaFin = fechaFinAlquiler;
    }

    public Date getFechaInicioAlquiler() {
        return fechaInicio;
    }

    public void setFechaInicioAlquiler(Date fechaInicioAlquiler) {
        this.fechaInicio = fechaInicioAlquiler;
    }

    public int getIdAlquiler() {
        return idAlquiler;
    }

    public void setIdAlquiler(int idAlquiler) {
        this.idAlquiler = idAlquiler;
    }

    public int getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(int idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

     public double getMonto() {
          return monto;
     }

     public void setMonto(double monto) {
          this.monto = monto;
     }

     public boolean isCalificado() {
          return calificado;
     }

     public void setCalificado(boolean calificado) {
          this.calificado = calificado;
     }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getImagenId() {
        return imagenId;
    }

    public void setImagenId(Integer imagenId) {
        this.imagenId = imagenId;
    }
     
     
}