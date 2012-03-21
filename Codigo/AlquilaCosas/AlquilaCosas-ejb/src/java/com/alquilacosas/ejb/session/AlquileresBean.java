/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alquilacosas.ejb.session;

import com.alquilacosas.common.AlquilaCosasException;
import com.alquilacosas.common.NotificacionEmail;
import com.alquilacosas.dto.AlquilerDTO;
import com.alquilacosas.dto.CalificacionDTO;
import com.alquilacosas.dto.PedidoCambioDTO;
import com.alquilacosas.ejb.entity.Alquiler;
import com.alquilacosas.ejb.entity.AlquilerXEstado;
import com.alquilacosas.ejb.entity.Calificacion;
import com.alquilacosas.ejb.entity.EstadoAlquiler;
import com.alquilacosas.ejb.entity.EstadoAlquiler.NombreEstadoAlquiler;
import com.alquilacosas.ejb.entity.EstadoPedidoCambio;
import com.alquilacosas.ejb.entity.EstadoPedidoCambio.NombreEstadoPedidoCambio;
import com.alquilacosas.ejb.entity.ImagenPublicacion;
import com.alquilacosas.ejb.entity.PedidoCambio;
import com.alquilacosas.ejb.entity.PedidoCambioXEstado;
import com.alquilacosas.ejb.entity.Periodo;
import com.alquilacosas.ejb.entity.Periodo.NombrePeriodo;
import com.alquilacosas.ejb.entity.Precio;
import com.alquilacosas.ejb.entity.Publicacion;
import com.alquilacosas.ejb.entity.Puntuacion;
import com.alquilacosas.ejb.entity.Usuario;
import com.alquilacosas.facade.AlquilerFacade;
import com.alquilacosas.facade.AlquilerXEstadoFacade;
import com.alquilacosas.facade.CalificacionFacade;
import com.alquilacosas.facade.EstadoAlquilerFacade;
import com.alquilacosas.facade.EstadoPedidoCambioFacade;
import com.alquilacosas.facade.PedidoCambioFacade;
import com.alquilacosas.facade.PedidoCambioXEstadoFacade;
import com.alquilacosas.facade.PeriodoFacade;
import com.alquilacosas.facade.PrecioFacade;
import com.alquilacosas.facade.PublicacionFacade;
import com.alquilacosas.facade.PuntuacionFacade;
import com.alquilacosas.facade.UsuarioFacade;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 *
 * @author damiancardozo
 */
@Stateless
public class AlquileresBean implements AlquileresBeanLocal {

    @Resource(name = "emailConnectionFactory")
    private ConnectionFactory connectionFactory;
    
    @Resource(name = "jms/notificacionEmailQueue")
    private Destination destination;
    @EJB
    private UsuarioFacade usuarioFacade;
    @EJB
    private AlquilerFacade alquilerFacade;
    @EJB
    private CalificacionFacade calificacionFacade;
    @EJB
    private PuntuacionFacade puntuacionFacade;
    @EJB
    private EstadoAlquilerFacade estadoFacade;
    @EJB
    private PrecioFacade precioFacade;
    @EJB
    private AlquilerXEstadoFacade axeFacade;
    @EJB
    private PedidoCambioFacade pedidoFacade;
    @EJB
    private PedidoCambioXEstadoFacade pcxeFacade;
    @EJB
    private EstadoPedidoCambioFacade estadoPedidoFacade;
    @EJB
    private PublicacionFacade publicacionFacade;
    @EJB
    private PeriodoFacade periodoFacade;
    @EJB
    private EstadoPedidoCambioFacade estadPedidoFacade;

    @Override
    public List<AlquilerDTO> getAlquileres(int usuarioId) {
        Usuario usuario = usuarioFacade.find(usuarioId);
        List<Alquiler> alquileres = alquilerFacade.getAlquileres(usuario);
        return convertirAlquileres(alquileres, usuario);
    }

