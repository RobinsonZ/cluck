package org.team1540.timeclock.backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val REALM = "TIMECLOCK"

@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var authConfig: AuthenticationConfig

    val basicAuthenticationEntryPoint
        @Bean get() = RestAuthEntryPoint()

    class RestAuthEntryPoint : BasicAuthenticationEntryPoint() {
        override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {
            response?.status = HttpServletResponse.SC_UNAUTHORIZED;
            response?.addHeader("WWW-Authenticate", "Basic realm=$realmName");
        }

        override fun afterPropertiesSet() {
            realmName = REALM
            super.afterPropertiesSet()
        }
    }

    override fun configure(web: WebSecurity) {
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                .authorizeRequests().antMatchers("/*").permitAll()
                .antMatchers("/timesheet/**").run {
                    if (authConfig.secureTimesheetApi) {
                        hasRole("TIMESHEET")
                    } else {
                        permitAll()
                    }
                }.antMatchers("/clockapi/**").hasRole("TIMECLOCK")
                .and().httpBasic().realmName(REALM).authenticationEntryPoint(basicAuthenticationEntryPoint)
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
                .passwordEncoder(NoOpPasswordEncoder.getInstance())
                .withUser(authConfig.timeclockUsername).password(authConfig.timeclockPassword).roles("TIMECLOCK", "TIMESHEET")
                .and().apply {
                    if (authConfig.secureTimesheetApi) {
                        withUser(authConfig.timesheetUsername).password(authConfig.timesheetPassword)
                    } else {
                        and()
                    }
                }
    }
}
