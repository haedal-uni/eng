package com.eng.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlController {
    @GetMapping("/")
    public String mainPage(){
        return "main";
    }

    @GetMapping("/my-page")
    public String myPage(){
        return "myPage";
    }

    @GetMapping("/blue")
    public String bluePage(){
        return "blue";
    }

    @GetMapping("/error")
    public String error(){
        return "error";
    }
}