    @Override
    public List<AlquilerDTO> getPedidos(int usuarioId) {
        Usuario usuario = usuarioFacade.find(usuarioId);
        List<Alquiler> alquileres = alquilerFacade.getPedidos(usuario);
        return convertirAlquileres(alquileres, usuario);
    }
    
    @Override
    public AlquilerDTO getAlquiler(int usuarioId, int alquilerId) {
        Usuario usuario = usuarioFacade.find(usuarioId);
        Alquiler alquiler = alquilerFacade.find(alquilerId);
        return convertirAlquiler(alquiler, usuario);
    }

    private List<AlquilerDTO> convertirAlquileres(List<Alquiler> alquileres, Usuario usuario) {
        List<AlquilerDTO> listaAlquileres = new ArrayList<AlquilerDTO>();
        for (Alquiler a : alquileres) {
            AlquilerDTO dto = convertirAlquiler(a, usuario);
            listaAlquileres.add(dto);
        }
        return listaAlquileres;
    }
    
    public AlquilerDTO convertirAlquiler(Alquiler a, Usuario usuario) {
        boolean tomado = false;
            if (a.getUsuarioFk().equals(usuario)) {
                tomado = true;
            }
            boolean calificado = calificacionFacade.isCalificacionExistente(usuario, a);
            Publicacion pub = a.getPublicacionFk();
            EstadoAlquiler estado = estadoFacade.getEstadoAlquiler(a);
            List<ImagenPublicacion> imagenes = pub.getImagenPublicacionList();
            int imagenId = -1;
            if (!imagenes.isEmpty()) {
                imagenId = imagenes.get(0).getImagenPublicacionId();
            }
            int usuarioId = 0;
            String username = "";
            if (tomado) {
                usuarioId = pub.getUsuarioFk().getUsuarioId();
                username = pub.getUsuarioFk().getLoginList().get(0).getUsername();
            } else {
                usuarioId = a.getUsuarioFk().getUsuarioId();
                username = a.getUsuarioFk().getLoginList().get(0).getUsername();
            }
            AlquilerDTO dto = new AlquilerDTO(pub.getPublicacionId(), usuarioId,
                    a.getAlquilerId(), imagenId, a.getFechaInicio(), a.getFechaFin(),
                    estado.getNombre(), pub.getTitulo(),
                    username, a.getCantidad(), a.getMonto(), calificado);
            dto.setTomado(tomado);
            // si el alquiler esta activo o confirmado; revisar si existe un pedido de cambio, y setearle su id al dto
            if ((estado.getNombre() == EstadoAlquiler.NombreEstadoAlquiler.CONFIRMADO
                    || estado.getNombre() == EstadoAlquiler.NombreEstadoAlquiler.ACTIVO) && pedidoFacade.hayPedidoEnviado(a)) {
                PedidoCambio pedido = pedidoFacade.getPedidoEnviado(a);
                int id = -1;
                if (pedido != null) {
                    id = pedido.getPedidoCambioId();
                }
                dto.setIdPedidoCambio(id);
            }
            return dto;
    }
    
    @Override
    public boolean cancelarAlquiler(int alquilerId) {
        Alquiler alquiler = alquilerFacade.find(alquilerId);
        AlquilerXEstado axe = axeFacade.findByAlquiler(alquilerId);
        axe.setFechaHasta(new Date());

        EstadoAlquiler estado = estadoFacade.findByNombre(EstadoAlquiler.NombreEstadoAlquiler.CANCELADO);
        AlquilerXEstado axeCancelado = new AlquilerXEstado();
        axeCancelado.setFechaDesde(new Date());
        axeCancelado.setEstadoAlquilerFk(estado);
        alquiler.agregarAlquilerXEstado(axeCancelado);
        alquilerFacade.edit(alquiler);
        
        // enviar email
        Usuario alquilador = alquiler.getUsuarioFk();
        String asunto = "Han cancelado uno de tus alquileres";
        String mensaje = "<html>Hola " + alquilador.getNombre() + ",<br/><br/>"
                + "El dueño del producto <b>" + alquiler.getPublicacionFk().getTitulo() 
                + "</b> ha cancelado tu alquiler. Ya puedes otorgar una calificacion por el mismo.<br/><br/> "
                + "Atentamente,<br/>"
                + "<b>AlquilaCosas</b>";
        NotificacionEmail email = new NotificacionEmail(alquilador.getEmail(), asunto, mensaje);
        enviarEmail(email);
        
        return true;
    }
    
