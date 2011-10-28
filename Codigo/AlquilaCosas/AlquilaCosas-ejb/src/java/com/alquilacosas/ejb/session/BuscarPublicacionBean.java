/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.ejb.session;

import com.alquilacosas.common.AlquilaCosasException;
import com.alquilacosas.common.Busqueda;
import com.alquilacosas.dto.CategoriaDTO;
import com.alquilacosas.dto.PeriodoDTO;
import com.alquilacosas.dto.PrecioDTO;
import com.alquilacosas.dto.PublicacionDTO;
import com.alquilacosas.ejb.entity.Categoria;
import com.alquilacosas.ejb.entity.Domicilio;
import com.alquilacosas.ejb.entity.ImagenPublicacion;
import com.alquilacosas.ejb.entity.Periodo;
import com.alquilacosas.ejb.entity.Publicacion;
import com.alquilacosas.facade.CategoriaFacade;
import com.alquilacosas.facade.ImagenPublicacionFacade;
import com.alquilacosas.facade.PeriodoFacade;
import com.alquilacosas.facade.PublicacionFacade;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author damiancardozo
 */
@Stateless
public class BuscarPublicacionBean implements BuscarPublicacionBeanLocal {
    @EJB
    private PrecioBeanLocal precioBean;
    @EJB
    private PublicacionFacade publicacionFacade;
    @EJB
    private CategoriaFacade categoriaFacade;
    @EJB
    private ImagenPublicacionFacade imagenFacade;
    @EJB
    private PeriodoFacade periodoFacade;

    @Override
    public Busqueda buscar(String palabra, Integer categoriaId,
            String ubicacion, Integer periodoId, Double precioDesde, Double precioHasta,
            int registros, int desde) {
        List<Publicacion> publicaciones = publicacionFacade.findPublicaciones(palabra, 
                categoriaId, ubicacion, periodoId, precioDesde, precioHasta, registros, desde);
        
        List<PublicacionDTO> pubFacadeList = new ArrayList<PublicacionDTO>();
        List<Categoria> categorias = new ArrayList<Categoria>();
        for (Publicacion p : publicaciones) {
            
            PublicacionDTO facade = crearPublicacionFacade(p);
            pubFacadeList.add(facade);
            
            Categoria c = p.getCategoriaFk();
            if (!categorias.contains(c)) {
                categorias.add(c);
            }
        }

        List<CategoriaDTO> catFacade = new ArrayList<CategoriaDTO>();
        for (Categoria c : categorias) {
            CategoriaDTO categoria = new CategoriaDTO(c.getCategoriaId(), c.getNombre());
            catFacade.add(categoria);
        }

        Busqueda busqueda = new Busqueda(pubFacadeList, catFacade);
        if(desde == 0) {
            Long totalRegistros = publicacionFacade.countBusquedaPublicaciones(palabra, 
                    categoriaId, ubicacion, periodoId, precioDesde, precioHasta);
            if (totalRegistros != null) {
                busqueda.setTotalRegistros(totalRegistros.intValue());
            }
        }
        return busqueda;
    }

    /*
     * Busca publicaciones por categoria
     */
    @Override
    public Busqueda buscarPublicacionesPorCategoria(int categoriaId, int registros, int desde)
            throws AlquilaCosasException {
        
        List<Integer> listaCategorias = new ArrayList<Integer>();
        Categoria cat = categoriaFacade.find(categoriaId);
        if(cat == null) {
            throw new AlquilaCosasException("Categoria inexistente");
        }
        listaCategorias.add(cat.getCategoriaId());
        List<Categoria> sublista = cat.getCategoriaList();
        List<Categoria> sublista2 = new ArrayList<Categoria>();
        for(Categoria c: sublista) {
            sublista2.add(c);
            listaCategorias.add(c.getCategoriaId());
        }
        List<Categoria> sublista3 = new ArrayList<Categoria>();
        for(Categoria c: sublista2) {
            sublista.add(c);
            listaCategorias.add(c.getCategoriaId());
        }
        for(Categoria c: sublista3)
            listaCategorias.add(c.getCategoriaId());
        
        
        List<Publicacion> publicaciones = publicacionFacade.findByCategoria(listaCategorias, registros, desde);
        List<PublicacionDTO> pubFacadeList = new ArrayList<PublicacionDTO>();
        for (Publicacion p : publicaciones) {
            
            PublicacionDTO facade = crearPublicacionFacade(p);
            pubFacadeList.add(facade);
        }

        List<CategoriaDTO> categorias = new ArrayList<CategoriaDTO>();
        Categoria categoria = categoriaFacade.find(categoriaId);
        categorias.add(new CategoriaDTO(categoriaId, categoria.getNombre()));

        Busqueda busqueda = new Busqueda(pubFacadeList, categorias);

        if (desde == 0) {
            Long totalRegistros = publicacionFacade.countByCategoria(listaCategorias);
            if (totalRegistros != null) {
                busqueda.setTotalRegistros(totalRegistros.intValue());
            }
        }

        return busqueda;
    }

    @Override
    public byte[] leerImagen(int id) {
        ImagenPublicacion imagen = imagenFacade.find(id);
        byte[] img = null;
        if (imagen != null) {
            img = imagen.getImagen();
        }
        return img;
    }

    private PublicacionDTO crearPublicacionFacade(Publicacion publicacion) {
        PublicacionDTO facade = new PublicacionDTO(publicacion.getPublicacionId(), 
                publicacion.getTitulo(), publicacion.getDescripcion(), publicacion.getFechaDesde(), 
                publicacion.getFechaHasta(), publicacion.getDestacada(),
                publicacion.getCantidad());
        List<Integer> imagenes = new ArrayList<Integer>();
        for (ImagenPublicacion ip : publicacion.getImagenPublicacionList()) {
            imagenes.add(ip.getImagenPublicacionId());
        }
        facade.setImagenIds(imagenes);
        Domicilio d = publicacion.getUsuarioFk().getDomicilioList().get(0);
        facade.setProvincia(d.getProvinciaFk().getNombre());
        facade.setCiudad(d.getCiudad());
        List<PrecioDTO> precios = precioBean.getPrecios(publicacion);
        facade.setPrecios(precios);
        
        return facade;
    }
    
    @Override
    public List<PeriodoDTO> getPeriodos() {
        List<Periodo> periodos = periodoFacade.findAll();
        List<PeriodoDTO> periodosDto = new ArrayList<PeriodoDTO>();
        for(Periodo p: periodos) {
            periodosDto.add(new PeriodoDTO(p.getPeriodoId(), p.getNombre()));
        }
        return periodosDto;
    }
}