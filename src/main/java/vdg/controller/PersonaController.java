package vdg.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vdg.model.domain.Persona;
import vdg.model.domain.RolDeUsuario;
import vdg.model.domain.Usuario;
import vdg.repository.PersonaRepository;
import vdg.repository.UsuarioRepository;

@RestController
@RequestMapping("/Persona")
@CrossOrigin
public class PersonaController {

	@Autowired
	private PersonaRepository personaRepo;
	
	@Autowired
	private UsuarioController usuarioController;

	@GetMapping
	public List<Persona> listar() {
		return personaRepo.findAll();
	}

	@PostMapping
	public Persona agregar(@RequestBody Persona persona) {
		return personaRepo.save(persona);
	}

	@DeleteMapping("/{id}")
	public void borrar(@PathVariable("id") int id) {
		Persona p = new Persona();
		p.setIdPersona(id);
		personaRepo.delete(p);
	}

	@GetMapping("/GetById/{id}")
	public Persona getById(@PathVariable("id") int id) {
		List<Persona> personas = personaRepo.findById(id);
		return personas.isEmpty() ? null : personas.get(0);
	}
	
	public Persona getByIdUsuario(int idUsuario) {
		List<Persona> personas = personaRepo.findByIdUsuario(idUsuario);
		return personas.isEmpty() ? null : personas.get(0);
	}

	@GetMapping("/GetDamnificadaByDni/{dni}")
	public Persona getDamnificadaByDni(@PathVariable("dni") String dni) {
		List<Persona> personas = personaRepo.findByDni(dni);
		if(personas.isEmpty())
			return null;
		Persona persona = personas.get(0);
		Usuario usuario = usuarioController.findByIdUsuario(persona.getIdUsuario());
		if(!usuario.getRolDeUsuario().equals(RolDeUsuario.DAMNIFICADA))
			return null;
		return persona;
	}
	
	@GetMapping("/GetVictimarioByDni/{dni}")
	public Persona getVictimarioByDni(@PathVariable("dni") String dni) {
		List<Persona> personas = personaRepo.findByDni(dni);
		if(personas.isEmpty())
			return null;
		Persona persona = personas.get(0);
		Usuario usuario = usuarioController.findByIdUsuario(persona.getIdUsuario());
		if(!usuario.getRolDeUsuario().equals(RolDeUsuario.VICTIMARIO))
			return null;
		return persona;
	}

	@PutMapping
	public Persona modificarPersona(Persona persona) {
		return personaRepo.save(persona);
	}
	
}
