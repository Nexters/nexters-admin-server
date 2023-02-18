package nexters.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.TimeZone
import javax.annotation.PostConstruct

@SpringBootApplication
class ServerApplication {

    @PostConstruct
    fun started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
