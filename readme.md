# Camel Spring Boot Test - Rob Law

## Synopsis

I was not made aware of this task until yesterday lunchtime, so only had 2 hours to complete as much as I could, 
which I found challenging considering the number of components, and setting up the spring boot / camel stack.

The solution comprises of a single rest endpoint http://localhost:8080/hello

Build the solution as, assuming linux/osx operating system

    * mvn install
    * docker build -t hmrc-rob-law/hello-world .
    * docker run -p 8080:8080 -t hmrc-rob-law/hello-world
    * ----
    * open up a second console, and try the following commands
    * curl -X POST -H "Content-Type: application/json" -d @src/main/resources/person-james.json http://localhost:8080/hello
    * curl -X POST -H "Content-Type: application/json" -d @src/main/resources/person-invalid.json http://localhost:8080/hello
    
## Rest Component Tests

See src/test/java/helloworld/hmrc/camel/RestEndToEndSolutionTest. Provides a happy path and an unhappy validation error test.

## Route Unit Tests

Normally I would provide very strong test coverage of Camel Routes, ensuring all possible paths and expectations are
asserted. However I was unable to configure the environment from scratch to obtain a CamelContext etc within a 
Unit Test environment within the short amount of time I had given myself. Hence I have provided 2 Route Unit tests 
for illustrative purposes only.

## Recommendations

I probably spent as much of my time setting up the maven/springboot/camel environment than creating routes. Since this
is just a Contractor Interview task, I would recommend providing a pom and stubbed but empty application. That way
2 hours can be spent on the task rather setting up environment from scratch. Camel documentation is notorious for always
being a couple of years out of date. It's good for documenting component properties, but pretty useless for configuration etc. 