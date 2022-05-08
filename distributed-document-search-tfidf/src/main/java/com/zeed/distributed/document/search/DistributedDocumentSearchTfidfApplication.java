package com.zeed.distributed.document.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DistributedDocumentSearchTfidfApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedDocumentSearchTfidfApplication.class, args);
    }

}
