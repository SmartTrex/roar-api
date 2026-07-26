package com.smarttrex.backend;

import org.springframework.stereotype.Service;
import  org.springframework.web.client.RestClient;

    @Service
    public class EnglishDictionaryProvider implements DictionaryProvider {
        private final RestClient restClient = RestClient.create("https://api.dictionaryapi.dev/api/v2/entries/en/<word>");
        @Override
        public boolean supports(String language) {
            return "en".equalsIgnoreCase(language);
        }

        @Override
        public String getTranscription(String word) {
            try {
                var response = restClient.get()
                        .uri("/{word}", word)
                        .retrieve()
                        .body(String.class);
                return "Транскрипция найдена (API ответил)";
            }catch (Exception e) {
                return "Не удалось получить транскрипцию";
            }
        }

    }
