package io.github.hello.grpc.client;

import io.github.hello.grpc.api.hello.HelloGrpc;
import io.github.hello.grpc.api.hello.HelloReply;
import io.github.hello.grpc.api.hello.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class ClientApp {

    public static void main(String[] args) {
        // 1. 初始化生产 Channel（应用级别全局单例，不要随用随建）
        ManagedChannel channel = ProductionChannelFactory.createProductionChannel("127.0.0.1:50051");

        try {
            // 2. 基于单例 Channel 构建 Stub（Stub 是轻量级的，可以按需创建）
            HelloGrpc.HelloBlockingStub stub = HelloGrpc.newBlockingStub(channel);

            // 3. 必须：每次发起 RPC 必须明确带上 Deadline 超时控制！
            HelloReply reply = stub
                    .withDeadlineAfter(3, TimeUnit.SECONDS) // 设置这次请求最高 3 秒超时
                    .sayHello(HelloRequest.newBuilder().setName("Production").build());

            System.out.println(reply.getMessage());

        } catch (StatusRuntimeException e) {
            // 4. 处理 gRPC 状态异常
            System.err.println("RPC Call failed. Status: " + e.getStatus().getCode() + ", Reason: " + e.getStatus().getDescription());
        } finally {
            // 5. 应用关闭/容器退出时，优雅关闭 Channel
            shutdownChannel(channel);
        }
    }

    private static void shutdownChannel(ManagedChannel channel) {
        if (channel != null && !channel.isShutdown()) {
            try {
                System.out.println("Shutting down gRPC Channel...");
                // 停止接受新调用，并等待已有请求完成
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Channel shutdown interrupted, forcing close.");
                channel.shutdownNow();
            }
        }
    }
}