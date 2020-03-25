package helloworld.hmrc.camel;

import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class FrontEndRoute extends RouteBuilder {
    @Override
    public void configure() {
        // error handler for json validation errors, returning json error back to callee
        onException(ValidationException.class)
                .log("${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .handled(true)
                .setBody(simple("{\"message\": \"validation error\"}"));

        onException(ServerProcessingError.class)
                .log("Server processing error")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .handled(true)
                .setBody(simple("{\"message\": \"server error\"}"));

        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json);

        rest("/hello").produces("application/json")
                .post()
                .route()
                .log(">>> ${body}")
                .log("validating input")
                .to("direct:jsonValidateFront")
                .log("validation ok")
                .log("transforming to backend json schema")
                .to("direct:transformFront2Back")
                .log("sending to backend processing")
                .to("direct:remoteService")
                .log("received response from backend")
                .to("direct:routeResponse")
                .endRest();

        from("direct:jsonValidateFront")
                // expects inputstream not json object
                .marshal().json(JsonLibrary.Jackson, true)
                .to("json-validator:front-end-schema.json");

        // route to unmarshal string to json object, expected type by rest binding mode json
        from("direct:inJsonString").unmarshal().json(JsonLibrary.Jackson, true);

        // route to transform front dto to back dto
        from("direct:transformFront2Back")
            .to("direct:inJsonString")
            .log("transforming >>> ${body}")
            .to("jolt:front-end-to-back-jolt.json")
            .log("transformed <<< ${body}");

        from("direct:routeResponse")
                .log("routing based on http response code: ${headers}")
                .choice()
                .when(header("SERVER_ERROR").isEqualTo(500))
                    .throwException(new ServerProcessingError())
                .otherwise()
                    .to("direct:transformResponse");

        from("direct:transformResponse")
                .log(">>> have ${body} <<<")
                // jolt uses json object
                .unmarshal().json(JsonLibrary.Jackson, true)
                .log("transforming >>> ${body}")
                .to("jolt:back-end-to-front-jolt.json")
                .log("transformed <<< ${body}");
    }
}
