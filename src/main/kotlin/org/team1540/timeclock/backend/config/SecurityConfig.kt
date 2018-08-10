package org.team1540.timeclock.backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.team1540.timeclock.backend.services.MongoUserDetailsService

@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var authConfig: AuthenticationConfig

    @Autowired
    private lateinit var mongoUserDetailsService: MongoUserDetailsService

    override fun configure(web: WebSecurity) {
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                .authorizeRequests().antMatchers("/*").permitAll()
                .antMatchers("/timesheet/**").hasRole("TIMESHEET")
                .antMatchers("/clockapi/**").hasRole("TIMECLOCK")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .and().httpBasic()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(mongoUserDetailsService).passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder())
    }
}