    @Override
    public AlquilerDTO modificarAlquiler(AlquilerDTO dto, Date fechaFin, double monto) throws AlquilaCosasException {
        int alquilerId = dto.getIdAlquiler();
        Alquiler alquiler = alquilerFacade.find(alquilerId);
        if (alquiler == null) {
            throw new AlquilaCosasException("Alquiler no encontrado");
        }

        Publicacion publicacion = alquiler.getPublicacionFk();

        Calendar cal = Calendar.getInstance();
        Date fechaInicio = alquiler.getFechaInicio();
        cal.setTime(fechaInicio);

        long periodoAlquilerEnMillis = fechaFin.getTime() - fechaInicio.getTime();

        long minimaDuracion = calcularDuracion(publicacion.getMinPeriodoAlquilerFk().getNombre(),
                publicacion.getMinValor());

        if (periodoAlquilerEnMillis < minimaDuracion) {
            throw new AlquilaCosasException("El periodo seleccionado no alcanza "
                    + "el minimo periodo de alquiler seleccionado para ese producto:"
                    + publicacion.getMinValor() + " "
                    + publicacion.getMinPeriodoAlquilerFk().getNombre().toString());
        }
        if (publicacion.getMaxPeriodoAlquilerFk() != null && publicacion.getMaxValor() != null) {
            long maximaDuracion = calcularDuracion(publicacion.getMaxPeriodoAlquilerFk().getNombre(),
                    publicacion.getMaxValor());
            if (periodoAlquilerEnMillis > maximaDuracion) {
                throw new AlquilaCosasException("El periodo seleccionado supera "
                        + "el maximo periodo de alquiler seleccionado para ese producto."
                        + publicacion.getMaxValor() + " "
                        + publicacion.getMaxPeriodoAlquilerFk().getNombre().toString());
            }
        }

        alquiler.setFechaFin(fechaFin);
        alquiler.setMonto(monto);
        alquilerFacade.edit(alquiler);

        dto.setMonto(monto);
        dto.setFechaFin(fechaFin);
        
        // rechazar pedidos de alquiler que se solapen.
        revisarPedidos(fechaInicio, fechaFin, publicacion.getPublicacionId());
        
        // enviar email
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Usuario alquilador = alquiler.getUsuarioFk();
        String asunto = "Se ha modificado la duracion de uno de tus alquileres";
        String mensaje = "<html>Hola " + alquilador.getNombre() + ",<br/><br/>"
                + "El dueño del producto <b>" + publicacion.getTitulo() + "</b> ha modificado la duración "
                + "de tu alquiler. La nueva fecha de finalizacion es " + sdf.format(fechaFin) + ".<br/><br/>"
                + "Atentamente,<br/>"
                + "<b>AlquilaCosas</b>";
        NotificacionEmail email = new NotificacionEmail(alquilador.getEmail(), asunto, mensaje);
        enviarEmail(email);
        
        return dto;
    }
    
