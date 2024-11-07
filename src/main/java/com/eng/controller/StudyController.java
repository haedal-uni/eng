package com.eng.controller;

import com.eng.dto.StudyResponseDto;
import com.eng.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudyController {
    private final StudyService service;

    @GetMapping("/study-words/{username}")
    public List<StudyResponseDto> getStudyWords(@PathVariable String username){
        return service.getStudyWord(username);
    }
}
