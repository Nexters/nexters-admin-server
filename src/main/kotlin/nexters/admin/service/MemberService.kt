package nexters.admin.service

import nexters.admin.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
        val memberRepository: MemberRepository
)