    @Override
    public void solicitarCambioAlquiler(int alquilerId, Date fechaFin, NombrePeriodo periodo, Integer duracion)
            throws AlquilaCosasException {

        Alquiler alquiler = alquilerFacade.find(alquilerId);
        Publicacion publicacion = alquiler.getPublicacionFk();

        
        Date fechaInicio = alquiler.getFechaInicio();
        if (fechaFin.before(new Date())) {
            throw new AlquilaCosasException("La fecha de fin del periodo seleccionado ya paso.");
        }

        long periodoAlquilerEnMillis = fechaFin.getTime() - fechaInicio.getTime();

        long minimaDuracion = calcularDuracion(publicacion.getMinPeriodoAlquilerFk().getNombre(),
                publicacion.getMinValor());

        if (periodoAlquilerEnMillis < minimaDuracion) {
            throw new AlquilaCosasException("El periodo seleccionado no alcanza "
                    + "el minimo periodo de alquiler seleccionado para ese producto:"
                    + publicacion.getMinValor() + " "
                    + publicacion.getMinPeriodoAlquilerFk().getNombre().toString());
        }
        if (publicacion.getMaxPeriodoAlquilerFk() != null && publicacion.getMaxValor() != null) {
            long maximaDuracion = calcularDuracion(publicacion.getMaxPeriodoAlquilerFk().getNombre(),
                    publicacion.getMaxValor());
            if (periodoAlquilerEnMillis > maximaDuracion) {
                throw new AlquilaCosasException("El periodo seleccionado supera "
                        + "el maximo periodo de alquiler seleccionado para ese producto."
                        + publicacion.getMaxValor() + " "
                        + publicacion.getMaxPeriodoAlquilerFk().getNombre().toString());
            }
        }
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(fechaInicio);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(fechaFin);
        
        Periodo period = periodoFacade.findByNombre(periodo);
        PedidoCambio pedido = new PedidoCambio();
        pedido.setDuracion(duracion);
        pedido.setPeriodoFk(period);

        EstadoPedidoCambio estado = estadPedidoFacade.findByNombre(NombreEstadoPedidoCambio.ENVIADO);
        PedidoCambioXEstado pcxe = new PedidoCambioXEstado(pedido, estado);
        pcxe.setFechaDesde(new Date());

        pedido.agregarPedidoCambioXEstado(pcxe);
        alquiler.agregarPedidoCambio(pedido);
        alquilerFacade.edit(alquiler);

        // enviar email
        Usuario dueno = publicacion.getUsuarioFk();
        String asunto = "Has recibido un pedido de cambio";
        String mensaje = "<html>Hola " + dueno.getNombre() + ",<br/><br/>"
                + "El alquilador del producto <b>" + publicacion.getTitulo() + "</b> ha "
                + " solicitado modificar la duracion del alquiler. <br/>"
                + "La nueva duracion propuesta es de " + duracion + " " + periodo.toString() + ".<br/>"
                + "Te recomendamos que antes de responder el pedido, contactes al alquilador "
                + "para ponerse de acuerdo con el pago. <br/><br/>"
                + "Atentamente,<br/>"
                + "<b>AlquilaCosas</b>";
        NotificacionEmail email = new NotificacionEmail(dueno.getEmail(), asunto, mensaje);
        enviarEmail(email);
    }
    
    @Override
    public PedidoCambioDTO getPedidoCambio(int pedidoCambioId) {
        PedidoCambio pedido = pedidoFacade.find(pedidoCambioId);
        return new PedidoCambioDTO(pedido.getPedidoCambioId(), pedido.getPeriodoFk().getNombre(), pedido.getDuracion());
    }

