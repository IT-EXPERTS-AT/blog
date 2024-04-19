# Getting started

The example code uses:

- Java 21
- Docker

On application start, Spring Boot starts the Rabbit MQ container defined in the `docker-compose.yml` automatically.  
To insert messages into the queues manually, the Rabbit MQ [admin UI](http://localhost:15672/#/queues) can be used.  
If you send a message to the incomingOrderQueue make sure to set the message header to
`__TypeId__=at.itexperts.orderprocessing.IncomingOrder` and use a JSON payload e.g. `{"id":1,"productId":100}`
