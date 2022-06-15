package com.bobocode.m2.hw1;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class Demo {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.println(getMaxSizeImage());
        System.out.println(System.currentTimeMillis() - start);
    }

    @SneakyThrows
    public static Pair<String, Long> getMaxSizeImageParallel() {
        HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        HttpRequest request = HttpRequest.newBuilder(new URI("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=15&api_key=DEMO_KEY")).build();
        String jsonBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonBody).findValuesAsText("img_src").stream()
                .map(url -> Pair.of(url, httpClient.sendAsync(HttpRequest.newBuilder(URI.create(url)).method("HEAD", noBody()).build(), HttpResponse.BodyHandlers.discarding())
                        .thenApply(HttpResponse::headers)
                        .thenApply(httpHeaders -> httpHeaders.firstValueAsLong("content-length"))))
                .toList().stream()
                .map(pair -> Pair.of(pair.getLeft(), getRight(pair.getRight())))
                .max(Comparator.comparing(Pair::getRight)).orElseThrow();
    }

    @SneakyThrows
    private static long getRight(CompletableFuture<OptionalLong> completableFuture) {
        OptionalLong optionalLong = completableFuture.get();
        return optionalLong.orElse(0);
    }

    @SneakyThrows
    public static Pair<String, Long> getMaxSizeImage() {
        HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        HttpRequest request = HttpRequest
                .newBuilder(new URI("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=15&api_key=DEMO_KEY"))
                .build();
        String jsonBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(jsonBody)
                .findValuesAsText("img_src").stream()
                .map(url -> createUrlImageSizePair(url, httpClient))
                .max(Comparator.comparing(Pair::getRight))
                .orElseThrow();
    }

    @SneakyThrows
    private static Pair<String, Long> createUrlImageSizePair(String url, HttpClient httpClient) {
        Long size = httpClient.send(HttpRequest.newBuilder(URI.create(url)).method("HEAD", noBody()).build(),
                HttpResponse.BodyHandlers.discarding())
                .headers().firstValueAsLong("content-length").orElseThrow();
        return Pair.of(url, size);
    }
}
