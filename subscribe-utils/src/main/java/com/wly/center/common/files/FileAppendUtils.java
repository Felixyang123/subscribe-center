package com.wly.center.common.files;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.*;
import java.io.IOException;
import java.util.List;

@Slf4j
public class FileAppendUtils {
    
    /**
     * 追加单行文本到文件
     */
    public static void appendLine(Path filePath, String line) throws IOException {
        String content = line + System.lineSeparator();
        Files.write(filePath, content.getBytes(), 
                   StandardOpenOption.CREATE, 
                   StandardOpenOption.APPEND);
    }
    
    /**
     * 追加多行文本到文件
     */
    public static void appendLines(Path filePath, List<String> lines) throws IOException {
        try (var writer = Files.newBufferedWriter(filePath, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
    /**
     * 追加文本并返回是否成功
     */
    public static boolean appendSafe(Path filePath, String content) {
        try {
            Files.writeString(filePath, content, 
                             StandardOpenOption.CREATE, 
                             StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            log.error("Failed to append content to file: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 使用示例
     */
    public static void main(String[] args) {
        Path logFile = Paths.get("application.log");
        
        // 示例1: 追加单行
        try {
            appendLine(logFile, "2024-01-15 10:30:00 - Application started");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 示例2: 安全追加（不抛出异常）
        boolean success = appendSafe(logFile, "User login successful");
        if (success) {
            System.out.println("日志记录成功");
        }
        
        // 示例3: 使用 Java 11+ 的 Files.writeString
        try {
            Files.writeString(logFile, "Another log entry\n", 
                             StandardOpenOption.CREATE, 
                             StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}