package com.github.mysterix5.capstone.cloudstorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Repository
@Slf4j
public class CloudRepository {

    private final RestTemplate restTemplate;

    private final String username;
    private final String password;
    private final String baseUrl;

    public CloudRepository(RestTemplate restTemplate,
                           @Value("${app.webdav.username}") String username,
                           @Value("${app.webdav.password}") String password,
                           @Value("${app.webdav.baseurl}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
    }

    public byte[] find(String filePath) throws RuntimeException {
        String url = baseUrl + filePath;

        ResponseEntity<byte[]> result = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders()), byte[].class);

        return result.getBody();
    }

    public HttpHeaders createHeaders() {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(
                    auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }

    public String save(String filePath, byte[] byteArray) throws RuntimeException {
        HttpHeaders headers = createHeaders();
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(filePath)
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        HttpEntity<byte[]> fileEntity = new HttpEntity<>(byteArray, fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String requestUrl = baseUrl + filePath;
        log.info(requestUrl);
        ResponseEntity<String> result = restTemplate.exchange(requestUrl, HttpMethod.PUT, requestEntity, String.class);

        return result.getBody();
    }

}
