package com.zeed.distributed.document.search.tfidf;

import com.zeed.distributed.document.search.registry.ServiceRegistry;
import com.zeed.distributed.document.search.request.SearchRequest;
import com.zeed.distributed.document.search.service.RestApiCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class TFIDF {

    @Autowired
    private RestApiCall restApiCall;

    @Autowired
    private ServiceRegistry serviceRegistry;

    public List<DocumentScore> getTermFrequencyInverseDocumentFrequencyScoreFromAllDocumentsWithDistributedSearch
            (List<String> documentPaths, String statement) {

        TreeMap<Double, List<String>> scoreTreeMap = new TreeMap<>();

        final List<DocumentRecord> documentRecords = new ArrayList<>();

        List<String> availableNodes = serviceRegistry.getAllChildrenNodes();
        int noPerNode = documentPaths.size()/availableNodes.size();

        List<List<String>> documentListList = new ArrayList<>();
        for (int i=0;i<availableNodes.size();i++) {
            int startIndex = i*noPerNode;
            int endIndex = (i+1)*noPerNode;
            endIndex = (i==availableNodes.size()-1) ? documentPaths.size() : endIndex;
            documentListList.add(documentPaths.subList(startIndex, endIndex));
        }

        List<CompletableFuture<DocumentRecord[]>> futures = new ArrayList<>();
        for (int i = 0; i < availableNodes.size(); i++) {
            String worker = availableNodes.get(i);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setSearchWords(statement);
            searchRequest.setDocumentPaths(documentListList.get(i));
            futures.add(restApiCall.makeCallForSearch(worker+"/tfidf/get-term-frequency", searchRequest));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (int i = 0; i < availableNodes.size(); i++) {
            try {
                documentRecords.addAll(Arrays.asList(futures.get(i).get()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        List<String> terms = Arrays.asList(statement.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
        Map<String, Double> inverseDocumentFrequency = getInverseDocumentFrequency(terms, documentRecords);

        // Calculate document score
        for (DocumentRecord documentRecord:documentRecords) {
            double score = calculateDocumentScore(terms, documentRecord, inverseDocumentFrequency);
            List<String> documents;
            if ((documents = scoreTreeMap.get(score)) == null) {
                documents = new ArrayList<>();
            }
            documents.add(documentRecord.getDocumentName());
            scoreTreeMap.put(score, documents);
        }

        return getDocumentScoreFromTreeMap(scoreTreeMap);
    }

    public List<DocumentRecord> getDocumentRecords(String searchStatement, List<String> documentPaths) throws IOException {
        List<String> terms = Arrays.asList(searchStatement.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
        final List<DocumentRecord> documentRecords = new ArrayList<>();
        for (String document:documentPaths) {
            documentRecords.add(getTermFrequencyForDocument(terms, document));
        }
        return documentRecords;
    }



    public List<DocumentScore> getTermFrequencyInverseDocumentFrequencyScoreFromAllDocuments
            (List<String> documentPaths, String statement) throws IOException {

        TreeMap<Double, List<String>> scoreTreeMap = new TreeMap<>();

        final List<DocumentRecord> documentRecords = new ArrayList<>();

        List<String> terms = Arrays.asList(statement.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
//        List<String> terms = Arrays.asList(statement.split("\\s+"));
        for (String document:documentPaths) {
            documentRecords.add(getTermFrequencyForDocument(terms, document));
        }

        Map<String, Double> inverseDocumentFrequency = getInverseDocumentFrequency(terms, documentRecords);

        // Calculate document score
        for (DocumentRecord documentRecord:documentRecords) {
            double score = calculateDocumentScore(terms, documentRecord, inverseDocumentFrequency);
            List<String> documents;
            if ((documents = scoreTreeMap.get(score)) == null) {
                documents = new ArrayList<>();
            }
            documents.add(documentRecord.getDocumentName());
            scoreTreeMap.put(score, documents);
        }

        return getDocumentScoreFromTreeMap(scoreTreeMap);
    }

    private List<DocumentScore> getDocumentScoreFromTreeMap(TreeMap<Double, List<String>> scoreTreeMap) {
        List<DocumentScore> documentScores = new ArrayList<>();
        for (Double score:scoreTreeMap.descendingKeySet()) {
            List<String> documentNames = scoreTreeMap.get(score);
            documentNames.forEach(d-> {
                documentScores.add(new DocumentScore(d, score));
            });
        }
        return documentScores;
    }

    private double calculateDocumentScore(List<String> terms, DocumentRecord documentRecord, Map<String, Double> inverseDocumentFrequency) {
        double score = 0;
        for (String term:terms) {
            score += documentRecord.getTermFrequency().get(term) * inverseDocumentFrequency.get(term);
        }
        return score;
    }

    private Map<String, Double> getInverseDocumentFrequency(List<String> terms, List<DocumentRecord> documentRecords) {
        Map<String, Double> inverseDocumentFrequency = new HashMap<>();
        for (String term:terms) {
            int count = 0;
            for (DocumentRecord documentRecord:documentRecords) {
                if (documentRecord.getTermFrequency().get(term) > 0.0) {
                    count++;
                }
            }
            inverseDocumentFrequency.put(term, count == 0 ? 0 : Math.log10((double)documentRecords.size()/count));
        }
        return inverseDocumentFrequency;
    }

    private DocumentRecord getTermFrequencyForDocument(List<String> terms, String documentPath) throws IOException {
        final List<String> words = new ArrayList<>();
        Files.lines(Paths.get(documentPath)).forEach(line-> {
            List<String> lineWords = Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
            lineWords = lineWords.stream().filter(w->!w.equals("")).collect(Collectors.toList());
            words.addAll(lineWords);
        });
        int docLen = words.size();
        DocumentRecord documentRecord = new DocumentRecord();
        documentRecord.setDocumentName(documentPath);
        for (String term:terms) {
            int count = 0;
            for (String word:words) {
                if (word.equalsIgnoreCase(term)) {
                    count++;
                }
            }
            documentRecord.populateTermFrequency(term, (double)count/docLen);
        }
        return documentRecord;
    }

}
