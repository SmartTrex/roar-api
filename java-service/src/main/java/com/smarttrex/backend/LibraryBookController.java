package com.smarttrex.backend;

import org.springframework.web.bind.annotation.*;

import java.util.List;

// Эндпоинт №1: Отдает список всех книг для экрана "Бесплатные книги"
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/books")
public class LibraryBookController {
    private final LibraryBookService libraryBookService;

    //Передаем контроллеру наш LibraryBookService
    public LibraryBookController(LibraryBookService libraryBookService) {
        this.libraryBookService = libraryBookService;
    }

    @GetMapping
    public List <LibraryBook> getAllBooks() {
        return libraryBookService.getAllBooks(); //повар отдай список
    }

        @GetMapping("/{id}")
                public LibraryBook getAllBookById(@PathVariable Long id) {
            return libraryBookService.getBookById(id);
        }
}
