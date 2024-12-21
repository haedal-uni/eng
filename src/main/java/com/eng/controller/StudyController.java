package com.eng.controller;

import com.eng.dto.StudyRequestDto;
import com.eng.dto.StudyResponseDto;
import com.eng.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudyController {
    private final StudyService service;

    @GetMapping("/study-words/{username}")
    public List<StudyResponseDto> getStudyWords(@PathVariable String username){
        return service.getStudyWord(username);
    }

    @PostMapping("/study-words")
    public void saveStudyWord(@RequestBody StudyRequestDto studyRequestDto){
        service.saveStudyWord(studyRequestDto);
    }
}