    @Override
    public AlquilerDTO responderPedidoCambio(int pedidoCambioId, AlquilerDTO alquilerDTO, Date fechaHasta, double monto, boolean aceptado) throws AlquilaCosasException {
        PedidoCambio pedido = pedidoFacade.find(pedidoCambioId);

        PedidoCambioXEstado pcxe = pcxeFacade.getPedidoCambioXEstadoActual(pedido);
        pcxe.setFechaHasta(new Date());

        PedidoCambioXEstado pcxeNuevo = new PedidoCambioXEstado();
        EstadoPedidoCambio estado = null;
        if (aceptado) {
            estado = estadoPedidoFacade.findByNombre(NombreEstadoPedidoCambio.ACEPTADO);
            alquilerDTO = modificarAlquiler(alquilerDTO, fechaHasta, monto);
        } else {
            estado = estadoPedidoFacade.findByNombre(NombreEstadoPedidoCambio.RECHAZADO);
        }
        pcxeNuevo.setEstadoPedidoCambioFk(estado);
        pcxeNuevo.setFechaDesde(new Date());
        pedido.agregarPedidoCambioXEstado(pcxeNuevo);
        
        pedidoFacade.edit(pedido);
        
        // enviar email
        Usuario alquilador = usuarioFacade.find(alquilerDTO.getIdUsuario());
        Publicacion pub = publicacionFacade.find(alquilerDTO.getIdPublicacion());
        String palabra = aceptado ? "aceptado" : "rechazado";
        String asunto = "Han " + palabra + " tu pedido de cambio";
        String mensaje = "<html>Hola " + alquilador.getNombre() + ",<br/><br/>"
                + "El dueño del producto <b>" + pub.getTitulo() + "</b> ha " + palabra
                + " tu pedido de alquiler. <br/><br/> "
                + "Atentamente,<br/>"
                + "<b>AlquilaCosas</b>";
        NotificacionEmail email = new NotificacionEmail(alquilador.getEmail(), asunto, mensaje);
        enviarEmail(email);
        
        return alquilerDTO;
    }
    
    @Override
    public void registrarCalificacion(Integer puntuacionId, Integer alquilerId, String comentario, Integer usuarioId, boolean tomado) {
        Alquiler alquiler = alquilerFacade.find(alquilerId);
        Usuario usuario = usuarioFacade.find(usuarioId);
        Usuario usuarioCalificado = null;
        if(tomado) {
            usuarioCalificado = alquiler.getPublicacionFk().getUsuarioFk();
        } else {
            usuarioCalificado = alquiler.getUsuarioFk();
        }        
        Puntuacion puntuacion = puntuacionFacade.find(puntuacionId);

        Calificacion nuevaCalificacion = new Calificacion();
        nuevaCalificacion.setUsuarioCalificadoFk(usuarioCalificado);
        nuevaCalificacion.setFechaCalificacion(new Date());
        nuevaCalificacion.setPuntuacionFk(puntuacion);
        nuevaCalificacion.setComentarioCalificador(comentario);
        nuevaCalificacion.setUsuarioCalificadorFk(usuario);

        alquiler.agregarCalificacion(nuevaCalificacion);
        alquilerFacade.edit(alquiler);

        // enviar email
        String asunto = "Has sido calificado por uno de tus alquileres";
        String mensaje = "<html>Hola " + usuarioCalificado.getNombre() + ",<br/><br/>"
                + "Has sido calificado por tu alquiler del producto " + alquiler.getPublicacionFk().getTitulo() + ". "
                + "Para ver tu calificacion ingresa a tu panel de usuario, y dirigete a 'Mis alquileres ofrecidos.'<br/><br/>"
                + "Atentamente,<br/>"
                + "<b>AlquilaCosas</b>";
        NotificacionEmail email = new NotificacionEmail(usuarioCalificado.getEmail(), asunto, mensaje);
        enviarEmail(email);
    }
    
    @Override
    public void registrarReplica(int calificacionId, String comentarioReplica, int usuarioId) {
        Calificacion calificacionAReplicar = calificacionFacade.find(calificacionId);
        calificacionAReplicar.setFechaReplica(new Date());
        calificacionAReplicar.setComentarioReplica(comentarioReplica);
        calificacionFacade.edit(calificacionAReplicar);
        // enviar email
        Usuario usuarioReplicado = calificacionAReplicar.getUsuarioCalificadorFk();
        String asunto = "Han replicado tu calificacion";
        String mensaje = "<html>Hola " + usuarioReplicado.getNombre() + ",<br/><br/>"
                + "Tu calificacion por el alquiler del producto "
                + calificacionAReplicar.getAlquilerFk().getPublicacionFk().getTitulo()
                + " ha sido replicada por la contraparte. Para ver la replica recibida"
                + " ingresa a tu panel de usuario, y dirigete a 'Mis alquileres ofrecidos.'<br/><br/>"
                + "Atentamente,<br/>"
                + "<b>AlquilaCosas</b>";
        NotificacionEmail email = new NotificacionEmail(usuarioReplicado.getEmail(), asunto, mensaje);
        enviarEmail(email);
    }
    
