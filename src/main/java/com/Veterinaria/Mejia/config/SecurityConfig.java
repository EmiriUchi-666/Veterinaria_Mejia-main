package com.Veterinaria.Mejia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                    "/usuarios/**", "/almacen/proveedores/**", "/almacen/ingresos/**",
                    "/reportes/**", "/crm/**", "/contingencia/**",
                    "/medicamentos-controlados/**", "/recetas/**", "/tratamientos/**",
                    "/mantenimiento/**", "/facturacion/**", "/caja/**", "/ia/**"
                ).hasAuthority("ROLE_Administrador")

                // Empleado: Acceso de solo lectura a ciertas áreas
                .requestMatchers(
                    HttpMethod.GET, "/pacientes/**", "/duenos/**", "/almacen/productos/**"
                ).hasAnyAuthority("ROLE_Administrador", "ROLE_Empleado")

                // Empleado: Acceso completo a módulos operativos
                .requestMatchers(
                    "/ventas/**", "/citas/**", "/dashboard"
                ).hasAnyAuthority("ROLE_Administrador", "ROLE_Empleado")

                // Administrador: Acceso completo a lo que el empleado tiene en solo lectura
                .requestMatchers(
                    "/pacientes/**", "/duenos/**", "/almacen/productos/**"
                ).hasAuthority("ROLE_Administrador")

                // API públicas para AJAX
                .requestMatchers("/api/utilidades/**", "/api/validacion/**").permitAll()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("usuario")
                .passwordParameter("pass")
                .defaultSuccessUrl("/", true)
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
