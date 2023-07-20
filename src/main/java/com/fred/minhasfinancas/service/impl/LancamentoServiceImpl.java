package com.fred.minhasfinancas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.hibernate.type.TrueFalseConverter;
import org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;

import com.fred.minhasfinancas.model.entity.Lancamento;
import com.fred.minhasfinancas.model.enums.StatusLancamento;
import com.fred.minhasfinancas.model.repository.LancamentoRepository;
import com.fred.minhasfinancas.model.repository.UsuarioRepository;
import com.fred.minhasfinancas.service.LancamentoService;
import com.fred.minhasfinancas.service.exceptions.RegraNegocioException;

import jakarta.transaction.Transactional;

@Service
public class LancamentoServiceImpl implements LancamentoService {

	private LancamentoRepository repository;
	
	public LancamentoServiceImpl(LancamentoRepository repository) {
		//super();
		this.repository = repository;
	}	
	
	@Override
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		validar(lancamento);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public Lancamento atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		repository.delete(lancamento);
		
	}

	@Override
	@Transactional
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
		//Vai pega a instância do Objeto lancamentoFiltro com os dados preenchidos
		// vai passar para o Objeto Example
		Example example = Example.of( lancamentoFiltro, 
				ExampleMatcher
				.matching()
				.withIgnoreCase()
				.withStringMatcher(StringMatcher.CONTAINING) );
		//CONTAINING busca comr parte do Lancamento que foi decrito na colsuta
		//Exact busca pela decrição exata 
		//Ending encontrar a descrição TERMINE com o Lancamento que foi decrito na colsuta
		return repository.findAll(example);
	}

	@Override
	public void atializarStatus(Lancamento lancamento, StatusLancamento status) {
		lancamento.setStatus(status);
		atualizar(lancamento);
		
	}
	
	@Override
	@Transactional
	public void validar(Lancamento lancamento) {
		// TODO Auto-generated method stub
		//boolean existe = repository.existsByEmail(email);
		if(lancamento.getDescricao()==null || lancamento.getDescricao().trim().equals("") ) {
			throw new RegraNegocioException("Informe uma Descrição Válida");
		}
		if(lancamento.getMes()==null || lancamento.getMes() < 1 || lancamento.getMes() > 12  ) {
			throw new RegraNegocioException("Informe um Mês Válido");
		}
		
		if(lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
			throw new RegraNegocioException("Informe um Ano Válido");
		}
		if(lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
			throw new RegraNegocioException("Informe um Usuário");
		}
		if(lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
			throw new RegraNegocioException("Informe um Valor Válido");
		}
		if(lancamento.getTipo() == null ) {
			throw new RegraNegocioException("Informe um Tipo de Lançamento");
		}
		
	}

}