    @Override
    public CalificacionDTO getCalificacionOfrece(Integer alquilerId) throws AlquilaCosasException {
        CalificacionDTO calificacionDTO = null;
        Alquiler alquiler = alquilerFacade.find(alquilerId);
        Calificacion calificacion = calificacionFacade.getCalificacionPorAlquilerUsuarioOfrece(alquiler);
        if (calificacion != null) {
            calificacionDTO = new CalificacionDTO();
            calificacionDTO.setComentarioCalificacion(calificacion.getComentarioCalificador());
            calificacionDTO.setComentarioReplica(calificacion.getComentarioReplica() != null ? calificacion.getComentarioReplica() : null);
            calificacionDTO.setFechaCalificacion(calificacion.getFechaCalificacion());
            calificacionDTO.setFechaReplica(calificacion.getFechaReplica() != null ? calificacion.getFechaReplica() : null);
            calificacionDTO.setIdCalificacion(calificacion.getCalificacionId());
            calificacionDTO.setNombrePuntuacion(calificacion.getPuntuacionFk().getNombre());
            calificacionDTO.setNombreUsuarioCalificador(calificacion.getUsuarioCalificadorFk().getNombre() + " " + calificacion.getUsuarioCalificadorFk().getApellido());
            calificacionDTO.setIdUsuarioCalificador(calificacion.getUsuarioCalificadorFk().getUsuarioId());
            calificacionDTO.setNombreUsuarioReplica(calificacion.getUsuarioCalificadoFk() != null ? calificacion.getUsuarioCalificadoFk().getNombre() + " " + calificacion.getUsuarioCalificadoFk().getApellido() : null);
            calificacionDTO.setIdUsuarioReplicador(calificacion.getUsuarioCalificadoFk() != null ? calificacion.getUsuarioCalificadoFk().getUsuarioId() : null);
            calificacionDTO.setYaReplico(calificacion.getFechaReplica() != null);
        }
        return calificacionDTO;
    }

    @Override
    public CalificacionDTO getCalificacionToma(Integer alquilerId) throws AlquilaCosasException {
        CalificacionDTO calificacionDTO = null;
        Alquiler alquiler = alquilerFacade.find(alquilerId);
        Calificacion calificacion = calificacionFacade.getCalificacionPorAlquilerUsuarioToma(alquiler);
        if (calificacion != null) {
            calificacionDTO = new CalificacionDTO();
            calificacionDTO.setComentarioCalificacion(calificacion.getComentarioCalificador());
            calificacionDTO.setComentarioReplica(calificacion.getComentarioReplica() != null ? calificacion.getComentarioReplica() : null);
            calificacionDTO.setFechaCalificacion(calificacion.getFechaCalificacion());
            calificacionDTO.setFechaReplica(calificacion.getFechaReplica() != null ? calificacion.getFechaReplica() : null);
            calificacionDTO.setIdCalificacion(calificacion.getCalificacionId());
            calificacionDTO.setNombrePuntuacion(calificacion.getPuntuacionFk().getNombre());
            calificacionDTO.setNombreUsuarioCalificador(calificacion.getUsuarioCalificadorFk().getNombre() + " " + calificacion.getUsuarioCalificadorFk().getApellido());
            calificacionDTO.setIdUsuarioCalificador(calificacion.getUsuarioCalificadorFk().getUsuarioId());
            calificacionDTO.setNombreUsuarioReplica(calificacion.getUsuarioCalificadoFk() != null ? calificacion.getUsuarioCalificadoFk().getNombre() + " " + calificacion.getUsuarioCalificadoFk().getApellido() : null);
            calificacionDTO.setIdUsuarioReplicador(calificacion.getUsuarioCalificadoFk() != null ? calificacion.getUsuarioCalificadoFk().getUsuarioId() : null);
            calificacionDTO.setYaReplico(calificacion.getFechaReplica() != null);
        }
        return calificacionDTO;
    }
    
