package com.zeed.distributed.document.search.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeed.distributed.document.search.request.SearchRequest;
import com.zeed.distributed.document.search.tfidf.DocumentRecord;
import com.zeed.distributed.document.search.tfidf.DocumentScore;
import com.zeed.distributed.document.search.tfidf.TFIDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tfidf")
public class SearchController {

    @Value("${documents.path:./resources/books}")
    private String documentPath;

    @Autowired
    private TFIDF tfidf;

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @PostMapping
    public List<DocumentScore> getDocumentScore(@RequestBody SearchRequest searchRequest) throws IOException {
        File file = new File(documentPath);
        List<String> filePaths = new ArrayList<>();
        for (String path:file.list()) {
            filePaths.add(String.format("%s/%s", documentPath, path));
        }
        TFIDF tfidf = new TFIDF();
        return tfidf.getTermFrequencyInverseDocumentFrequencyScoreFromAllDocuments(filePaths, searchRequest.getSearchWords());
    }

    @PostMapping("/get-term-frequency")
    public List<DocumentRecord> getTermFrequency(@RequestBody SearchRequest searchRequest) throws IOException {
        LOGGER.info("Handling part of distribution search work with document size of " + searchRequest.getDocumentPaths().size());
        LOGGER.info("Files to search" + OBJECT_MAPPER.writeValueAsString(searchRequest.getDocumentPaths()));
        return tfidf.getDocumentRecords(searchRequest.getSearchWords(), searchRequest.getDocumentPaths());
    }


    @PostMapping("/distributed-search")
    public List<DocumentScore> getDocumentScoreWithDistributedSearch(@RequestBody SearchRequest searchRequest) throws IOException {
        // Divide work
        LOGGER.info("The coordinator handling the job with document path " + documentPath);
        File file = new File(documentPath);
        List<String> filePaths = new ArrayList<>();
        for (String path:file.list()) {
            filePaths.add(String.format("%s/%s", documentPath, path));
        }
        return tfidf.getTermFrequencyInverseDocumentFrequencyScoreFromAllDocumentsWithDistributedSearch(
                filePaths, searchRequest.getSearchWords());
    }

}
