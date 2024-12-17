package com.eng.controller;

import com.eng.dto.QuizRequestDto;
import com.eng.dto.QuizResponseDto;
import com.eng.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/quiz/{username}")
    public List<QuizResponseDto> quiz(@PathVariable String username) {
        return quizService.quizList(username);
    }

    @PutMapping("/quiz/{username}")
    public void quiz_correct(@PathVariable String username, @RequestBody QuizRequestDto dto) {
        quizService.quiz_correct(dto);
    }
}
