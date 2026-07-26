package com.smarttrex.backend;

public class LibraryBook {
    private Long id;
    private String title;
    private String author;
    private String level;
    private String coverCode;
    private String coverUrl; //ссылка на обложку
    private String downloadUrl; //ссылка на файл книги epub
    private String content;

    public LibraryBook(Long id, String title, String author, String level, String coverCode, String coverUrl, String downloadUrl, String content) {
        this. id = id;
        this. title = title;
        this. author = author;
        this. level = level;
        this. coverCode = coverCode;
        this. coverUrl = coverUrl;
        this. downloadUrl = downloadUrl;
        this. content = content;
    }

    public  Long getId () {return id; }
    public String getTitle () {return title; }
    public String getAuthor () {return author; }
    public String getLevel () {return level; }
    public String getCoverCode () {return coverCode; }
    public String getCoverUrl() {return coverUrl; }
    public String getDownloadUrl() {return downloadUrl; }
    public String getContent () {return content; }

}
