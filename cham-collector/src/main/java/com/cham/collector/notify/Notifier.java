package com.cham.collector.notify;

import com.cham.collector.domain.CollectSource;

import java.util.List;

/**
 * 실패 알림. 지금은 로그만 남긴다(LogNotifier). 실패 내역은 수집관리 탭의 수집 이력에서도 보인다.
 * TODO 센드온(SENDON_APIKEY) 문자 알림. cham-api 에도 아직 발송 코드가 없어 같이 만들어야 한다.
 */
public interface Notifier {

    void sourceFailed(CollectSource source, String error);

    void jobFinishedWithFailures(long jobId, List<String> failedSources);
}
