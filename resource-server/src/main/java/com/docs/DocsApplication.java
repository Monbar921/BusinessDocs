package com.docs;

import com.docs.service.Example;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;


@SpringBootApplication
public class DocsApplication {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/path/to/the/file.txt");
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
        }


        String doc = "Act.docx";
        ApplicationContext context = SpringApplication.run(DocsApplication.class, args);
        Example example = context.getBean(Example.class);
        example.getFilledDocument("1234", doc, Map.of("#1#", "ss", "#2#", "aa","#3#", "dd"));




    }

}
