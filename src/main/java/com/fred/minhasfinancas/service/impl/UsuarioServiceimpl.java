package com.fred.minhasfinancas.service.impl;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fred.minhasfinancas.model.entity.Usuario;
import com.fred.minhasfinancas.model.repository.UsuarioRepository;
import com.fred.minhasfinancas.service.UsuarioService;
import com.fred.minhasfinancas.service.exceptions.ErroAutenticacao;
import com.fred.minhasfinancas.service.exceptions.RegraNegocioException;



@Service
public class UsuarioServiceimpl implements UsuarioService {
	
	private UsuarioRepository repository;
	private PasswordEncoder encoder;
	
	public UsuarioServiceimpl(
			UsuarioRepository repository, 
			PasswordEncoder encoder) {
		super();
		this.repository = repository;
		this.encoder = encoder;
	}

	@Override
	public Usuario autenticar(String email, String senha) {
		Optional<Usuario> usuario =  repository.findByEmail(email);
		
		if(!usuario.isPresent()){
			throw new ErroAutenticacao("Usuário Não Encontrado Para o Email Informado!");
		}
		
		boolean senhasBatem = encoder.matches( senha, usuario.get().getSenha());
		//verifica a senha enviada com a senha criptografada do banco
		
		if( !senhasBatem ) {
			throw new ErroAutenticacao("Senha Inválida!");
		}
	
		return usuario.get();
	}

	@Override
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());
		criptografarSenha(usuario);
		return repository.save(usuario);
	}

	private void criptografarSenha(Usuario usuario) {
		String senha = usuario.getSenha();
		String senhaCripto = encoder.encode(senha);
		usuario.setSenha(senhaCripto);
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

	@Override
	public Optional<Usuario> obterPorId(Long id) {
		// TODO Auto-generated method stub
		return repository.findById(id);
	}

}
