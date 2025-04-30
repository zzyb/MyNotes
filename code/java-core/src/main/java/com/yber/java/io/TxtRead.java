package com.yber.java.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TxtRead {
    public static void main(String[] args) throws IOException {
//        InputStreamReader isr = new InputStreamReader(System.in);
//        int read = isr.read();
//        while(read != -1){
//            System.out.println(read);
//            read = isr.read();
//        }
//        PrintWriter out = new PrintWriter("/Users/zhangyanbo/out.txt", String.valueOf(StandardCharsets.UTF_8));
//        out.print("hello");
//        out.print(" ");
//        out.print("world");
//        out.close();
//        List<String> strings = Files.readAllLines(Paths.get("/Users/zhangyanbo/out.txt"), StandardCharsets.UTF_8);
//        for(String value:strings){
//            System.out.println(value);
//        }
//        Stream<String> lines = Files.lines(Paths.get("/Users/zhangyanbo/out.txt"),StandardCharsets.UTF_8);
//        String collect = lines.collect(Collectors.joining(","));
//        System.out.println(collect);


//        DataInputStream dis =
//                new DataInputStream(
//                        new BufferedInputStream(
//                                new FileInputStream("/Users/zhangyanbo/out.txt"))
//        );
//        int i = dis.readInt();
//        System.out.println(i);


//        PushbackInputStream pis = new PushbackInputStream(
//                new BufferedInputStream(
//                        new FileInputStream("/Users/zhangyanbo/out.txt"))
//        );
//
//
//        System.out.println("原始输入可用字节：" + pis.available());
//        int read = pis.read();
//        System.out.println("读取到字节[pis.read()]：" + read);
//        System.out.println("读取后剩余可用字节：" + pis.available());
//        System.out.println("回推读取到的字节[pis.unread(read)]：" + read);
//        pis.unread(read);
//        System.out.println("回推后可用字节：" + pis.available());


//        PrintWriter printWriter = new PrintWriter("/Users/zhangyanbo/writer.txt");
//        printWriter.println("hello zyb.");
//        printWriter.flush();
//        printWriter.close();
//        Scanner in = new Scanner(new File("/Users/zhangyanbo/writer.txt"));
//
//        while(in.hasNext()){
//            System.out.println(in.next());
//        }
//        Scanner in = new Scanner(new FileInputStream("/Users/zhangyanbo/writer.txt"));
//
//        while(in.hasNext()){
//            System.out.println(in.next());
//        }
//
//        List<String> allLines = Files.readAllLines(Paths.get("/Users/zhangyanbo/allLines.txt"),StandardCharsets.UTF_8);
//        for(String value:allLines){
//            System.out.println(value);
//        }

//        Stream<String> lines = Files.lines(Paths.get("/Users/zhangyanbo/allLines.txt"), StandardCharsets.UTF_8);
//        String collect = lines.collect(Collectors.joining("|"));
//        System.out.println(collect);
//        Scanner in = new Scanner(new FileInputStream("/Users/zhangyanbo/allLines.txt"));
//
//
//        in.useDelimiter("\\PL+");
//        while (in.hasNext()){
//            System.out.println(in.next());
//        }
//        InputStream inputStream = new FileInputStream("/Users/zhangyanbo/allLines.txt");
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//        String line;
//        while ((line = bufferedReader.readLine()) != null){
//            System.out.println(line);
//        }
//
//
//
//        Stream<String> lines = bufferedReader.lines();
//        String collect = lines.collect(Collectors.joining("\n"));
//        System.out.println(collect);

//        DataInputStream dataInputStream = new DataInputStream(new FileInputStream("/Users/zhangyanbo/allLines.txt"));
//        int i = dataInputStream.readInt();
//        System.out.println(i);
//        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream("/Users/zhangyanbo/allLines.txt"));
//        dataOutputStream.writeInt(1234);
//
//        DataInputStream dataInputStream = new DataInputStream(new FileInputStream("/Users/zhangyanbo/allLines.txt"));
//        int i = dataInputStream.readInt();
//        System.out.println(i);
        RandomAccessFile r = new RandomAccessFile("/Users/zhangyanbo/allLines.txt", "r");
        System.out.println(r.readInt());
        System.out.println(r.getFilePointer());

        r.seek(0);
        System.out.println(r.getFilePointer());

        System.out.println(r.readInt());
        System.out.println(r.getFilePointer());
    }
}
