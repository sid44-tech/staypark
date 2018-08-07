package staypark.parking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private StayParkAuthenticationProvider authProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/about", "/airport-parking", "/room-park", "/room-park-results", "/affiliates",
                        "/faq", "/contact-us", "/sitemap", "/privacy", "/tac", "/browse-airports", "/parking/**",
                        "/airport/**", "/hotel-parking/**", "/checkout", "/forgot", "/signup", "/images/**", "/js/**", "/css/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(new StayParkAuthenticationSuccessHandler())
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                //.logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                //.addLogoutHandler(logoutHandler)
                //.deleteCookies(cookieNamesToClear)
                .permitAll();

    }
}
