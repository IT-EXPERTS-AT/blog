# Getting started

The example code uses:

- Java 21
- Docker

This repository demonstrates data replication with Debezium to an Elasticsearch Index.  

## Quick start
1. Before running `docker compose up -d`, please download and unpack the ElasticSearch Sink Connector from [here](https://www.confluent.io/hub/confluentinc/kafka-connect-elasticsearch).  
2. Spin up the services via `docker compose`
3. Start the application
4. Configure Kafka connect by running `requests/post-cdc-db-connector.http` and `requests/post-elastic-sink.http`
5. Play around by manipulating the `person` table and querying the ElasticSearch index!

For detailed instructions see [blog post](https://it-experts.at/blog/posts/blog-page-06-debezium.html).