package com.fred.minhasfinancas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fred.minhasfinancas.api.dto.TokenDTO;
import com.fred.minhasfinancas.api.dto.UsuarioDTO;
import com.fred.minhasfinancas.model.entity.Usuario;
import com.fred.minhasfinancas.service.JwtService;
import com.fred.minhasfinancas.service.LancamentoService;
import com.fred.minhasfinancas.service.UsuarioService;
import com.fred.minhasfinancas.service.exceptions.RegraNegocioException;

import lombok.RequiredArgsConstructor;

import com.fred.minhasfinancas.service.exceptions.ErroAutenticacao;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
//Substitui o código comentado com a inclusão do final
public class UsuarioResource {
	
	/*@GetMapping("/")
	public String helloworld() {
		return "Hello World";
	}*/
	
	/*private UsuarioService service;
	
	public UsuarioResource( UsuarioService service ) {
		this.service = service;
	}*/
	
	private final UsuarioService service;
	private final LancamentoService lancamentoService;
	private final JwtService jwtService;
	
	@PostMapping("/autenticar")
	public ResponseEntity<?> autenticar( @RequestBody UsuarioDTO dto ) {
		try {
			Usuario usuarioAutenticado = service.autenticar(dto.getEmail(), dto.getSenha());
			String token = jwtService.gerarToken(usuarioAutenticado);
			TokenDTO tokenDTO = new TokenDTO(usuarioAutenticado.getName(),token);
			return  ResponseEntity.ok(tokenDTO);
		}catch (ErroAutenticacao e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PostMapping
	//@RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
	public ResponseEntity salvar( @RequestBody UsuarioDTO dto ) {
		System.out.println("UsuarioDTO: Email" + dto.getEmail() + "Nome:" + dto.getNome());
		Usuario usuario = Usuario.builder()
				.name(dto.getNome())
				.email(dto.getEmail())
				.senha(dto.getSenha())
				.build();
	
		try {
			Usuario usuarioSalvo = service.salvarUsuario(usuario);
			System.out.println(ResponseEntity.ok(usuarioSalvo));
			System.out.println(usuarioSalvo);
			
			//return new ResponseEntity(usuarioSalvo, HttpStatus.OK);
			return ResponseEntity.ok(usuarioSalvo);
			
		}catch (RegraNegocioException e) {
			System.out.println(e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	
	}
	
	@GetMapping("{id}/saldo")
	public ResponseEntity obterSaldo( @PathVariable("id") Long id ) {
		Optional<Usuario> usuario = service.obterPorId(id);
		
		if(!usuario.isPresent()) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		
		BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
		return ResponseEntity.ok(saldo);
		
	}

}
