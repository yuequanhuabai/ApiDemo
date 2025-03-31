//package com.example.demo.conf;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoders;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http.authorizeHttpRequests(
//                (authorize) -> authorize.anyRequest().authenticated()
//        ).oauth2ResourceServer(
//                (oauth2) -> oauth2.jwt(Customizer.withDefaults() )
//        );
//
//        return http.build();
//    }
//
//
//    public JwtDecoder jwtDecoder(){
//        return JwtDecoders.fromIssuerLocation("https://localhost:8080");
//    }
//}
