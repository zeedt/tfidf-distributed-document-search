package com.zeed.distributed.document.search.request;


import lombok.Data;

import java.util.List;

@Data
public class SearchRequest {

    private String searchWords;

    private List<String> documentPaths;

}
