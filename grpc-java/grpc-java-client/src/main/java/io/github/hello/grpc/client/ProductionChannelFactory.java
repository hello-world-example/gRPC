package io.github.hello.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.ChannelOption;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProductionChannelFactory {

    /**
     * 创建生产级别的 ManagedChannel 单例
     *
     * @param target 目标地址，支持 DNS、K8s Service 或注册中心地址 (例: "dns:///my-service.prod:50051")
     */
    public static ManagedChannel createProductionChannel(String target) {

        // 【服务端配置 JSON：配置负载均衡与自动重试】
        Map<String, Object> serviceConfig = createServiceConfig();

        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(target)

                // 【负载均衡与服务发现】
                .defaultLoadBalancingPolicy("round_robin") // 设置客户端负载均衡策略（如轮询 round_robin）
                .defaultServiceConfig(serviceConfig)       // 应用自动重试 (Retry) 与对冲 (Hedging) 策略配置

                // 【数据传输限制】
                .maxInboundMessageSize(10 * 1024 * 1024)   // 允许接收的最大单条响应大小为 10MB
                .maxInboundMetadataSize(8 * 1024)          // 允许接收的最大 Header/Metadata 为 8KB

                // 【连接保活与心跳 (Keepalive)】
                .keepAliveTime(30, TimeUnit.SECONDS)       // 每 30 秒发起一次心跳，保持长连接有效性
                .keepAliveTimeout(10, TimeUnit.SECONDS)    // 心跳等待响应超时时间（10秒未响应认为连接失效）
                .keepAliveWithoutCalls(true)               // 即使当前没有 RPC 请求在跑，也持续保活（防防火墙静默断开）

                // 【Netty 底层 Socket 参数】
                .withOption(ChannelOption.TCP_NODELAY, true)   // 禁用 Nagle 算法（降低小请求的延迟）
                .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 建连 TCP 握手超时时间设置为 5 秒

                // 【拦截器 (Interceptors)】
                // .intercept(new ClientAuthInterceptor())    // 自动为请求追加 Auth Token/Trace ID

                // 【连接模式配置】
                .usePlaintext(); // 警告：仅限内网/开发环境使用；跨网/生产环境请配置 TLS

        // 【生产环境推荐 TLS 加密通道配置】
        /*
        try {
            File trustCertCollectionFile = new File("path/to/ca.crt");
            builder.useTransportSecurity()
                   .trustManager(trustCertCollectionFile); // 校验服务端的 CA 证书
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TLS certificates", e);
        }
        */

        return builder.build();
    }

    /**
     * 构造基于 ServiceConfig 的自动重试 JSON 策略
     */
    private static Map<String, Object> createServiceConfig() {
        Map<String, Object> config = new HashMap<>();

        // 重试规则列表
        Map<String, Object> retryPolicy = new HashMap<>();
        retryPolicy.put("maxAttempts", 3.0);             // 最多重试 3 次（含首次请求）
        retryPolicy.put("initialBackoff", "0.1s");       // 初始重试退避间隔 100ms
        retryPolicy.put("maxBackoff", "1s");             // 最大重试退避间隔 1s
        retryPolicy.put("backoffMultiplier", 2.0);        // 退避指数乘数（按 100ms, 200ms, 400ms... 指数递增）
        retryPolicy.put("retryableStatusCodes", java.util.Arrays.asList(
                "UNAVAILABLE",         // 服务暂时不可用（例如网络闪断）
                "RESOURCE_EXHAUSTED"   // 资源耗尽/限流
        ));

        Map<String, Object> methodConfig = new HashMap<>();
        // name 留空表示作用于当前 Channel 调用的所有方法
        methodConfig.put("name", java.util.Collections.singletonList(new HashMap<>()));
        methodConfig.put("retryPolicy", retryPolicy);

        config.put("methodConfig", java.util.Collections.singletonList(methodConfig));
        return config;
    }
}