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
    fun `운영진 여부 반환`() {
        val generationMember =  createNewGenerationMember(position = Position.MANAGER)

        generationMember.isManager() shouldBe true
    }
}