    @Override
    public List<Puntuacion> getPuntuaciones() {
        return puntuacionFacade.findAll();
    }
    
    @Override
    public List<Date> getFechasSinStock(int alquilerId) {
        Alquiler alquiler = alquilerFacade.find(alquilerId);
        Publicacion publicacion = alquiler.getPublicacionFk();
        List<Date> respuesta = new ArrayList<Date>();
        List<Alquiler> alquileres = alquilerFacade.getAlquileresByPublicacionFromToday(publicacion);
        alquileres.remove(alquiler);
        Iterator<Alquiler> itAlquiler = alquileres.iterator();

        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        int disponibles = publicacion.getCantidad();

        HashMap<String, Integer> dataCounter = new HashMap(60);//probablemente no existan pedidos mas haya de 60 dias desde hoy
        Calendar lastDate = Calendar.getInstance();
        lastDate.setTime(new Date());

        while (itAlquiler.hasNext()) {

            Alquiler temp = itAlquiler.next();
            Calendar date = Calendar.getInstance();
            date.setTime(temp.getFechaInicio());
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            Calendar fechaFin = Calendar.getInstance();
            fechaFin.setTime(temp.getFechaFin());
            if (fechaFin.get(Calendar.HOUR_OF_DAY) == 0 && fechaFin.get(Calendar.MINUTE) == 0
                    && fechaFin.get(Calendar.SECOND) == 0) { //fechaFin.add(Calendar.SECOND, 1); 
            //No me importan las fechas anteriores a hoy, no son seleccionables
                if (date.before(today)) {
                    date.setTime(today.getTime());
                }
            }
            //Me fijo si en los dias que dura el alquiler analizado hay disponibilidad de
            //productos para el pedido que estoy creando
            if (alquiler.getCantidad() <= disponibles - temp.getCantidad()) { //Si alcanzan los dias guardo en el hashmap el dia y la cantidad de
            //productos del alquiler, para ir acumulando esta cantidad para cada fecha
            //al final me fijo por cada fecha si alcanza, porque ya tengo todos los 
            //alquileres analizados
                while (date.before(fechaFin)) {
                    Integer acumulado = dataCounter.get(date.getTime().toString());
                    if (acumulado != null) {
                        dataCounter.put(date.getTime().toString(), new Integer(acumulado + temp.getCantidad()));
                    } else {
                        dataCounter.put(date.getTime().toString(), new Integer(temp.getCantidad()));
                    }
                    date.add(Calendar.DATE, 1);
                }
            } else {//si no alcanza, marco a todos los dias de este alquiler en el hasmap con el valor
            //de la disponibilidad total de la publicacion, lo cual significa que no va a alcanzar
            //ese dia para hacer el pedido ni siquiera de 1 producto
                while (date.before(fechaFin)) {
                    dataCounter.put(date.getTime().toString(), new Integer(disponibles));
                    date.add(Calendar.DATE, 1);
                }
            }

            if (date.after(lastDate)) {
                lastDate = date;
            }

        }
        //Reviso el hashmap y voy llenando la lista de respuesta con las fechas que 
        //no me alcanza el stock disponible
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);

