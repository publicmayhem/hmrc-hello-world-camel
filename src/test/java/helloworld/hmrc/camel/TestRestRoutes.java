package helloworld.hmrc.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRestRoutes {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void happyPathTest() throws IOException {
        File resource = ResourceUtils.getFile("classpath:test-hello-in.json");
        String requestJsonAsString = new String(Files.readAllBytes(resource.toPath()));
        ObjectMapper objectMapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJsonAsString, headers);

        ResponseEntity<String> responseAsStr = restTemplate.postForEntity("/hello", request, String.class);
        assertThat(responseAsStr.getStatusCodeValue(), is(200));
        String body = responseAsStr.getBody();

        assertThat(body, is(notNullValue()));
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("fullname").asText(), is("James Dean"));
        assertThat(root.path("message").asText(), is("Hello James, how are you today"));
    }

    @Test
    public void unhappyPathTest() throws IOException {
        File resource = ResourceUtils.getFile("classpath:test-hello-invalid.json");
        String requestJsonAsString = new String(Files.readAllBytes(resource.toPath()));
        ObjectMapper objectMapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJsonAsString, headers);

        ResponseEntity<String> responseAsStr = restTemplate.postForEntity("/hello", request, String.class);
        assertThat(responseAsStr.getStatusCodeValue(), is(400));
        String body = responseAsStr.getBody();

        assertThat(body, is(notNullValue()));
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("message").asText(), is("validation error"));
    }
}
