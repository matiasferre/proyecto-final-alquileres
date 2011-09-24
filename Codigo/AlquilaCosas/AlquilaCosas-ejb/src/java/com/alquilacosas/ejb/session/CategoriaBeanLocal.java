/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.ejb.session;

import com.alquilacosas.common.AlquilaCosasException;
import com.alquilacosas.dto.CategoriaDTO;
import com.alquilacosas.ejb.entity.Categoria;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author wilson
 */
@Local
public interface CategoriaBeanLocal {

     List<Categoria> getCategorias();

     Categoria getCategoriaPadre(int categoriaId);
     
     void registrarCategoria (String nombre, String descripcion, Categoria categoriaPadre) throws AlquilaCosasException;

     void borrarCategoria(int categoriaId);

     void modificarCategoria(Categoria categoria);
     
     List<CategoriaDTO> getCategoriaFacade();

     List<CategoriaDTO> getSubCategorias(int categoria);

    List<CategoriaDTO> getCategoriasPrincipal();
    
    List<Integer> getCategoriasPadre(int categoriaId);
}