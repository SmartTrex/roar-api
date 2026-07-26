package com.smarttrex.backend;

public interface DictionaryProvider {
    String getTranscription(String word);
    boolean supports(String language);
}
