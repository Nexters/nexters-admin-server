package nexters.admin.domain.generation_member

import io.kotest.matchers.shouldBe
import nexters.admin.testsupport.createNewGenerationMember
import org.junit.jupiter.api.Test

class GenerationMemberTest {

    @Test
    fun `회원 직군 정보 변경`() {
        val generationMember = createNewGenerationMember()

        generationMember.updatePosition(Position.DESIGNER, null)

        generationMember.position shouldBe Position.DESIGNER
        generationMember.subPosition shouldBe null
    }

    @Test
    fun `점수 변화 내역을 기준으로 현재 출결 점수 다시 계산하여 수정`() {
        val generationMember = createNewGenerationMember(score = 50)

        generationMember.updateScoreByChanges(listOf(-5, -10, -10, 10))

        generationMember.score shouldBe MAX_SCORE - 15
    }

    @Test
    fun `운영진 여부 반환`() {
        val generationMember =  createNewGenerationMember(position = Position.MANAGER)

        generationMember.isManager() shouldBe true
    }
}
