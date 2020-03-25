package helloworld.hmrc.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

/**
 * Backend route service, for ease, this is simply another set of routes within the same application as
 * frontend service.
 */
@Component
public class BackEndRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // handle validation errors, result in headers being set to indicate error
        onException(ValidationException.class)
                .log("validation failed")
                .log("${exception.message}")
                .setHeader("SERVER_ERROR", constant(500))
                .setHeader("ERROR_TYPE", constant("validation error"))
                .handled(true)
                .setBody();

        from("direct:remoteService")
                .routeId("direct-route")
                .tracing()
                .log("direct:remoteService")
                .log("in >>> ${body}")
                .log("validating input")
                .to("direct:jsonValidateBack")
                .log("validation ok")
                .unmarshal().json(JsonLibrary.Jackson)
                .log("out <<< ${body}")
                .to("direct:createResponse")
                .endRest();

        // validate received json
        from("direct:jsonValidateBack")
                // expects inputstream not json object
                .marshal().json(JsonLibrary.Jackson, true)
                .to("json-validator:back-end-schema.json");

        // route to unmarshal string to json object, expected type by rest binding mode json
        from("direct:inJsonString2").unmarshal().json(JsonLibrary.Jackson);

        // process json input and return response as json string since this would normally be done as a back end service
        from("direct:createResponse")
                .process(new Processor() {

                    final AtomicLong counter = new AtomicLong();

                    @Override
                    public void process(Exchange exchange) throws Exception {

                        HashMap<String, Object> body = (HashMap<String, Object>) exchange.getIn().getBody();
                        String forename = (String) body.get("forename");
                        String surname = (String) body.get("surname");
                        HelloWorldResponse response = HelloWorldResponse.builder()
                                .greeting(format("Hello %s, how are you today", forename))
                                .fullname(format("%s %s", forename, surname))
                                .responseCount(counter.incrementAndGet())
                                .build();
                        exchange.getIn().setBody(response);
                    }
                })
        .marshal().json(JsonLibrary.Jackson);
    }
}
