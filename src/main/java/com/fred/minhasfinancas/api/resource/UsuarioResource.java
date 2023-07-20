package com.fred.minhasfinancas.api.resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fred.minhasfinancas.api.dto.UsuarioDTO;
import com.fred.minhasfinancas.model.entity.Usuario;
import com.fred.minhasfinancas.service.UsuarioService;
import com.fred.minhasfinancas.service.exceptions.RegraNegocioException;
import com.fred.minhasfinancas.service.exceptions.ErroAutenticacao;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioResource {
	
	/*@GetMapping("/")
	public String helloworld() {
		return "Hello World";
	}*/
	
	private UsuarioService service;
	
	public UsuarioResource( UsuarioService service ) {
		this.service = service;
	}
	
	@PostMapping("/autenticar")
	public ResponseEntity autenticar( @RequestBody UsuarioDTO dto ) {
		try {
			Usuario usuarioAutenticado = service.autenticar(dto.getEmail(), dto.getSenha());
			return  ResponseEntity.ok(usuarioAutenticado);
		}catch (ErroAutenticacao e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
		
	}
	
	@PostMapping("/salvar")
	public ResponseEntity salvar( @RequestBody UsuarioDTO dto ) {
		
		Usuario usuario = Usuario.builder().name(dto.getNome()).email(dto.getEmail()).senha(dto.getSenha()).build();
	
		try {
			Usuario usuarioSalvo = service.salvarUsuario(usuario);
			return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
		}catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	
	}

}
