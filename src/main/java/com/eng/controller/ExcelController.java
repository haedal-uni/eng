package com.eng.controller;

import com.eng.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService service;

    @PostMapping("/upload-excel")
    public void postWord(MultipartFile file) throws IOException {
        service.saveWord(file);
    }
}
