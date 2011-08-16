/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.mbean;

import com.alquilacosas.common.UsuarioFacade;
import com.alquilacosas.ejb.session.UsuarioBeanLocal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author ignaciogiagante
 */
@ManagedBean (name = "administrarUsuarios")
@ViewScoped
public class AdministrarUsuariosMBean {
    
    @EJB
    private UsuarioBeanLocal usuarioBean;
    
    private List<UsuarioFacade> usuarios;
    private List<Integer> tipos;
    
    public AdministrarUsuariosMBean() {    
    }
    
    @PostConstruct
    public void init() {
        usuarios = usuarioBean.getUsuariosList();
        tipos = new ArrayList<Integer>();
    }
    
    public void asignarRol(RowEditEvent ev){
        
        try {
            
            UsuarioFacade uf = (UsuarioFacade)ev.getObject();
            if( uf.isUsuarioRol() && uf.isAdminRol() ){
                tipos.add(1);
                tipos.add(2);
            }else if( uf.isUsuarioRol() && !uf.isAdminRol()){
                tipos.add(1);
            }else if( uf.isAdminRol() && !uf.isAdminRol() ){
                tipos.add(2);          
            }
            
            usuarioBean.setRoles(uf, tipos);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
    }

    public List<UsuarioFacade> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<UsuarioFacade> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Integer> getTipos() {
        return tipos;
    }

    public void setTipos(List<Integer> tipos) {
        this.tipos = tipos;
    }
    
    
}