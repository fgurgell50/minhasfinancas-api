package com.fred.minhasfinancas.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fred.minhasfinancas.model.entity.Usuario;
import com.fred.minhasfinancas.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtServiceImpl implements JwtService{

	@Value("${jwt.expiracao}") //para injetar 
	private String expiracao;
	
	@Value("${jwt.chave-assinatura}") 
	private String chaveAssinatura;
	
	@Override
	public String gerarToken(Usuario usuario) {
		long exp = Long.valueOf(expiracao);
		LocalDateTime dataHoraExpiracao = LocalDateTime.now().plusMinutes(exp);
		//pega a hora taula e adiciona 30min
		Instant instant = dataHoraExpiracao.atZone(ZoneId.systemDefault()).toInstant();
		//zona do sistema operacional
		Date data = Date.from(instant);
		// java.util.Date
				
				String horaExpiracaoToken = dataHoraExpiracao.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
				String token = Jwts
							.builder()
							.setExpiration(data)
							.setSubject(usuario.getEmail())//identificador do usuario
							.claim("nome", usuario.getName())
							.claim("horaExpiracao", horaExpiracaoToken)
							.signWith(SignatureAlgorithm.HS512, chaveAssinatura )
							.compact();
		return token;
	}

	@Override
	public Claims obterClaims(String token) throws ExpiredJwtException {
		//Claims contem as informações do Token
		return Jwts
				.parser()
				.setSigningKey(chaveAssinatura)
				.parseClaimsJws(token)
				.getBody();
	}

	@Override
	public boolean isTokenValido(String token) {
		try {
			Claims claims = obterClaims(token);
			Date dataEx = claims.getExpiration();
			LocalDateTime dataExpiracao = dataEx.toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			
			boolean dataHoraAtualIsAfterDataExpiracao = LocalDateTime.now().isAfter(dataExpiracao);
			//verifica se é depois data de Expiração
			return !dataHoraAtualIsAfterDataExpiracao;
			
		}catch (ExpiredJwtException e) {
			return false;
		}
		
	}

	@Override
	public String obterLoginUsuario(String token) {
		Claims claims = obterClaims(token);
		return claims.getSubject();
	}

}
