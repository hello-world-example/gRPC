package io.github.hello.protobuf;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

public class ProtobufDemo {
    static void main(String[] args) throws InvalidProtocolBufferException {
        // -------------------------------------------------------------
        // 1. 构建动态字段数据
        // -------------------------------------------------------------

        // A. 构建 DetailInfo 并打包进 Any
        DetailInfo detail = DetailInfo.newBuilder()
                .setAddress("北京市朝阳区")
                .setAge(28)
                .build();
        Any anyData = Any.pack(detail);

        // B. 构建 Struct (类似 JSON: {"theme": "dark", "score": 95})
        Struct structData = Struct.newBuilder()
                .putFields("theme", Value.newBuilder().setStringValue("dark").build())
                .putFields("score", Value.newBuilder().setNumberValue(95).build())
                .build();

        // -------------------------------------------------------------
        // 2. 序列化：创建 Person 对象并转换为二进制 byte[]
        // -------------------------------------------------------------
        Person person = Person.newBuilder()
                .setId(1001L)
                .setName("张三")
                .setExtraAny(anyData)
                .setExtraStruct(structData)
                .build();

        byte[] rawBytes = person.toByteArray();
        System.out.println("序列化后的字节流长度: " + rawBytes.length + " bytes");

        // -------------------------------------------------------------
        // 3. 反序列化：从 byte[] 还原为 Java 对象
        // -------------------------------------------------------------
        Person deserializedPerson = Person.parseFrom(rawBytes);

        System.out.println("\n--- 解包基础字段 ---");
        System.out.println("ID: " + deserializedPerson.getId());
        System.out.println("Name: " + deserializedPerson.getName());

        // A. 解包 Any（先判断类型是否匹配，再 Unpack）
        System.out.println("\n--- 解包 Any 字段 ---");
        if (deserializedPerson.getExtraAny().is(DetailInfo.class)) {
            DetailInfo unpackedDetail = deserializedPerson.getExtraAny().unpack(DetailInfo.class);
            System.out.println("Address: " + unpackedDetail.getAddress());
            System.out.println("Age: " + unpackedDetail.getAge());
        }

        // B. 读取 Struct 中的 JSON 字段
        System.out.println("\n--- 解包 Struct 字段 ---");
        Struct readStruct = deserializedPerson.getExtraStruct();
        String theme = readStruct.getFieldsOrThrow("theme").getStringValue();
        double score = readStruct.getFieldsOrThrow("score").getNumberValue();
        System.out.println("Theme: " + theme + ", Score: " + score);
    }
}