# Akka

### Working but not distributed. 

#### Server-Client communication (one-many) over TCP (remote). Server and each client are different applications with unique ActorSystem. Messages are asynchronous but server can deal only with one client at a time (unfortunately).
