package com;

import java.io.*;

public class Test {
    public static void main(String[] args) throws IOException {
        File file = new File("./src/file");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getCanonicalPath());
    }
    
}
