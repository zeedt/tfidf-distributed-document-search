package com.zeed.distributed.search.entry.request;


import lombok.Data;

import java.util.List;

@Data
public class SearchRequest {

    private String searchWords;

    private List<String> documentPaths;

}
