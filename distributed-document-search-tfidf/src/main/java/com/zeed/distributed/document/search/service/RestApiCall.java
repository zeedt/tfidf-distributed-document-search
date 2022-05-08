package com.zeed.distributed.document.search.service;


import com.zeed.distributed.document.search.request.SearchRequest;
import com.zeed.distributed.document.search.tfidf.DocumentRecord;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;

@Service
public class RestApiCall {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Async
    public CompletableFuture<DocumentRecord[]> makeCallForSearch(String workerUrl, SearchRequest searchRequest) {
        DocumentRecord[] result = REST_TEMPLATE.postForObject(workerUrl, searchRequest, DocumentRecord[].class);
        return CompletableFuture.completedFuture(result);
    }

}
