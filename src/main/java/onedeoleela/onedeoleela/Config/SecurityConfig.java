package onedeoleela.onedeoleela.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Apply CORS configuration before applying other security filters
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS configuration
                .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless APIs
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Allow all requests - you might want to restrict this in production
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow specific origins for frontend access
        config.setAllowedOrigins(List.of("http://192.168.1.67:3030", "http://103.6.120.246:3030"));

        // Allow specific HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow headers that are necessary for your requests
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept", "department"));

        // Allow credentials such as cookies, Authorization headers, etc.
        config.setAllowCredentials(true);

        // Expose any necessary headers to be accessible by the frontend (e.g., Authorization)
        config.addExposedHeader("Authorization");

        // Create the CorsConfigurationSource instance and register the configuration
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // Apply to all endpoints
        return source;
    }
}