### Distributed TFIDF document search implementation

This service is used to calculate the TFIDF (term frequency inverse document frequency) score. This implementation heavily leverage on zookeeper for distribution and coordination.

The application is of two modules:

#### 1. distributed-document-search-tfidf

This is the main service that does the search. Multiple instances of this service can be created. Each instance of this service will create a child node in the worker znode.
This means 5 children in the worker znode signifies that 5 instances of the application is running. 

The ephemeral sequential is used which means the child nodes are named incrementally. The first child is used as the leader node and it is responsible for coordinating the tfidf search activity.

The application instance of this leader node will always have updated information of the other worker nodes since 
it will be distributing the term frequency operation to the non-leader worker nodes.

The leader node application instance address (i.e. ip:port) is then saved in the service registry node for use by the second service (distributed-search-entry).

The distributed-document-search-tfidf service has endpoint (`/tfidf/distributed-search`) for distributing the term frequency calculation among the worker nodes (aside the leader node). 
The leader node coordinates this search by making API call (the number of available documents is shared equally or almost equally among the non-leader worker nodes) 
to the different nodes available at the time and aggregate the response.

The aggregated term frequency is further used to calculate the TFIDF score against all documents. 
This makes the search fast since the operation is distributed among different non-leader worker nodes (instances of the distributed-document-search-tfidf service)


#### 2. distributed-search-entry

This service serves as main entry for TFIDF document search score. The only endpoint it has is `/tfidf-document-search`. 
This service also has connection to the zookeeper and it has a watcher attched to the service registry znode so as to always get the updated 
address of the leader node instance. When the `tfidf-document-search` is invoked, this service gets the address of the current leader
node for the `distributed-document-search-tfidf` service. It makes call to this endpoint `/tfidf/distributed-search` of
the `distributed-document-search-tfidf` service (leader instance).

Not : The service registry znode `service_registry` must be created before starting this service otherwise the application will fail to start.

##### Sample request

`
{


    "searchWords" : "The girl that falls through a rabbit hole into a fantasy wonderland"
}
`

##### Sample response

`
[


    {
        "documentName": "./resources/books/Alice’s Adventures in Wonderland.txt",
        "score": 9.696860929659861E-4
    },
    {
        "documentName": "./resources/books/The Iliad of Homer by Homer.txt",
        "score": 5.3232577082794685E-5
    },
    {
        "documentName": "./resources/books/The Wonderful Wizard of Oz.txt",
        "score": 4.964753556251226E-5
    },
    {
        "documentName": "./resources/books/Moby Dick.txt",
        "score": 4.300814499061496E-5
    },
    {
        "documentName": "./resources/books/The Adventures of Tom Sawyer.txt",
        "score": 4.04227521175738E-5
    },
    {
        "documentName": "./resources/books/Grimms’ Fairy Tales.txt",
        "score": 3.505889814691011E-5
    },
    {
        "documentName": "./resources/books/Heart of Darkness.txt",
        "score": 3.336407070977303E-5
    },
    {
        "documentName": "./resources/books/Crime And Punishment.txt",
        "score": 3.326755019303753E-5
    },
    {
        "documentName": "./resources/books/The Count of Monte Cristo.txt",
        "score": 2.0930511856295092E-5
    },
    {
        "documentName": "./resources/books/The Adventures of Sherlock Holmes.txt",
        "score": 1.8504441623191227E-5
    },
    {
        "documentName": "./resources/books/Dracula.txt",
        "score": 1.4260373428613982E-5
    },
    {
        "documentName": "./resources/books/A Tale of Two Cities.txt",
        "score": 1.3351152025949666E-5
    },
    {
        "documentName": "./resources/books/The Souls of Black Folk.txt",
        "score": 7.061414630844442E-6
    },
    {
        "documentName": "./resources/books/War and Peace.txt",
        "score": 5.988399317097557E-6
    },
    {
        "documentName": "./resources/books/The Strange Case Of Dr. Jekyll And Mr. Hyde.txt",
        "score": 5.9107093519190745E-6
    },
    {
        "documentName": "./resources/books/Pride and Prejudice.txt",
        "score": 2.497733846941051E-6
    },
    {
        "documentName": "./resources/books/The Importance of Being Earnest.txt",
        "score": 0.0
    },
    {
        "documentName": "./resources/books/A Modest Proposal.txt",
        "score": 0.0
    },
    {
        "documentName": "./resources/books/Frankenstein.txt",
        "score": 0.0
    },
    {
        "documentName": "./resources/books/The Yellow Wallpaper.txt",
        "score": 0.0
    }
]
`
