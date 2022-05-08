package com.zeed.distributed.search.entry.service.impl;

import com.zeed.distributed.search.entry.request.SearchRequest;
import com.zeed.distributed.search.entry.service.DocumentSearchService;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;


@Service
public class DocumentSearchServiceImpl implements DocumentSearchService, Watcher {

    public static final String SERVICE_REGISTRY_NODE = "/service_registry";

    private static String COORDINATOR_URL = null;

    private static final String DOCUMENT_SEARCH_PATH = "/tfidf/distributed-search";

    private RestTemplate restTemplate = new RestTemplate();

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSearchServiceImpl.class);

    @Autowired
    private ZooKeeper zooKeeper;

    @PostConstruct
    public void init() throws Exception {
        Stat stat = this.zooKeeper.exists(SERVICE_REGISTRY_NODE, this);
        if (stat == null)
            throw new Exception("Service registry node not yet created");
        getLatestCoordinatorAddress();
    }

    @Override
    public List<Object> searchDocument(SearchRequest searchRequest) {
        return restTemplate.postForObject(COORDINATOR_URL+DOCUMENT_SEARCH_PATH, searchRequest, List.class);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            getLatestCoordinatorAddress();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getLatestCoordinatorAddress() throws KeeperException, InterruptedException {
        byte[] data = this.zooKeeper.getData(SERVICE_REGISTRY_NODE,false, null);
        COORDINATOR_URL = new String(data).replaceAll("\"","");
        LOGGER.info("Latest coordinator URL ==> " + COORDINATOR_URL);
    }
}
