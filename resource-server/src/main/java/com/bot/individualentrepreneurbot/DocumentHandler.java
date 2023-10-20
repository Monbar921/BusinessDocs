package com.bot.individualentrepreneurbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.RuleBasedNumberFormat;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@PropertySource("application.properties")
public class DocumentHandler {
    @Value("${file.input}")
    private String inputFileName;
    @Value("${file.output}")
    private String outputFileName;
    private String date;
    private int counter;
    private String requisites;
    private int hours;
    private int cost_per_hour;

    public void getDocument(String date, int counter, String requisites, int hours, int cost_per_hour) throws IOException {
        deleteFile();
        this.date = date;
        this.counter = counter;
        this.requisites = requisites;
        this.hours = hours;
        this.cost_per_hour = cost_per_hour;
        updateDocument();
    }

    public void updateDocument()
            throws IOException {
        try (XWPFDocument doc = new XWPFDocument(
                Files.newInputStream(Paths.get(inputFileName)))
        ) {
            List<XWPFParagraph> xwpfParagraphList = doc.getParagraphs();
            readAllParagraphsFromDoc(xwpfParagraphList, counter, String.valueOf(hours * cost_per_hour));

            List<XWPFTable> tables = doc.getTables();
            readAllTablesFromDoc(tables, requisites, hours, cost_per_hour);
            // save the docs
            try (FileOutputStream out = new FileOutputStream(outputFileName)) {
                doc.write(out);
            }
        }
    }

    private void readAllParagraphsFromDoc(List<XWPFParagraph> xwpfParagraphList, int counter, String sum) {
        for (XWPFParagraph xwpfParagraph : xwpfParagraphList) {
            for (XWPFRun xwpfRun : xwpfParagraph.getRuns()) {
                String docText = xwpfRun.getText(0);
                System.out.println(docText);
                //replacement and setting position
                if (docText != null) {
                    docText = paragraphFieldsToReplace(docText, counter, sum);
                }
                xwpfRun.setText(docText, 0);
            }
        }
    }

    private String paragraphFieldsToReplace(String docText, int counter, String sum) {
        docText = docText.replace("counter", String.valueOf(counter));
        if (docText.matches(".*date.*")) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//            LocalDateTime dateTime = LocalDateTime.now();
            docText = docText.replace("date", date);
        }
        docText = docText.replace("sum", sum);
        if (docText.matches(".*v.*")) {
            RuleBasedNumberFormat nf = new RuleBasedNumberFormat(Locale.forLanguageTag("ru"),
                    RuleBasedNumberFormat.SPELLOUT);
            String result = nf.format(Integer.parseInt(sum));
            docText = docText.replace("v", result.substring(0, 1).toUpperCase() + result.substring(1));
        }
        return docText;
    }

    private void readAllTablesFromDoc(List<XWPFTable> tables, String requisites, int hours, int cost) {
        for (XWPFTable xwpfTable : tables) {
            List<XWPFTableRow> tableRows = xwpfTable.getRows();
            for (XWPFTableRow xwpfTableRow : tableRows) {
                List<XWPFTableCell> tableCells = xwpfTableRow
                        .getTableCells();
                for (XWPFTableCell xwpfTableCell : tableCells) {
                    List<XWPFParagraph> insideTable = xwpfTableCell.getParagraphs();
                    for (XWPFParagraph inPar : insideTable) {
                        for (XWPFRun xwpfRun : inPar.getRuns()) {
                            String docText = xwpfRun.getText(0);
                            System.out.println(docText);
                            if (docText != null) {
                                docText = tableFieldsToReplace(docText, requisites, hours, cost);
                            }
                            xwpfRun.setText(docText, 0);
                        }
                    }
                }
            }
        }
    }

    private String tableFieldsToReplace(String docText, String requisites, int hours, int cost) {
        docText = docText.replace("requisites", requisites);
        docText = docText.replace("hours", String.valueOf(hours));
        docText = docText.replace("cost", String.valueOf(cost));
        docText = docText.replace("sum", String.valueOf(cost * hours));
        return docText;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void deleteFile() throws IOException {
        Path fileToDeletePath = Paths.get(outputFileName);
        Files.deleteIfExists(fileToDeletePath);
    }
}
