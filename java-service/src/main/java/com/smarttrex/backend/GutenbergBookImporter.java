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
import java.util.ArrayList;

public class GutenbergBookImporter {

    public static void main(String[] args) throws Exception {
        //Где лежит каталог выбранных книг
        Path catalogDir = Path.of("catalog");
        Path selectionFile = catalogDir.resolve ("selection.json");

        //Куда складываются EPUB
        Path booksDir = catalogDir.resolve("books");

        Files.createDirectories(booksDir);

        Path coversDir = catalogDir.resolve("covers");

        Files.createDirectories(coversDir);

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

        GutenbergMetadataClient metadataClient = new GutenbergMetadataClient();

        List<CatalogBook> catalogBooks = new ArrayList<>();

        //Загружаем каждую выбранную книгу
        for (long bookId : bookIds) {

            String epubUrl =
                    "https://dev.gutenberg.org/cache/epub/"
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

            GutenbergMetadataClient.GutenbergBookMetadata metadata =
                    metadataClient.getMetadata(bookId);

            System.out.println("Название: " + metadata.title());
            System.out.println("Автор: " + metadata. author());
            System.out.println("Язык: " + metadata. language());

            //Адрес обложки на Gutenberg
            String remoteCoverUrl =
                    "https://dev.gutenberg.org/cache/epub/"
                    + bookId
                    + "/pg"
                    +bookId
                    + ".cover.medium.jpg";

            //Куда сохраняем обложку на пк
            Path coverFile = coversDir.resolve(bookId + ".jpg");

            //пока считаем что обложки нет
            String localCoverUrl = null;

            try {
                HttpRequest coverRequest = HttpRequest.newBuilder()
                        .uri(URI.create(remoteCoverUrl))
                        .timeout(Duration.ofMinutes(2))
                        .header("User-Agent", "SmartTrexBookImporter/1.0")
                        .GET()
                        .build();

                HttpResponse<Path> coverResponse = httpClient.send(
                        coverRequest,
                        HttpResponse.BodyHandlers.ofFile(coverFile)
                );

                if (coverResponse.statusCode() == 200) {
                    localCoverUrl = "/covers/" +bookId + ".jpg";

                    System.out.println(
                            "Обложка сохранена: " + coverFile.toAbsolutePath()
                    );
                } else {
                    Files.deleteIfExists(coverFile);

                    System.out.println(
                            "Не удалось скачать обложку. HTTP status: "
                                    +coverResponse.statusCode()
                    );
                }


            } catch (Exception e) {
                Files.deleteIfExists(coverFile);

                System.out.println(
                        "Ошибка загрузки обложки: " + e.getMessage()
                );
            }

            CatalogBook catalogBook = new CatalogBook(
                    bookId,
                    metadata.title(),
                    metadata.author(),
                    metadata.language(),
                    "/books/" +bookId + ".epub",
                    localCoverUrl
            );

            catalogBooks.add(catalogBook);

        }

        Path catalogFile = catalogDir.resolve("catalog.json");

        objectMapper.writeValue(catalogFile, catalogBooks);

        System.out.println(
                "Каталог сохранен: " + catalogFile.toAbsolutePath()
        );

    }
}