/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.mbean;

import com.alquilacosas.common.AlquilaCosasException;
import com.alquilacosas.common.DomicilioFacade;
import com.alquilacosas.ejb.entity.Pais;
import com.alquilacosas.ejb.entity.Provincia;
import com.alquilacosas.ejb.session.UsuarioBeanLocal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author damiancardozo
 */
@ManagedBean(name = "usuario")
@ViewScoped
public class RegistrarUsuarioMBean implements Serializable {

    /** Creates a new instance of UsuarioMBean */
    public RegistrarUsuarioMBean() {
    }
    
    @EJB
    private UsuarioBeanLocal usuarioBean;
    private String username;
    private String password;
    private String password2;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;
    private String calle;
    private String depto;
    private String barrio;
    private String ciudad;
    private Integer numero;
    private Integer piso;
    private Date fechaNacimiento;
    private Date today;
    private List<SelectItem> provincias;
    private int provinciaSeleccionada;
    private List<SelectItem> paises;
    private int paisSeleccionado;
    private List<DomicilioFacade> domicilios;
    private DomicilioFacade domicilioSeleccionado;
    private boolean creado;
    
    @PostConstruct
    public void init() {
        paises = new ArrayList<SelectItem>();
        provincias = new ArrayList<SelectItem>();
        domicilios = new ArrayList<DomicilioFacade>();
        List<Pais> listaPais = usuarioBean.getPaises();
        for(Pais p: listaPais) {
            paises.add(new SelectItem(p.getPaisId(), p.getNombre()));
        }
        today = new Date();
    }
    
    public void agregarDomicilio() {
        DomicilioFacade dom = new DomicilioFacade();
        dom.setCalle(calle);
        dom.setNumero(numero);
        if(piso != null)
            dom.setPiso(piso);
        if(!depto.equals(""))
            dom.setDepto(depto);
        dom.setBarrio(barrio);
        dom.setCiudad(ciudad);
        domicilios.add(dom);
//        calle = "";
//        numero = null;
//        piso = null;
//        depto = "";
//        barrio = "";
//        ciudad = "";
    }
    
    public void borrarDomicilio() {
        domicilios.remove(domicilioSeleccionado);
    }
    
    public String crearUsuario() {   
        try {
            usuarioBean.registrarUsuario(username, password, nombre, apellido, domicilios,
                provinciaSeleccionada, fechaNacimiento, dni, telefono, email);
            return "usuarioCreado.xhtml?email=" + email;
        } catch (AlquilaCosasException e) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error al crear usuario", e.getMessage()));
            return null;
        }
    }    
    
    public void actualizarProvincias() {
        provincias.clear();
        List<Provincia> prov = usuarioBean.getProvincias(paisSeleccionado);
        for(Provincia p: prov) {
            provincias.add(new SelectItem(p.getProvinciaId(), p.getNombre()));
        }
    }
    
    public void prepararDomicilio() {
        
    }
    
    public void validarUsername(FacesContext context, UIComponent toValidate, Object value) {
        String user = (String) value;
        if(user.equals(""))
            return;
        if(user.length() < 6) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "El usuario debe tener al menos 6 caracteres", 
                    "El usuario debe tener al menos 6 caracteres");
            throw new ValidatorException(message);
        }
        boolean existe = usuarioBean.usernameExistente(user);
        if(existe) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "El usuario ya existe", 
                    "El usuario ya existe");
            throw new ValidatorException(message);
        }
    }
    
    public void validarPassword(FacesContext context, UIComponent toValidate, Object value) {
        String confirmarPassword = (String)value;
        if(!password.equals(confirmarPassword)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Las contraseñas no coinciden", "Las contraseñas no coinciden");
            throw new ValidatorException(message);
        }
    }
    
    
    /*
     * Getters & Setters
     */
    
    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getDepto() {
        return depto;
    }

    public void setDepto(String depto) {
        this.depto = depto;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public Integer getPiso() {
        return piso;
    }

    public void setPiso(Integer piso) {
        this.piso = piso;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<SelectItem> getPaises() {
        return paises;
    }

    public void setPaises(List<SelectItem> paises) {
        this.paises = paises;
    }

    public List<SelectItem> getProvincias() {
        return provincias;
    }

    public void setProvincias(List<SelectItem> provincias) {
        this.provincias = provincias;
    }

    public int getPaisSeleccionado() {
        return paisSeleccionado;
    }

    public void setPaisSeleccionado(int selectedPais) {
        this.paisSeleccionado = selectedPais;
    }

    public int getProvinciaSeleccionada() {
        return provinciaSeleccionada;
    }

    public void setProvinciaSeleccionada(int selectedProvincia) {
        this.provinciaSeleccionada = selectedProvincia;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }

    public boolean isCreado() {
        return creado;
    }

    public void setCreado(boolean creado) {
        this.creado = creado;
    }

    public List<DomicilioFacade> getDomicilios() {
        return domicilios;
    }

    public void setDomicilios(List<DomicilioFacade> domicilios) {
        this.domicilios = domicilios;
    }

    public DomicilioFacade getDomicilioSeleccionado() {
        return domicilioSeleccionado;
    }

    public void setDomicilioSeleccionado(DomicilioFacade domicilioSeleccionado) {
        this.domicilioSeleccionado = domicilioSeleccionado;
    }
    
    
}