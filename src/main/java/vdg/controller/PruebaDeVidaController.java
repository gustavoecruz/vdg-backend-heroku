package vdg.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vdg.model.domain.EstadoNotificacion;
import vdg.model.domain.EstadoPruebaDeVida;
import vdg.model.domain.Incidencia;
import vdg.model.domain.Notificacion;
import vdg.model.domain.Persona;
import vdg.model.domain.PruebaDeVida;
import vdg.model.domain.TipoIncidencia;
import vdg.model.domain.Usuario;
import vdg.repository.NotificacionRepository;
import vdg.repository.PruebaDeVidaRepository;

@RestController
@RequestMapping("/PruebaDeVida")
@CrossOrigin
public class PruebaDeVidaController {

	@Autowired
	private PruebaDeVidaRepository pruebaDeVidaRepo;
	
	@Autowired
	private NotificacionRepository notificacionRepo;

	@Autowired
	private IncidenciaController incidenciaController;
	
	@Autowired
	private PersonaController personaController;
	
	@Autowired
	private UsuarioController usuarioController;
	
	@GetMapping("/{idPersona}")
	public List<PruebaDeVida> getPruebasDeVidaPersona(@PathVariable("idPersona") int idPersona){
		return pruebaDeVidaRepo.findByIdPersonaRestriccionOrderByFechaDesc(idPersona);
	}
	
	@GetMapping("/getByMail/{email}")
	public List<PruebaDeVida> getPruebasDeVidaApp(@PathVariable("email") String email){
		Usuario u = usuarioController.findByEmail(email);
		Persona p = personaController.getByIdUsuario(u.getIdUsuario());
		return pruebaDeVidaRepo.findByIdPersonaRestriccionAndEstadoOrderByFechaDesc(p.getIdPersona(), EstadoPruebaDeVida.Pendiente);
	}
	
	@PostMapping
	public PruebaDeVida agregar(@RequestBody PruebaDeVida pruebaDeVida) {
		// CREO EL TIMESTAMP
		Date ahora = new Date();
		ahora.setTime(ahora.getTime());
		Timestamp ahoraStamp = new Timestamp(ahora.getTime());
		pruebaDeVida.setFecha(ahoraStamp);
		pruebaDeVida.setEstado(EstadoPruebaDeVida.Pendiente);
		generarNotificacionVictimario(pruebaDeVida.getDescripcion(), ahoraStamp, pruebaDeVida.getIdPersonaRestriccion());
		
		return pruebaDeVidaRepo.save(pruebaDeVida);
	}

	@PutMapping("/{idPruebaDeVida}")
	public PruebaDeVida modificar(@RequestBody PruebaDeVida pruebaDeVida, @PathVariable("idPruebaDeVida") int idPruebaDeVida) {
		
		if(pruebaDeVida.getEstado().equals(EstadoPruebaDeVida.Rechazada))
			generarIncidenciaPruebaDeVida(pruebaDeVida);
		
		pruebaDeVida.setIdPruebaDeVida(idPruebaDeVida);
		return pruebaDeVidaRepo.save(pruebaDeVida);
	}

	private void generarIncidenciaPruebaDeVida(PruebaDeVida pruebaDeVida) {
		Incidencia incidencia = new Incidencia();
		
		//GENERO LA FECHA DE INCIDENCIA
		Date ahora = new Date();
		ahora.setTime(ahora.getTime());
		Timestamp ahoraStamp = new Timestamp(ahora.getTime());
		//TRAIGO LA PERSONA DE LA PRUEBA DE VIDA
		Persona persona = personaController.getById(pruebaDeVida.getIdPersonaRestriccion());
		
		incidencia.setTopico(TipoIncidencia.PruebaDeVidaFallida);
		incidencia.setIdRestriccion(pruebaDeVida.getIdRestriccion());
		incidencia.setFecha(ahoraStamp);
		incidencia.setDescripcion("Fallo la prueba de vida de: " + persona.getApellido() + ", " +
		persona.getNombre() + ". Descripción de prueba de vida: " + pruebaDeVida.getDescripcion() +
		". Fecha de petición: " + pruebaDeVida.getFecha());
		
		incidenciaController.agregar(incidencia);
	}
	
	private void generarNotificacionVictimario(String peticion, Timestamp fecha, int idPersona) {
		Persona persona = personaController.getById(idPersona);

		Notificacion notificacion = new Notificacion();
		notificacion.setEstado(EstadoNotificacion.NoVista);
		notificacion.setAsunto("Nueva prueba de Vida");
		notificacion.setDescripcion(peticion);
		notificacion.setFecha(fecha);
		notificacion.setIdUsuario(persona.getIdUsuario());
		
		notificacionRepo.save(notificacion);
	}

}
