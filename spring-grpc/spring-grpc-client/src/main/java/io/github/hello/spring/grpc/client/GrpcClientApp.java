package io.github.hello.spring.grpc.client;

import io.github.hello.spring.grpc.api.hello.HelloGrpc;
import io.github.hello.spring.grpc.api.hello.HelloRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.ImportGrpcClients;


@SpringBootApplication
@ImportGrpcClients(basePackages = "io.github.hello.spring.grpc.api")
public class GrpcClientApp {

    static void main(String[] args) {
        SpringApplication.run(GrpcClientApp.class, args);
    }

}
