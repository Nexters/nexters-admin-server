package nexters.admin.config

import org.springframework.context.annotation.Configuration
import java.util.TimeZone
import javax.annotation.PostConstruct

@Configuration
class TimeZoneConfig {

    @PostConstruct
    fun setSeoulTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
