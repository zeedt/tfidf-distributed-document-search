package com.zeed.distributed.search.entry.service;


import com.zeed.distributed.search.entry.request.SearchRequest;

import java.util.List;
import java.util.Map;

public interface DocumentSearchService {

    List<Object> searchDocument(SearchRequest searchRequest);

}
