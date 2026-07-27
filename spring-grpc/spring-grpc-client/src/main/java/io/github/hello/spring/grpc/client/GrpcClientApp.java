//package io.github.hello.spring.grpc.client;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.grpc.client.ImportGrpcClients;
//
//@ImportGrpcClients
//@SpringBootApplication
//public class GrpcClientApp {
//
//    static void main(String[] args) {
//        SpringApplication.run(GrpcClientApp.class, args);
//    }
//
//    @Bean
//    public CommandLineRunner runner(SimpleGrpc.SimpleBlockingStub stub) {
//        return args -> {
//            System.out.println(stub.sayHello(HelloRequest.newBuilder().setName("Alien").build()));
//        };
//    }
//
//}
