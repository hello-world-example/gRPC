package io.github.hello.grpc.server;

import io.github.hello.grpc.server.service.HelloGrpcImpl;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.ChannelOption;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProductServer {

    private Server server;

    private final int port = 50051;

    public void start() throws IOException {
        // 自定义业务处理线程池（防止默认线程池在极高并发下无限创建线程导致 OOM）
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);

        NettyServerBuilder builder = NettyServerBuilder.forPort(port)
                .executor(executor) // 指定业务处理线程池

                // 【数据传输限制】
                .maxInboundMessageSize(10 * 1024 * 1024) // 限制单条入站消息最大 10MB（防御大包攻击）
                .maxInboundMetadataSize(8 * 1024)        // 限制 Header/Metadata 最大 8KB

                // 【连接与心跳控制 (Keepalive)】
                .permitKeepAliveTime(10, TimeUnit.SECONDS) // 允许客户端发送心跳的最短间隔（小于该值会被视为 Keepalive 刷屏）
                .permitKeepAliveWithoutCalls(true)        // 即使当前没有活跃 RPC 调用，也允许客户端发送心跳保活
                .maxConnectionIdle(15, TimeUnit.MINUTES)   // 连接空闲超过 15 分钟则自动关闭（释放无用连接）
                .maxConnectionAge(30, TimeUnit.MINUTES)    // 强制单条连接的最大存活时间为 30 分钟（有利于配合 L4 负载均衡重连）
                .maxConnectionAgeGrace(5, TimeUnit.MINUTES) // 连接强制关闭前的优雅缓冲时间（给未完成的请求 5 分钟完成）

                // 【Netty 底层 Socket 参数调优】
                .withOption(ChannelOption.SO_BACKLOG, 1024)     // TCP 半连接/全连接队列深度（应对突发高并发连接请求）
                .withOption(ChannelOption.SO_REUSEADDR, true)   // 允许重用本地地址和端口（方便快速重启服务）
                .withOption(ChannelOption.TCP_NODELAY, true)   // 禁用 Nagle 算法（减少小包延迟，提高响应速度）
                .withOption(ChannelOption.SO_KEEPALIVE, true); // 开启操作系统 TCP 层的 Keepalive 探测

        // 【安全配置 (生产环境推荐开启 TLS/mTLS)】
        /*
        File certChainFile = new File("path/to/server.crt");
        File privateKeyFile = new File("path/to/server.key");
        builder.useTransportSecurity(certChainFile, privateKeyFile); // 单向 TLS
        */


        // 【注册服务与拦截器】
        builder.addService(new HelloGrpcImpl());
        // 反射服务
        builder.addService(ProtoReflectionServiceV1.newInstance());
        // 注册服务端全局日志/鉴权拦截器
        // builder.intercept(new ServerLoggingInterceptor());

        this.server = builder.build().start();
        System.out.println("Production gRPC Server started on port " + port);

        // 注册 JVM 停止时的优雅关机 Hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        if (server == null) {
            return;
        }
        try {
            System.out.println("Initiating graceful shutdown...");
            // 终止接收新请求，给已有请求 30 秒处理时间
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Graceful shutdown interrupted, forcing termination.");
            server.shutdownNow(); // 超过 30 秒则强行终止
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}