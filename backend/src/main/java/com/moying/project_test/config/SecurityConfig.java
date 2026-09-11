package com.moying.project_test.config;

import com.moying.project_test.Filter.JwtAuthFilter;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

/**
 * @author 墨莹
 * @date 2026/8/26 15:31
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 安全过滤链：现阶段全部接口放行
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter ) throws Exception {

        http

                // ✅ 自动去找名称为 corsConfigurationSource 的Bean，不用传参！
                //cors 跨域配置
                .cors(Customizer.withDefaults())             //关闭csrf防护，前后端分离必须关掉
                .csrf(csrf -> csrf.disable())
                //声明当前项目是无状态认证，Spring Security 永远不会创建 HttpSession
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers("/api/auth/login","/api/product/*","/api/auth/register","/api/auth/refresh","/api/product/page","/api/category/*").permitAll()//登录注册放行
                        .requestMatchers("/images/**").permitAll()
                        .anyRequest().authenticated() //剩下所有接口，必须登录认证
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 密码加密器，后面注册、登录校验密码要用 BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}