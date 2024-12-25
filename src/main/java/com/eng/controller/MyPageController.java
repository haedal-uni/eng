package com.eng.controller;

import com.eng.dto.LevelResponseDto;
import com.eng.dto.MyPageRequestDto;
import com.eng.dto.MyPageResponseDto;
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
}
