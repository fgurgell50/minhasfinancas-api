package com.fred.minhasfinancas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fred.minhasfinancas.api.JwtTokenFilter;
import com.fred.minhasfinancas.service.JwtService;
import com.fred.minhasfinancas.service.impl.SecurityUserDetailsService;


@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter  {

	@Autowired
	private SecurityUserDetailsService userDetailsService;
	
	@Autowired
	private JwtService jwtService;
	
	@Bean // registra dentro do container 
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		//gera um hash diferente sempre
		return encoder;
	}
	
	@Bean
	public JwtTokenFilter jwtTokenFilter() {
		return new JwtTokenFilter(jwtService, userDetailsService);
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		//AuthenticationManagerBuilder fazer a autenticação de usuários em memória
		//a senha precisa ser criptografada
		//String senhaCodificada = passwordEncoder().encode("qwe123");
		
		auth
			.userDetailsService(userDetailsService)
			.passwordEncoder(passwordEncoder());
			
		
		/*.inMemoryAuthentication()
			.withUser("usuario")
			.password(senhaCodificada)
			.roles("USER");
		*/
	}
	
	@Override //sobrescrevendo a classe Pai
	protected void configure(HttpSecurity http) throws Exception{
		http
			.csrf().disable()
			.authorizeRequests()
			.antMatchers(HttpMethod.POST, "/api/usuarios/autenticar").permitAll()
			.antMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
			//.antMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMIN")
			//uma forma de se colocar o acesso por grupo de usuários um perfil de ADMIN
			//.antMatchers(HttpMethod.POST, "/api/usuarios").hasAuthority("CADASTRA_USUARIO")
			///uma forma de se colocar o acesso por AUTHORITIES como por tela de cadastro
			//.antMatchers(HttpMethod.POST, "/api/usuarios").hasAnyRole("ADMIN", "RH")
			//uma forma de se colocar o acesso par amais de um grupo de usuários um perfil de ADMIN
			.anyRequest().authenticated()
		.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			//não salva o estado da sessão após uma sessão ter sido logada ou cookies
		//.and()
		//.httpBasic();
		.and()
			.addFilterBefore( jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class );
	}

}
