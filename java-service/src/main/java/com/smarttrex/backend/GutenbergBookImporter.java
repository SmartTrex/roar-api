package com.smarttrex.backend;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class GutenbergBookImporter {

    public static void main(String[] args) throws Exception {
        //Где лежит каталог выбранных книг
        Path catalogDir = Path.of("catalog");
        Path selectionFile = catalogDir.resolve ("selection.json");

        //Куда складываются EPUB
        Path booksDir = catalogDir.resolve("books");

        Files.createDirectories(booksDir);

        //Читаем selection.Json
        ObjectMapper objectMapper =new ObjectMapper();
        List <Long> bookIds = objectMapper.readValue(
                selectionFile.toFile(),
                new TypeReference<List<Long>>() {}
        );

        //Создаем http клиент
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        //Загружаем каждую выбранную книгу
        for (long bookId : bookIds) {

            String epubUrl =
                    "https://www.gutenberg.org/cache/epub/"
                        +bookId
                        +"/pg"
                        +bookId
                        +".epub";

            Path outputFile = booksDir.resolve(bookId + ".epub");

            System.out.println("Скачиваем книгу " + bookId);
            System.out.println("URL: " + epubUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(epubUrl))
                    .timeout(Duration.ofMinutes(5))
                    .header("User-Agent", "SmartTrexBookImporter/1.0")
                    .GET()
                    .build();

            HttpResponse<Path> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofFile(outputFile)
            );

            if (response.statusCode() !=200) {
                Files .deleteIfExists(outputFile);

                throw new RuntimeException(
                        "Не удалось скачать книгу " +bookId
                                + ". HTTP status: "
                                +response.statusCode()
                );

            }
            System.out.println("Готово: " + outputFile.toAbsolutePath());

        }

    }
}