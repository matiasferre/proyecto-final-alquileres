/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.ejb.session;

import com.alquilacosas.dto.UsuarioDTO;
import com.alquilacosas.ejb.entity.Login;
import com.alquilacosas.ejb.entity.Rol;
import com.alquilacosas.ejb.entity.Usuario;
import com.alquilacosas.facade.LoginFacade;
import com.alquilacosas.facade.RolFacade;
import com.alquilacosas.facade.UsuarioFacade;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author damiancardozo
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AdministrarUsuariosBean implements AdministrarUsuariosBeanLocal {

    @PersistenceContext(unitName = "AlquilaCosas-ejbPU")
    private EntityManager entityManager;
    
    @EJB
    private UsuarioFacade usuarioFacade;
    @EJB
    private LoginFacade loginFacade;
    @EJB
    private RolFacade rolFacade;

    @Override
    public List<UsuarioDTO> getUsuariosList() {
        
        List<UsuarioDTO> usuarios = new ArrayList<UsuarioDTO>();
        
        Query query = entityManager.createNativeQuery("select u.usuario_id, l.username, "
                + "u.email, u.nombre, u.apellido, uxe.fecha_desde, COUNT(r.rol_id) as NumRoles, r.nombre "
       + " from ROL r, LOGIN_X_ROL lxr, LOGIN l, USUARIO u, USUARIO_X_ESTADO uxe, ESTADO_USUARIO eu "
       + " where lxr.rol_fk = r.rol_id "
       + " and lxr.login_fk = l.login_id " 
       + " and l.usuario_fk = u.usuario_id "
       + " and u.usuario_id = uxe.usuario_fk "
       + " and uxe.estado_fk = eu.estado_usuario_id "
       + " and fecha_hasta IS NULL "
       + " group by u.usuario_id");
        
        List<Object[]> list = query.getResultList();
        
        for( Object[] object : list){
            
            Integer id = (Integer) object[0];
            String username = (String) object[1];
            String email = (String) object[2];
            String nombre = (String) object[3];
            String apellido = (String) object[4];
            Date fechaDeRegistro = (Date) object[5];
            Long numRoles = (Long) object[6];
            String tipoUsuario = (String) object[7];

            UsuarioDTO uf = new UsuarioDTO( id, username, email, nombre, 
                    apellido, fechaDeRegistro, numRoles, tipoUsuario); 
            
           usuarios.add(uf);     
        }
        
        return usuarios;
    }

    @Override
    public void setRoles(UsuarioDTO usuarioDTO, List<Integer> rolesVista) {
       
       Usuario usuario = usuarioFacade.find(usuarioDTO.getId());
       
       Login login = loginFacade.findByUsuario(usuario);
       List<Rol> rols = login.getRolList();
       List<Integer> rolIds = new ArrayList<Integer>();
       for(Rol r: rols) {
           rolIds.add(r.getRolId());
       }
       
       for(Integer i: rolesVista) {
           if(!rolIds.contains(i)) {
               Rol r = rolFacade.find(i);
               rols.add(r);               
           }
       }
       for(Integer i: rolIds) {
           if(!rolesVista.contains(i)) {
               for(int j = 0; j < rols.size(); j++) {
                   Rol r = rols.get(j);
                   if(r.getRolId() == i) {
                       rols.remove(r);
                       break;
                   }
               }
           }
       }
       login.setRolList(rols);
       loginFacade.edit(login); 
    }
}
