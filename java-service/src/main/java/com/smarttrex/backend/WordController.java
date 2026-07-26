package com.smarttrex.backend;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
public class WordController {
    @GetMapping("/translate")
    public Map<String, String> translateWord(@RequestParam String word) {
        System.out.println("Запрос получен, слово: " + word);
        return Map.of(
                "original", word,
                "translation", "яблоко"
        );
    }

}
