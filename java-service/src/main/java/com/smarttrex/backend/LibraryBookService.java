package com.smarttrex.backend;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.ArrayList;

@Service
public class LibraryBookService {

    //RestTemplate Родной инструмент java который умеет делать HTTP запросы в интернет
    private final RestTemplate restTemplate;

    public LibraryBookService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(30)); // Ждем подключение максимум 3 сек
        factory.setReadTimeout(java.time.Duration.ofSeconds(30));  // Ждем ответ максимум 3 сек
        this.restTemplate = new RestTemplate(factory);

    }

    public List <LibraryBook> getAllBooks() {
        //Запрашиваем адрес бесплатной библиотеки Project Gutenberg (Книги на английском)
        String url = "https://gutendex.com/books?languages=en";

        try {
            //идем в интернет и забираем ответ от сервера Гутенберга
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            JsonNode results = response.get("results");

            List <LibraryBook> realBooks = new ArrayList<>();

            //проходим по полученным книгам
            if (results != null && results.isArray()) {
                for (JsonNode bookNode : results) {
                    Long id = bookNode.get("id").asLong();
                    String title = bookNode.get("title").asText();

                    //Достаем имя автора
                    String author = "Unknown Author";
                    if (bookNode.has("authors") && bookNode.get("authors").size() > 0) {
                        author = bookNode.get("authors").get(0).get("name").asText();
                    }

                    //Переменные под ссылки
                    String coverUrl = null;
                    String downloadUrl = null;

                    //Обложка и файл
                    if (bookNode.has("formats")) {
                        JsonNode formats = bookNode.get("formats");

                        //ищем ссылку на обложку
                        if (formats.has("image/jpeg")) {
                            coverUrl = formats.get("image/jpeg").asText();
                        }

                        //ищет ссылку на файл EPUB
                        if (formats.has("application/epub+zip")) {
                            downloadUrl = formats.get("application/epub+zip").asText();
                        } else if (formats.has("text/plain; charset=us-ascii")) {
                            downloadUrl = formats.get("text/plain; charset=us-ascii").asText();
                        }
                    }



                    //Превращаем данные с внешнего сервера в наш объект LibraryBook
                    realBooks.add (new LibraryBook(
                            id,
                            title,
                            author,
                            "B2",
                            "EN",
                            coverUrl,
                            downloadUrl,
                            "Read text content for book ID " + id
                    ));
                }
            }

            return realBooks;

        } catch (Exception e) {
            e.printStackTrace();
            // Если интернет отвалился или внешний сервис недоступен — отдаем наш резервный список
            return List.of(
                    new LibraryBook(1L, "Frankenstein", "Mary Shelley",
                            "B2", "FR", null,
                            null, "Once..."),

                    new LibraryBook(2L, "Moby-Dick",
                            "Herman Melville", "C1",
                            "MD", null,
                            null, "Call...")
            );
        }
    }

    public LibraryBook getBookById(long id) {
        for (LibraryBook book : getAllBooks()) {
            if (book.getId(). equals(id)) {
                return book;
            }
        }
        return null;
    }

    }
