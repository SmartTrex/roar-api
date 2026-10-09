package com.smarttrex.backend;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.time.Duration;


public class GutenbergMetadataClient {

    private final HttpClient httpClient;
    private final DocumentBuilderFactory factory;

    public GutenbergMetadataClient() {

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    public GutenbergBookMetadata getMetadata(Long bookId) throws Exception {
        String metadataUrl =
                "https://www.gutenberg.org/cache/epub/"
                + bookId
                + "/pg"
                + bookId
                + ".rdf";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metadataUrl))
                .timeout(Duration.ofMinutes(5))
                .header("User-Agent", "SmartTrexBookImporter/1.0")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
        );

        if (response.statusCode() !=200) {
            throw new RuntimeException(
                    "Не удалось получить metadata книги "
                            + bookId
                            + ". HTTP status: "
                            + response.statusCode()
            );
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(response.body());

        String title = getFirstText(
                document,
                "http://purl.org/dc/terms/",
                "title"
        );

        String author = getAuthor(document);

        String language = getLanguage(document);

        return new GutenbergBookMetadata(
                bookId,
                title,
                author,
                language
        );

    }

    private String getFirstText(
            Document document,
            String namespace,
            String localName
    ) {
        NodeList nodes = document.getElementsByTagNameNS(
                namespace,
                localName
        );

        if (nodes.getLength() == 0) {
            return null;
        }

        return nodes.item(0)
                .getTextContent()
                .trim();
    }

    private String getAuthor (Document document) {
        NodeList creators = document.getElementsByTagNameNS(
                "http://purl.org/dc/terms/",
                "creator"
        );

        if (creators .getLength() ==0 ) {
            return "Unknown author";
        }

        Element creator = (Element) creators.item(0);

        NodeList names = creator.getElementsByTagNameNS(
                "http://www.gutenberg.org/2009/pgterms/",
                "name"
        );

        if (names.getLength() ==0 ) {
            return "Unknown author";
        }

        return names.item(0)
                .getTextContent()
                .trim();
    }

    private String getLanguage (Document document) {
        NodeList languages = document.getElementsByTagNameNS(
                "http://purl.org/dc/terms/",
                "language"
        );

        if (languages.getLength() ==0 ) {
            return null;
        }

        Element language = (Element) languages.item(0);
        NodeList values = language.getElementsByTagNameNS(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                "value"
        );

        if (values.getLength() ==0 ) {
            return null;
        }

        return values.item(0)
                .getTextContent()
                .trim();
    }

    public record GutenbergBookMetadata(
            long id,
            String title,
            String author,
            String language
    ) {

    }
}
