package fr.univ.orleans.ws.wsalakon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @autor Vincent
 * @date 19/03/2020
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
   /* @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("fred").password("{noop}fred").roles("USER")
                .and()
                .withUser("admin").password("{noop}admin").roles("USER", "ADMIN");

    }*/
/*
    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        UserDetails vins = User.builder().
                username("Vincent")
                .password("{noop}vinc")
                .roles("USER").build();

        UserDetails admin = User.builder().
                username("admin")
                .password("{noop}admin")
                .roles("USER", "ADMIN").build();
        return new InMemoryUserDetailsManager(vins, admin);

    }
    */

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return new CustomDetailService();

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/swagger-ui/").permitAll()
                .antMatchers(HttpMethod.GET, "/api/messages").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .anyRequest().hasRole("USER")
                .and()
                .httpBasic()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
