package com.bumh3r.config;

import com.bumh3r.service.impl.UsuarioDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler rolBasedSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            switch (role) {
                case "ROLE_DDA", "ROLE_CIT" -> response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                case "ROLE_SUBDIRECTOR"      -> response.sendRedirect(request.getContextPath() + "/subdirector");
                case "ROLE_COORDINADOR"      -> response.sendRedirect(request.getContextPath() + "/coordinador");
                case "ROLE_TUTOR"            -> response.sendRedirect(request.getContextPath() + "/tutor");
                case "ROLE_TUTORADO"         -> response.sendRedirect(request.getContextPath() + "/tutorado");
                default                      -> response.sendRedirect(request.getContextPath() + "/login");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y rutas públicas
                .requestMatchers("/css/**", "/js/**", "/images/**", "/styles/**",
                                 "/webjars/**", "/login", "/error", "/error/**", "/public/**", "/").permitAll()
                // Imágenes subidas (fotos de tutores, tutorados, coordinadores, etc.)
                // Deben ir ANTES de las reglas de rol para que cualquier usuario autenticado pueda verlas
                .requestMatchers(
                    "/tutor/*.jpg",  "/tutor/*.jpeg",  "/tutor/*.png",  "/tutor/*.gif",  "/tutor/*.webp",
                    "/tutor/*.JPG",  "/tutor/*.JPEG",  "/tutor/*.PNG",
                    "/tutorado/*.jpg", "/tutorado/*.jpeg", "/tutorado/*.png", "/tutorado/*.gif", "/tutorado/*.webp",
                    "/tutorado/*.JPG", "/tutorado/*.JPEG", "/tutorado/*.PNG",
                    "/coordinador/*.jpg", "/coordinador/*.jpeg", "/coordinador/*.png", "/coordinador/*.gif",
                    "/coordinador/*.JPG", "/coordinador/*.JPEG", "/coordinador/*.PNG",
                    "/pat/*.jpg", "/pat/*.jpeg", "/pat/*.png", "/pat/*.gif",
                    "/actividad/*.jpg", "/actividad/*.jpeg", "/actividad/*.png", "/actividad/*.gif",
                    "/evidencia/*.jpg", "/evidencia/*.jpeg", "/evidencia/*.png", "/evidencia/*.gif",
                    "/evidencia/*.pdf"
                ).authenticated()
                // Área administrativa (DDA y CIT)
                .requestMatchers("/admin/**").hasAnyRole("DDA", "CIT")
                // Dashboard raíz redirige según rol — requiere autenticación
                .requestMatchers("/dashboard").hasAnyRole("DDA", "CIT")
                // API de búsqueda (usada desde vistas admin vía AJAX)
                .requestMatchers("/api/search/**").hasAnyRole("DDA", "CIT")
                // Panel de subdirector
                .requestMatchers("/subdirector/**").hasRole("SUBDIRECTOR")
                // Panel de coordinador
                .requestMatchers("/coordinador/**").hasRole("COORDINADOR")
                // Panel de tutor
                .requestMatchers("/tutor/**").hasRole("TUTOR")
                // Panel de tutorado
                .requestMatchers("/tutorado/**").hasRole("TUTORADO")
                // Mi cuenta — cualquier usuario autenticado
                .requestMatchers("/mi-cuenta/**").authenticated()
                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(rolBasedSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}
