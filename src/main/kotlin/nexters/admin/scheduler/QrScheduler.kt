package nexters.admin.scheduler

import nexters.admin.repository.QrCodeRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class QrScheduler(
        private val qrCodeRepository: QrCodeRepository
) {
    // TODO: 동시성 이슈 나중에 해결 필요 - 스케줄러가 동작할 때 관리자에 의해 initializeCodes 메서드 호출되면 데이터가 잘못 덮어씌어질 수 있음.
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    fun runEveryMinute() {
        qrCodeRepository.updateValidCodes()
    }
}
