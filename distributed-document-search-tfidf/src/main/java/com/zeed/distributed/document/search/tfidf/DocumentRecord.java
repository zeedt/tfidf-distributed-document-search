package com.zeed.distributed.document.search.tfidf;


import java.util.HashMap;
import java.util.Map;

public class DocumentRecord {

    private String documentName;

    private final Map<String, Double> termFrequency = new HashMap<>();

    public void populateTermFrequency(String term, Double value) {
        termFrequency.put(term, value);
    }


    public Map<String, Double> getTermFrequency() {
        return termFrequency;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentName() {
        return documentName;
    }
}
