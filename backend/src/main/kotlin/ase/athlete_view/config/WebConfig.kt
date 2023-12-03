package ase.athlete_view.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebMvc
class WebConfig {
    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val source = UrlBasedCorsConfigurationSource();
        var config = CorsConfiguration();
        config.allowCredentials = true;
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("http://127.0.0.1:4200")
        config.addAllowedOrigin("https://localhost:4200")
        config.addAllowedOrigin("https://127.0.0.1:4200")
        config.allowedHeaders = listOf(
            HttpHeaders.ACCEPT,
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE
        )

        config.allowedMethods = listOf(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.PUT.name(),
            HttpMethod.OPTIONS.name()
        )

        //30 min
        config.maxAge = 3600L;
        source.registerCorsConfiguration("/**", config);
        val bean = FilterRegistrationBean(CorsFilter(source))
        bean.order = -1000
        return bean
    }
}