        if (lastDate.get(Calendar.HOUR_OF_DAY) == 0 && lastDate.get(Calendar.MINUTE) == 0
                && lastDate.get(Calendar.SECOND) == 0) {
            lastDate.add(Calendar.SECOND, 1);
        }
        while (date.before(lastDate)) {
            Integer acumulado = dataCounter.get(date.getTime().toString());
            if (acumulado != null) {
                if (alquiler.getCantidad() > (disponibles - acumulado)) {
                    respuesta.add(date.getTime());
                }
            }
            date.add(Calendar.DATE, 1);
        }

        return respuesta;
    }
    
    private long calcularDuracion(NombrePeriodo nombrePeriodo, long periodo) {

        if (nombrePeriodo == NombrePeriodo.HORA) {
            periodo *= 60 * 60 * 1000;
        } else if (nombrePeriodo == NombrePeriodo.DIA) {
            periodo *= 24 * 60 * 60 * 1000;
        } else if (nombrePeriodo == NombrePeriodo.SEMANA) {
            periodo *= 7 * 24 * 60 * 60 * 1000;
        } else if (nombrePeriodo == NombrePeriodo.MES) {
            Date now = new Date();
            Calendar temp = Calendar.getInstance();
            temp.setTime(now);
            temp.add(Calendar.MONTH, (int) periodo);
            periodo = temp.getTimeInMillis() - now.getTime();
        }
        return periodo;
    }
    
    private void revisarPedidos(Date fechaDesde, Date fechaHasta, int publicacionId) throws AlquilaCosasException {

        List<Date> fechas = getIntervaloFechas(fechaDesde, fechaHasta);
        List<Alquiler> alquileresPedidos = alquilerFacade.getAlquilerPorPeriodo(fechaDesde, fechaHasta, -1, publicacionId);
        List<Alquiler> alquileresConfirmadosActivos = alquilerFacade.getAlquileresConfirmadosActivos(publicacionId);

        if (alquileresPedidos.isEmpty() || alquileresConfirmadosActivos.isEmpty()) {
            return;
        }

        int max = 0;
        int cant = 0;
        int resto = 0;
        for (Date fecha : fechas) {
            for (Alquiler a : alquileresConfirmadosActivos) {
                if (a.getFechaInicio().after(fecha) || a.getFechaInicio().equals(fecha)
                        || a.getFechaFin().before(fecha) || a.getFechaFin().equals(fecha)) {
                    cant += a.getCantidad();
                }
            }
            if (cant > max) {
                max = cant;
                cant = 0;
            }
        }

        Publicacion p = publicacionFacade.find(publicacionId);

        resto = p.getCantidad() - max;

        List<Alquiler> alquileresARechazar = new ArrayList<Alquiler>();

        for (Alquiler a : alquileresPedidos) {
            if (a.getCantidad() > resto) {
                alquileresARechazar.add(a);
            }
        }
        for (Alquiler a : alquileresARechazar) {
            AlquilerXEstado axeViejo = axeFacade.findByAlquiler(a.getAlquilerId());
            axeViejo.setFechaHasta(new Date());

            EstadoAlquiler ea = estadoFacade.findByNombre(NombreEstadoAlquiler.PEDIDO_RECHAZADO);

            AlquilerXEstado axe = new AlquilerXEstado(a, ea, new Date());
            a.agregarAlquilerXEstado(axe);
            alquilerFacade.edit(a);
            // enviar email
        }

    }

    private List<Date> getIntervaloFechas(Date fechaInicio, Date fechaFin) {

        List<Date> dates = new ArrayList<Date>();

        long interval = 24 * 1000 * 60 * 60; // 1 hour in millis
        long endTime = fechaFin.getTime();
        long curTime = fechaInicio.getTime();

        while (curTime <= endTime) {
            dates.add(new Date(curTime));
            curTime += interval;
        }

        return dates;
    }
    
    private void enviarEmail(NotificacionEmail email) {
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(true,
                    Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(destination);
            ObjectMessage message = session.createObjectMessage();
            message.setObject(email);
            producer.send(message);
            session.close();
            connection.close();
        } catch(Exception e) {
            
        }
    }

   
}
