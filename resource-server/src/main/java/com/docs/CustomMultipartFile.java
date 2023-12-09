package com.docs;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomMultipartFile implements MultipartFile {
    private byte[] input;
    private String originalFilename;

    public CustomMultipartFile(String pathToFile) {
        Path path = Paths.get(pathToFile);
        this.originalFilename = pathToFile;
        try{
            this.input = Files.readAllBytes(path);
        }catch (IOException e){

        }


    }

    @Override
    public String getName() {
        return this.originalFilename;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return null;
    }
    @Override
    public boolean isEmpty() {
        return input == null || input.length == 0;
    }

    @Override
    public long getSize() {
        return input.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return input;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(input);
    }

    @Override
    public void transferTo(File destination) throws IOException, IllegalStateException {
        try(FileOutputStream fos = new FileOutputStream(destination)) {
            fos.write(input);
        }
    }
}