package com.eng.controller;

import com.eng.dto.*;
import com.eng.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MyPageController {
    private final MyPageService myPageService;

    // 사용자가 학습하기, 퀴즈 풀이를 눌렀을 때 학습 시간 db 저장
    @PostMapping("/my-page")
    public void saveStudyTime(@RequestBody MyPageRequestDto myPageRequestDto) {
        myPageService.saveStudyTime(myPageRequestDto);
    }

    // 학습시간 표 결과 출력
    @GetMapping("/my-page/time/{username}")
    public List<MyPageResponseDto> get7dayTime(@PathVariable String username) {
        return myPageService.get7dayTime(username);
    }

    @GetMapping("/my-page/level/{username}")
    public List<LevelResponseDto> getLevel(@PathVariable String username) {
        return myPageService.getLevel(username);
    }

    // 복습 우선 순위 테이블
    @GetMapping("/my-page/review/{username}")
    public List<QuizHistoryResponseDto> getReview(@PathVariable String username) {
      return myPageService.getReview(username);
    }

    // 최근 오답 기록 top 3
    @GetMapping("/my-page/recent-quiz/{username}")
    public List<QuizHistoryResponseDto> getRecentWrongQuiz(@PathVariable String username) {
      return myPageService.getRecentWrongQuiz(username);
    }

    // 어휘, 문법, 응답시간 (ai 피드백 중 ai 응답값 외 나머지)
    @GetMapping("/my-page/ai/{username}")
    public AiFeedbackResponseDto getAiData(@PathVariable String username) {
      return myPageService.getAiData(username);
    }

    // db에서 ai 피드백 가져오기
    @GetMapping("/my-page/ai/{username}/feedback")
    public AiFeedbackResponseDto getAiFeedback(@PathVariable String username) {
        return myPageService.getAiFeedback(username);
    }

    // ai 피드백 새로고침
    @GetMapping("/my-page/ai/{username}/refresh")
    public AiFeedbackResponseDto getRefreshFeedback(@PathVariable String username) {
        return myPageService.getRefreshFeedback(username);
    }
}
