package com.fred.minhasfinancas.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fred.minhasfinancas.model.entity.Usuario;
import com.fred.minhasfinancas.model.repository.UsuarioRepository;
import com.fred.minhasfinancas.service.UsuarioService;
import com.fred.minhasfinancas.service.exceptions.ErroAutenticacao;
import com.fred.minhasfinancas.service.exceptions.RegraNegocioException;

import jakarta.transaction.Transactional;

@Service
public class UsuarioServiceimpl implements UsuarioService {
	
	private UsuarioRepository repository;
	
	public UsuarioServiceimpl(UsuarioRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Usuario autenticar(String email, String senha) {
		Optional<Usuario> usuario =  repository.findByEmail(email);
		
		if(!usuario.isPresent()){
			throw new ErroAutenticacao("Usuário Não Encontrado Para o Email Informado!");
		}
		
		if(!usuario.get().getSenha().equals(senha)) {
			throw new ErroAutenticacao("Senha Inválida!");
		}
	
		return usuario.get();
	}

	@Override
	public Usuario salvarUsuario(Usuario usuario) {
		
		validarEmail(usuario.getEmail());
		
		return repository.save(usuario);
	}

	@Override
	@Transactional
	public void validarEmail(String email) {
		// TODO Auto-generated method stub
		boolean existe = repository.existsByEmail(email);
		if(existe) {
			throw new RegraNegocioException("Já existe um usuário cadastrado com esse email.");
		}
	}

}
