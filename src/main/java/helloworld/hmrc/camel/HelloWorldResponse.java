package helloworld.hmrc.camel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelloWorldResponse {
    private String fullname;
    private long responseCount;
    private String greeting;
}
