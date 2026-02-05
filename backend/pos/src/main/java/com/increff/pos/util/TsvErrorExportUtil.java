package com.increff.pos.util;

import com.increff.pos.model.data.TsvUploadError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TsvErrorExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static byte[] exportErrorsToTsv(List<TsvUploadError> errors, String uploadType) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write("Row Number\tOriginal Data\tError Message\n");
            
            for (TsvUploadError error : errors) {
                writer.write(error.getRowNumber() != null ? error.getRowNumber().toString() : "");
                writer.write("\t");
                
                if (error.getOriginalData() != null) {
                    String originalData = String.join(" | ", error.getOriginalData());
                    writer.write(escapeTsvField(originalData));
                } else {
                    writer.write("");
                }
                writer.write("\t");
                
                writer.write(error.getErrorMessage() != null ? escapeTsvField(error.getErrorMessage()) : "");
                writer.write("\n");
            }
        }
        
        return outputStream.toByteArray();
    }

    public static String generateErrorFilename(String uploadType) {
        return uploadType + "-upload-errors-" + LocalDateTime.now().format(DATE_FORMATTER) + ".tsv";
    }

    private static String escapeTsvField(String field) {
        if (field == null) {
            return "";
        }
        
        if (field.contains("\t") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }
}
