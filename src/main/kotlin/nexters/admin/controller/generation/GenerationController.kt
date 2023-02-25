package nexters.admin.controller.generation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.service.generation.CreateGenerationRequest
import nexters.admin.service.generation.GenerationResponse
import nexters.admin.service.generation.GenerationService
import nexters.admin.service.generation.UpdateGenerationRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Generation", description = "기수")
@RequestMapping("/api/generation")
@RestController
class GenerationController(
        private val generationService: GenerationService
) {

    @Operation(summary = "현재 기수 조회")
    @GetMapping("/current")
    fun getCurrentGeneration(): ResponseEntity<GenerationResponse> {
        val generation = generationService.findCurrentGeneration().let {
            GenerationResponse.from(it)
        }

        return ResponseEntity.ok(generation)
    }

    @Operation(summary = "전체 기수 조회")
    @GetMapping
    fun getAllGenerations(): ResponseEntity<List<GenerationResponse>> {
        val generations = generationService.findAllGeneration().map {
            GenerationResponse.from(it)
        }

        return ResponseEntity.ok(generations)
    }

    @Operation(summary = "기수 추가")
    @PostMapping
    fun addGeneration(
            @RequestBody @Valid request: CreateGenerationRequest
    ): ResponseEntity<Void> {
        generationService.createGeneration(request)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "기수 삭제")
    @DeleteMapping("/{generation}")
    fun removeGeneration(
            @PathVariable generation: Int,
    ): ResponseEntity<Void> {
        generationService.deleteGeneration(generation)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "기수 수정")
    @PutMapping("/{generation}")
    fun updateGeneration(
            @PathVariable generation: Int,
            @RequestBody @Valid request: UpdateGenerationRequest,
    ): ResponseEntity<Void> {
        generationService.updateGeneration(generation, request)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "기수 상세조회")
    @GetMapping("/{generation}")
    fun getGenerationDetail(
            @PathVariable generation: Int,
    ): ResponseEntity<GenerationResponse> {
        val generation = generationService.findGeneration(generation).let {
            GenerationResponse.from(it)
        }

        return ResponseEntity.ok(generation)
    }

}
