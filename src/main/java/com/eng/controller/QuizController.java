package com.eng.controller;

import com.eng.dto.StudyResponseDto;
import com.eng.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/quiz/{username}")
    public List<StudyResponseDto> quiz(@PathVariable String username) {
        return quizService.quizList(username);
    }
}
