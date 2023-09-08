package com.fred.minhasfinancas.api;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fred.minhasfinancas.service.JwtService;
import com.fred.minhasfinancas.service.impl.SecurityUserDetailsService;

public class JwtTokenFilter extends OncePerRequestFilter {
		
	private JwtService jwtService;
	private SecurityUserDetailsService userDetailsService;

	public JwtTokenFilter(
			JwtService jwtService,
			SecurityUserDetailsService userDetailsService
			) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override //método que vai fazer receber a requisição
	protected void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain)
			throws ServletException, IOException {
			
		String authorization = request.getHeader("Authorization");
		System.out.println("Authorization " +" " + authorization);
		// Bearer Dunhan - DSV BUCeyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
		//anter era a Basic e agora vamos enviar a Autenticação Bearer
		
		if(authorization != null && authorization.startsWith("Bearer")) {
			String token = authorization.split(" ")[1];
			System.out.println("Token" +" "+ token);
			System.out.println("JwtService" +" "+ jwtService);
					//transforma em um Array , "Bearer" , "Dunhan - DSV BUC...."
					//na posição 0 Berarer e na posição 1 o token
			boolean isTokenValid = jwtService.isTokenValido(token);
		
			
			if(isTokenValid) {
				String login = jwtService.obterLoginUsuario(token);
				//pega o usuario q vem dentro do token e colocar na sessão do Sprig Security
				UserDetails usuarioAutenticado = userDetailsService.loadUserByUsername(login);
				//para carregra i isiario do Banco de Ddaos
				
				UsernamePasswordAuthenticationToken user = 
						new UsernamePasswordAuthenticationToken(
								usuarioAutenticado, null,usuarioAutenticado.getAuthorities());
				
				user.setDetails( new WebAuthenticationDetailsSource().buildDetails(request) );
				//criando uma autenticação par acolocar no Spring Security e exige que coloque dentro
				
				SecurityContextHolder.getContext().setAuthentication(user);
			}
		}
		filterChain.doFilter(request, response);
		//para dar continuidade a execução 
	}

}
