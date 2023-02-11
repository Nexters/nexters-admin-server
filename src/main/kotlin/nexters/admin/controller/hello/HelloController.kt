package nexters.admin.controller.hello

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Hello", description = "헬스 체킹 용도")
@RestController()
class HelloController {

    @Operation(summary = "서버 살아있나?")
    @GetMapping("/health-check")
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity.ok("{\"result\": \"ok\"}")
    }
}