package io.github.hello.grpc.server;

import java.io.IOException;

public class ProductApp {
    static void main() throws IOException {
        new ProductServer().start();

        IO.readln();
    }
}
