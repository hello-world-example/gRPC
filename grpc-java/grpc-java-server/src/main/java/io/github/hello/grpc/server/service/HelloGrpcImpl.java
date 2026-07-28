package io.github.hello.grpc.server.service;

import io.github.hello.grpc.api.hello.HelloGrpc;
import io.github.hello.grpc.api.hello.HelloReply;
import io.github.hello.grpc.api.hello.HelloRequest;
import io.grpc.stub.StreamObserver;

public class HelloGrpcImpl extends HelloGrpc.HelloImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> resp) {
        String name = request.getName();
        //
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + name + " " + System.currentTimeMillis()).build();
        //
        resp.onNext(reply);
        resp.onCompleted();

    }
}
