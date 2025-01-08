package com.eng.controller;

import com.eng.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/autocomplete")
    public List<Map<String, List<String>>> autocomplete(@RequestParam String query) {
        return searchService.getWordRedis(query);
    }
}
