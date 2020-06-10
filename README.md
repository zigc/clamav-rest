Clamav-rest [ClamAV](http://www.clamav.net/) REST proxy. Builds on top of clamav-java which is a minimal Java client for ClamAV.

Freshclam update virus databases every 2h.

# Usage

```bash
    docker run --name clamav-rest -d -p 8080:8080 zigc/clamav-rest
```

## Testing the REST service

You can use [curl](http://curl.haxx.se/) as it's REST. Here's an example test session:

```
curl localhost:8080
Clamd responding: 
true - clean file
false - threat found

curl -F "name=testfile" -F "file=@./eicar.txt" localhost:8080/scan
false

curl -F "name=non-virus" -F "file=@./non-virus.txt" localhost:8080/scan
true
```

EICAR is a test file which is recognized as a virus by scanners even though it's not really a virus. Read more [EICAR information here](https://www.eicar.org/?page_id=3950).


## The technical details

This is a REST proxy server with support for basic INSTREAM scanning and PING command. 

Clamd protocol is explained here:
http://linux.die.net/man/8/clamd

Clamd protocol contains command such as shutdown so exposing clamd directly to external services is not a feasible option. Accessing clamd directly is fine if you are running single application and it's on the localhost. 

### Build
```
  mvn package
  docker build -t clamav-rest .
```

### Development

You have two options. You can use [Docker](https://www.docker.com/) and run a Docker imageto test it. The Docker image is based on the supplied [Dockerfile specification](https://github.com/solita/clamav-rest/blob/master/Dockerfile).

Or you can build the JAR. This creates a stand-alone JAR with embedded [Jetty serlet container](http://www.eclipse.org/jetty/).

```
  mvn package
```

Starting the REST service is quite straightforward.

```
  java -jar clamav-rest-1.0.2.jar --server.port=8765 --clamd.host=myprecious.clamd.serv.er --clamd.port=3310
```

## Description
ClamAV daemon as a Docker image. It *builds* with a current virus database and
*runs* `freshclam` in the background constantly updating the virus signature database. `clamd` itself
is listening on exposed port `3310`.

