package nexters.admin.config

import nexters.admin.service.user.AdminService
import nexters.admin.support.auth.AdminAuthInterceptor
import nexters.admin.support.auth.JwtTokenProvider
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorConfig(
        private val jwtTokenProvider: JwtTokenProvider,
        private val adminService: AdminService,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(AdminAuthInterceptor(jwtTokenProvider, adminService))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin") // TODO: DB 연동 시 제거
                .excludePathPatterns("/api/auth/login/**")
                .excludePathPatterns("/api/members/me")
                .excludePathPatterns("/api/members/password")
                .excludePathPatterns("/api/sessions/home")
                .excludePathPatterns("/api/attendance")
    }
}
