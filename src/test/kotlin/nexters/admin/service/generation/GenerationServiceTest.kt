package nexters.admin.service.generation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nexters.admin.controller.generation.CreateGenerationRequest
import nexters.admin.controller.generation.UpdateGenerationRequest
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.exception.BadRequestException
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import nexters.admin.testsupport.ApplicationTest
import nexters.admin.testsupport.createNewGenerationMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class GenerationServiceTest(
        @Autowired private val generationService: GenerationService,
        @Autowired private val generationRepository: GenerationRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
) {

    @Test
    fun `기수를 생성한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(22)) }

        val generation = generationRepository.findByGeneration(22)
        generation?.generation shouldBe 22
        generation?.status shouldBe GenerationStatus.BEFORE_ACTIVITY
    }

    @Test
    fun `특정 기수를 조회한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(21)) }
        generationService.run { createGeneration(CreateGenerationRequest(22)) }

        val generation = generationService.findGeneration(22)
        generation.generation shouldBe 22
        generation.status shouldBe GenerationStatus.BEFORE_ACTIVITY
    }

    @Test
    fun `전체 기수를 조회한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(21)) }
        generationService.run { createGeneration(CreateGenerationRequest(22)) }

        val generations = generationService.findAllGeneration()

        generations.data shouldHaveSize 2
    }

    @Test
    fun `현재 기수를 조회한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(21)) }
        generationService.run { createGeneration(CreateGenerationRequest(22)) }

        val generation = generationService.findCurrentGeneration()

        generation.generation shouldBe 22
    }

    @Test
    fun `기수 정보를 변경한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(22)) }

        generationService.updateGeneration(22, UpdateGenerationRequest(GenerationStatus.DURING_ACTIVITY.value!!))

        val generation = generationRepository.findByGeneration(22)
        generation?.status shouldBe GenerationStatus.DURING_ACTIVITY
    }

    @Test
    fun `잘못된 활동정보로 기수 정보를 변경하면 예외를 반환한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(22)) }

        shouldThrow<BadRequestException> {
            generationService.updateGeneration(22, UpdateGenerationRequest("김태현의 활동"))
        }
    }

    @Test
    fun `기수회원이 없는 기수를 삭제한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(22)) }

        generationService.deleteGeneration(22)

        val generation = generationRepository.findByGeneration(22)
        generation shouldBe null
    }

    @Test
    fun `기수회원이 있는 기수를 삭제하면 예외를 반환한다`() {
        generationService.run { createGeneration(CreateGenerationRequest(22)) }
        generationMemberRepository.save(createNewGenerationMember())

        shouldThrow<BadRequestException> {
            generationService.deleteGeneration(22)
        }
    }
}
