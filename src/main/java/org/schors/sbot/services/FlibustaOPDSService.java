package org.schors.sbot.services;

import lombok.AllArgsConstructor;
import org.schors.sbot.atom.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class FlibustaOPDSService {

    private WebClient webClient;

    public Mono<Feed> searchBook(String book) {
        return searchOpds(book, "books");
    }

    public Mono<Feed> searchAuthor(String author) {
        return searchOpds(author, "authors");
    }

    public Mono<Feed> searchOpds(String what, String type) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("opds", "search")
                        .queryParam("searchType", type)
                        .queryParam("searchTerm", what)
                        .build())
                .accept(MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(Feed.class);
    }

    public Mono<Feed> searchOpds(String url) {
        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(Feed.class);
    }


}
