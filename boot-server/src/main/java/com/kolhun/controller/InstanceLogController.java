package com.kolhun.controller;

import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Controller
@RequestMapping("logs")
@ResponseBody
@AllArgsConstructor
public class InstanceLogController {

    private static final String LOG_ENDPOINT_ID = "logfile";
    private static final String LOGS_ZIP_FILE_NAME = "all_logs.zip";

    private final InstanceRegistry registry;
    private final WebClient webClient = WebClient.create();


    @GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity> fetchAllLogs() {
        Flux<SourceBody> endpointBodyFlux = registry.getInstances()
                .flatMap(instance -> {
                    for (Endpoint endpoint : instance.getEndpoints()) {
                        if (LOG_ENDPOINT_ID.equals(endpoint.getId())) {
                            return webClient.get().uri(endpoint.getUrl()).retrieve()
                                    .bodyToMono(String.class)
                                    .map(body -> new SourceBody(instance.getRegistration().getName(), body));
                        }
                    }
                    return Flux.empty();
                });

        BiFunction<List<SourceBody>, SourceBody, List<SourceBody>> accumulator =
                (endpointBodies, endpointBody) -> {
                    endpointBodies.add(endpointBody);
                    return endpointBodies;
                };
        Mono<List<SourceBody>> monoEndpoints = endpointBodyFlux
                .reduce(new ArrayList<>(), accumulator);


        Mono<ResponseEntity> response = monoEndpoints.map(s -> {
                    try {
                        ByteArrayOutputStream zos = createZipFile(s);
                        return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=" + LOGS_ZIP_FILE_NAME)
                                .body(zos.toByteArray());
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error occurred");
                    }
                }
        );

        return response;
    }

    private ByteArrayOutputStream createZipFile(List<SourceBody> endpointBodies) throws IOException {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (SourceBody endpointBody : endpointBodies) {
                ZipEntry entry = new ZipEntry(endpointBody.sourceName + ".log");
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(endpointBody.body.getBytes());
            }
            return byteArrayOutputStream;
        }
    }


    @AllArgsConstructor
    private static class SourceBody {

        private String sourceName;

        private String body;

    }

}