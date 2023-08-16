package com.fred.minhasfinancas.api.resource;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fred.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.fred.minhasfinancas.api.dto.LancamentoDTO;
import com.fred.minhasfinancas.model.entity.Lancamento;
import com.fred.minhasfinancas.model.entity.Usuario;
import com.fred.minhasfinancas.model.enums.StatusLancamento;
import com.fred.minhasfinancas.model.enums.TipoLancamento;
import com.fred.minhasfinancas.service.LancamentoService;
import com.fred.minhasfinancas.service.UsuarioService;
import com.fred.minhasfinancas.service.exceptions.RegraNegocioException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor 

//Substitui o código comentado com a inclusão do final
public class LancamentoResource{
	
	//private LancamentoService service;
	//private UsuarioService usuarioService;
	
	/*public LancamentoResource( LancamentoService service, UsuarioService usuarioService ) {
		this.service = service;
		this.usuarioService = usuarioService;
	}*/
	
	private final LancamentoService service;
	private final UsuarioService usuarioService;	
	
	@GetMapping
	public ResponseEntity buscar(
			@RequestParam( value = "descricao" , required = false) String descricao,
			@RequestParam( value = "mes" , required = false) Integer mes,
			@RequestParam( value = "ano" , required = false) Integer ano,
			@RequestParam( "usuario" ) Long idUsuario
			
			//@RequestParam java.util.Map<String, String> params
			//Poderia ser dessa forma tb para receber os parametros
			// porém todos os parametros são opcionais
			
			) {
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		
		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		if(!usuario.isPresent()) {
		return ResponseEntity.badRequest()
				.body("Não foi poível realizar a consulta. Usuário não encontrado");
		}else {
			lancamentoFiltro.setUsuario(usuario.get());
		}
		
		List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);	
		return ResponseEntity.ok(lancamentos);
	}
	
	@GetMapping("{id}")
	public ResponseEntity obterLancamento( @PathVariable("id") Long id) {
		return service.obterPorId(id)
				.map( lancamento -> new ResponseEntity(converter(lancamento), HttpStatus.OK))
				.orElseGet( () -> new ResponseEntity(HttpStatus.NOT_FOUND));
	}
	
	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
		try {
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			return new ResponseEntity(entidade,HttpStatus.CREATED);		
		}catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PutMapping("{id}")
	public ResponseEntity atualizar( @PathVariable("id") Long id, @RequestBody LancamentoDTO dto ) {
			return service.obterPorId(id).map( entity -> {
			try {
				Lancamento lancamento = converter(dto);
				lancamento.setId(entity.getId());
				service.atualizar(lancamento);
				return ResponseEntity.ok(lancamento);			
			}catch (RegraNegocioException e) {			
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> 
		new ResponseEntity("Lancamento não encontrado ma base de dados", HttpStatus.BAD_REQUEST));
	}
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus( @PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto ) {
		
		return service.obterPorId(id).map( entity -> { 
			
			StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());
			
			if(statusSelecionado== null) {
				return ResponseEntity.badRequest()
						.body("Não foi possível atulizar o Status, envie um Status Válido");
			}
			try {
				entity.setStatus(statusSelecionado);
				service.atualizar(entity);
				return ResponseEntity.ok(entity);			
			}catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}

		}).orElseGet( () -> 
		new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST) );
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id) {
		return service.obterPorId(id).map( entidade -> {
			try {
				service.deletar(entidade);
				//return ResponseEntity.ok(entidade);	
				return new ResponseEntity(HttpStatus.NO_CONTENT);
			}catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> 
		new ResponseEntity("Lancamento não encontrado ma base de dados", HttpStatus.BAD_REQUEST));
	}
			
	private LancamentoDTO converter(Lancamento lancamento) {
		return LancamentoDTO.builder()
				.Id(lancamento.getId())
				.descricao(lancamento.getDescricao())
				.valor(lancamento.getValor())
				.mes(lancamento.getMes())
				.ano(lancamento.getAno())
				.status(lancamento.getStatus().name())
				.tipo(lancamento.getTipo().name())
				.usuario(lancamento.getUsuario().getId())
				.build();
	}
	
	private Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		
		Usuario usuario = usuarioService
				.obterPorId(dto.getUsuario())
				.orElseThrow(() -> new RegraNegocioException("Usuário não encontrado para Id informado"));
		
		lancamento.setUsuario(usuario);
		
		if(dto.getTipo() != null) {
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));	
		}
		
		if(dto.getStatus() != null) {
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));			
		}
		return lancamento;
	}
}
