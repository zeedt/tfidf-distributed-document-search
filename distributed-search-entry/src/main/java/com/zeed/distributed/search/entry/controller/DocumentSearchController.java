package com.zeed.distributed.search.entry.controller;

import com.zeed.distributed.search.entry.request.SearchRequest;
import com.zeed.distributed.search.entry.service.DocumentSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tfidf-document-search")
public class DocumentSearchController {

    @Autowired
    private DocumentSearchService documentSearchService;

    @PostMapping
    public List<Object> searchDocumentWithTfidf(@RequestBody SearchRequest searchRequest) {
        return documentSearchService.searchDocument(searchRequest);
    }

}
