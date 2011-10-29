/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.mbean;

import com.alquilacosas.common.AlquilaCosasException;
import com.alquilacosas.ejb.entity.TipoPago.NombreTipoPago;
import com.alquilacosas.ejb.entity.TipoPublicidad.DuracionPublicidad;
import com.alquilacosas.ejb.entity.TipoPublicidad.UbicacionPublicidad;
import com.alquilacosas.ejb.session.PublicidadBeanLocal;
import com.alquilacosas.pagos.PaypalUtil;
import com.alquilacosas.validator.webutil.Navigation;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.json.JSONObject;

/**
 *
 * @author damiancardozo
 */
@ManagedBean(name = "publicidadBean")
@ViewScoped
public class RegistrarPublicidadMBean implements Serializable {

    @EJB
    private PublicidadBeanLocal publicidadBean;
    @ManagedProperty(value="#{login}")
    private ManejadorUsuarioMBean loginMBean;
    private String titulo, url, caption;
    private Double precio;
    private Date fechaDesde;
    private List<SelectItem> duraciones, ubicaciones;
    private UbicacionPublicidad ubicacionSeleccionada;
    private DuracionPublicidad duracionSeleccionada;
    private byte[] imagen;
    private List<Date> fechas;
    private String myJson;
    private Date fechaHasta;

    /** Creates a new instance of RegistrarPublicidadMBean */
    public RegistrarPublicidadMBean() {
    }

    @PostConstruct
    public void init() {
        
        fechas = new ArrayList<Date>();//publicidadBean.getFechasSinStock(UbicacionPublicidad.CARRUSEL); 
        this.createDictionary();
        
        duraciones = new ArrayList<SelectItem>();
        for(DuracionPublicidad d: DuracionPublicidad.values()) {
            duraciones.add(new SelectItem(d, d.toString()));
        }
        ubicaciones = new ArrayList<SelectItem>();
        for(UbicacionPublicidad u: UbicacionPublicidad.values()) {
            ubicaciones.add(new SelectItem(u, u.toString()));
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        imagen = event.getFile().getContents();
        FacesMessage msg = new FacesMessage(event.getFile().getFileName() + 
                "fue cargado correctamente", "");

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void registrarPublicidad() {
        
        if(imagen == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Debe cargar una imagen", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
       
        Integer pagoId = null;
        
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaDesde);
            DuracionPublicidad duracion = duracionSeleccionada;
            if(duracion == DuracionPublicidad.SEMANAL) {
                cal.add(Calendar.DAY_OF_YEAR, 7);
                this.setFechaHasta(cal.getTime());
            } else if(duracion == DuracionPublicidad.BISEMANAL) {
                cal.add(Calendar.DAY_OF_YEAR, 14);
                this.setFechaHasta(cal.getTime());
            } else if(duracion == DuracionPublicidad.MENSUAL) {
                cal.add(Calendar.MONTH, 1);
                this.setFechaHasta(cal.getTime());
            } else if(duracion == DuracionPublicidad.BIMENSUAL) {
                cal.add(Calendar.MONTH, 2);
                this.setFechaHasta(cal.getTime());
            }
            
            Iterator<Date> itFechasSinStock = fechas.iterator();
            boolean noStockFlag = false;
            Calendar fecha = Calendar.getInstance();
            //Recorro la lista de fechas sin stock fijandome si alguna cae
            //en el periodo seleccionado
            while(!noStockFlag && itFechasSinStock.hasNext()){
                fecha.setTime(itFechasSinStock.next());
                if(fechaDesde.before(fecha.getTime()) && fechaHasta.after(fecha.getTime()))//la fecha sin stock cae en el periodo
                    noStockFlag = true;
            }
            
            if(noStockFlag){
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Hay fechas sin stock en el periodo seleccionado", ""));
            }else{
                pagoId = publicidadBean.registrarPublicidad(loginMBean.getUsuarioId(), 
                        titulo, url, caption, ubicacionSeleccionada, 
                        duracionSeleccionada, imagen, fechaDesde, fechaHasta, precio, 
                        NombreTipoPago.PAYPAL);
            }
            
        } catch (AlquilaCosasException e) {
            Logger.getLogger(RegistrarPublicidadMBean.class).
                    error("registrarPublicidad(). Excepcion al invocar registrarPublicidad(): " 
                    + e + ": " + e.getMessage());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al registrar publicidad", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if(pagoId == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al registrar publicidad", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        String descripcion = "Publicar anuncio: " + titulo;
        String redirectUrl = PaypalUtil.setExpressCheckout(descripcion, Integer.toString(pagoId), null, precio.toString());
        if (redirectUrl != null) {
            Navigation.redirect(redirectUrl);
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al comunicarse con paypal", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    public void duracionCambio() {
        if(duracionSeleccionada != null && ubicacionSeleccionada != null) {
            precio = publicidadBean.getPrecio(duracionSeleccionada, ubicacionSeleccionada);
        } else {
            precio = null;
        }
    }

    public void ubicacionCambio() {
        if(duracionSeleccionada != null && ubicacionSeleccionada != null) {
            precio = publicidadBean.getPrecio(duracionSeleccionada, ubicacionSeleccionada);
            fechas = publicidadBean.getFechasSinStock(ubicacionSeleccionada); 
            this.createDictionary();
        } else {
            precio = null;
        }
    }
    
    /*
     * Getters & Setters
     */
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public DuracionPublicidad getDuracionSeleccionada() {
        return duracionSeleccionada;
    }

    public void setDuracionSeleccionada(DuracionPublicidad duracionSeleccionada) {
        this.duracionSeleccionada = duracionSeleccionada;
    }

    public List<SelectItem> getDuraciones() {
        return duraciones;
    }

    public void setDuraciones(List<SelectItem> duraciones) {
        this.duraciones = duraciones;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public UbicacionPublicidad getUbicacionSeleccionada() {
        return ubicacionSeleccionada;
    }

    public void setUbicacionSeleccionada(UbicacionPublicidad ubicacionSeleccionada) {
        this.ubicacionSeleccionada = ubicacionSeleccionada;
    }

    public List<SelectItem> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<SelectItem> ubicaciones) {
        this.ubicaciones = ubicaciones;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ManejadorUsuarioMBean getLoginMBean() {
        return loginMBean;
    }

    public void setLoginMBean(ManejadorUsuarioMBean loginMBean) {
        this.loginMBean = loginMBean;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechas() {
        return myJson;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }
    
    private void createDictionary() {
        try {      
            int month = -1;
            JSONObject yearJson = new JSONObject();
            JSONObject monthJson = new JSONObject();
            JSONObject dayJson = null;
            Calendar cal = Calendar.getInstance();
            for(Date d: fechas) {
                cal.setTime(d);
                if(cal.get(Calendar.MONTH) + 1 != month) {
                    if(dayJson != null) {
                        monthJson.putOpt(Integer.toString(month), dayJson);
                    }
                    dayJson = new JSONObject();
                    month = cal.get(Calendar.MONTH) + 1;
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    dayJson.putOpt(Integer.toString(day), true);
                }
                else {
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    dayJson.putOpt(Integer.toString(day), true);
                }
            }
            if(dayJson != null) {
                monthJson.putOpt(Integer.toString(month), dayJson);
            }
            int y = cal.get(Calendar.YEAR);
            yearJson.putOpt(Integer.toString(y), monthJson);
            myJson = yearJson.toString();
        } catch (Exception e) {
            //Logger.getLogger(this).error("Exception creating JSON dictionary: " + e);
        }
    }
}
