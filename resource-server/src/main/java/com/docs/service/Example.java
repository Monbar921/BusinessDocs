package com.docs.service;

import com.docs.exceptions.CannotCreateDirException;
import com.docs.exceptions.CannotSaveFileException;
import com.docs.exceptions.NoSuchFieldInDocException;
import com.docs.exceptions.NoSuchTemplateException;
import com.docs.enums.Extension;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class Example {
    @Value("${storage_dir}")
    private String storageDir;
    @Value("${templates_dir_name}")
    private String templatesDir;
    @Value("${examples_dir_name}")
    private String examplesDir;
    @Value("${filename_pattern}")
    private String filenamePattern;

    public void getFilledDocument(String userLogin, String templateName, Map<String, String> data) throws IOException {
        String userDir = storageDir + userLogin + "/";
        try (XWPFDocument document = getTemplate(userDir, templateName)) {
            List<XWPFParagraph> paragraphs = getAllParagraphs(document);
            data.forEach((k, v) -> replaceGivenField(paragraphs, k, v));
            Extension format = Extension.DOCX;
            saveFile(document, userDir, format);
        }
    }

    public void saveTemplate(String userLogin, String templateName, MultipartFile file){

    }

    private XWPFDocument getTemplate(String userDir, String templateName) throws NoSuchTemplateException {
        String pathToTemplate = userDir + templatesDir + templateName;
        Path docAsPath = Paths.get(pathToTemplate);
        try {
            return new XWPFDocument(Files.newInputStream(docAsPath));
        } catch (IOException ignored) {
            throw new NoSuchTemplateException();
        }
    }

    private List<XWPFParagraph> getAllParagraphs(XWPFDocument document) {
        List<XWPFParagraph> paragraphs = new ArrayList<>();
        paragraphs.addAll(document.getParagraphs());
        paragraphs.addAll(readParagraphsFromTables(document));
        return paragraphs;
    }

    private List<XWPFParagraph> readParagraphsFromTables(XWPFDocument document) {
        List<XWPFParagraph> paragraphs = new ArrayList<>();
        List<XWPFTable> tables = document.getTables();
        for (XWPFTable xwpfTable : tables) {
            List<XWPFTableRow> tableRows = xwpfTable.getRows();
            for (XWPFTableRow xwpfTableRow : tableRows) {
                List<XWPFTableCell> tableCells = xwpfTableRow
                        .getTableCells();
                for (XWPFTableCell xwpfTableCell : tableCells) {
                    List<XWPFParagraph> innerParagraphs = xwpfTableCell.getParagraphs();
                    paragraphs.addAll(innerParagraphs);
                }
            }
        }
        return paragraphs;
    }

    private void saveFile(XWPFDocument document, String userDir, Extension format) throws CannotSaveFileException {
        createUserWorkspace(userDir);
        String outputFileName = generateOutputFileName(userDir, format);
        try (FileOutputStream out = new FileOutputStream(outputFileName)) {
            document.write(out);
        } catch (IOException e) {
            throw new CannotSaveFileException();
        }
    }

    private void createUserWorkspace(String userDir) throws CannotCreateDirException {
        Path pathUserDir = Paths.get(userDir);
        Path pathUserExamplesDir = Paths.get(userDir + examplesDir);

        createDir(pathUserDir);
        createDir(pathUserExamplesDir);
    }

    private void createDir(Path path) throws CannotCreateDirException {
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new CannotCreateDirException();
            }
        }
    }

    private String generateOutputFileName(String userDir, Extension extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(filenamePattern);
        return userDir + examplesDir + LocalDateTime.now().format(formatter) + extension.extensionWithDot();
    }

    private void replaceGivenField(List<XWPFParagraph> paragraphs, String fieldToReplace, String replacingData) throws NoSuchFieldInDocException {
        AtomicBoolean isFieldExists = new AtomicBoolean(false);
        paragraphs.forEach(p -> p.getRuns().forEach(r -> {
            String line = r.getText(0);
            if (line != null && line.contains(fieldToReplace)) {
                line = line.replace(fieldToReplace, replacingData);
                isFieldExists.set(true);
            }
            r.setText(line, 0);
        }));
        if (!isFieldExists.get()) {
            throw new NoSuchFieldInDocException();
        }
    }


}
