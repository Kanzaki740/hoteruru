package com.example.moattravel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class WebSecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { //public外した
		http
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/css/**", "/images/**", "/js/**", "/storage/**", "/", "/signup/**","/houses","/houses/{id}").permitAll() //すべてのユーザーにアクセスを許可するURL
						.requestMatchers("/admin/**").hasRole("ADMIN") //管理者のみアクセスを許可するURL
						.anyRequest().authenticated() //上記以外のURLはログインが必要(会員または管理者のどちらでもOK)
				)
				.formLogin(form -> form
						.loginPage("/login") //ログインページのURL
						.loginProcessingUrl("/login") //ログインフォームの送信先URL
						.defaultSuccessUrl("/?loggedIn", true) //ログイン成功時のリダイレクト先URL trueを追加
						.failureUrl("/login?error") //ログイン失敗時のリダイレクト先URL
						.permitAll())
				.logout(logout -> logout
						.logoutSuccessUrl("/?loggedOut") //ログアウト時のリダイレクト先URL
						.permitAll());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() { //public外した
		return new BCryptPasswordEncoder();
	}
}
