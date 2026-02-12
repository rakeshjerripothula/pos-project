package com.increff.pos.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

public class PdfUtil {

    private static final String INVOICE_DIR = "invoices/";

    public static String save(String base64Pdf) {

        try {
            byte[] decoded = Base64.getDecoder().decode(base64Pdf);

            Files.createDirectories(Paths.get(INVOICE_DIR));

            String fileName = "invoice-" + UUID.randomUUID() + ".pdf";

            Path path = Paths.get(INVOICE_DIR + fileName);

            Files.write(path, decoded);

            return path.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to store invoice PDF", e);
        }
    }
}
