package io.github.hello.spring.grpc.client;

import io.github.hello.spring.grpc.api.hello.HelloGrpc;
import io.github.hello.spring.grpc.api.hello.HelloReply;
import io.github.hello.spring.grpc.api.hello.HelloRequest;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class GrpcClientRunner implements CommandLineRunner {

    @Resource
    private HelloGrpc.HelloBlockingStub stub;

    @Override
    public void run(String... args) throws Exception {
        //
        //
        for (int i = 0; i < 10; i++) {
            TimeUnit.SECONDS.sleep(1);
            //
            try {
                HelloRequest request = HelloRequest.newBuilder().setName("Alien").build();
                HelloReply reply = stub.sayHello(request);
                System.out.println(reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
