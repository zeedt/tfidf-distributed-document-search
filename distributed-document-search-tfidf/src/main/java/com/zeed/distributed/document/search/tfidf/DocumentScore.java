package com.zeed.distributed.document.search.tfidf;

public class DocumentScore {

    private String documentName;

    private Double score;

    public DocumentScore(String documentName, Double score) {
        this.documentName = documentName;
        this.score = score;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
