package com.Veterinaria.Mejia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Públicos
                .requestMatchers(
                    "/login", "/recuperar-password/**",
                    "/css/**", "/js/**", "/images/**",
                    "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Solo Administrador
                .requestMatchers(
                    "/usuarios/**", "/almacen/proveedores/**",
                    "/almacen/ingresos/**", "/mantenimiento/categorias/**",
                    "/reportes/**", "/crm/**", "/contingencia/**"
                ).hasAuthority("ROLE_Administrador")

                // Administrador o Veterinario
                .requestMatchers(
                    "/recetas/**", "/medicamentos-controlados/**",
                    "/tratamientos/**", "/ia/**", "/pacientes/**", "/duenos/**"
                ).hasAnyAuthority("ROLE_Administrador", "ROLE_Empleado")

                // Operativos (cajero + admin)
                .requestMatchers(
                    "/ventas/**", "/almacen/productos/**",
                    "/mantenimiento/servicios/**", "/facturacion/**",
                    "/caja/**", "/api/utilidades/**", "/api/validacion/**",
                    "/dashboard"
                ).hasAnyAuthority("ROLE_Administrador", "ROLE_Empleado")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("usuario")
                .passwordParameter("pass")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
