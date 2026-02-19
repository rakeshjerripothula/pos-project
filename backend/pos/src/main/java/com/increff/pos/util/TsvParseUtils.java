package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.form.InventoryUploadForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductUploadForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.increff.pos.util.ConversionUtil.normalize;

public class TsvParseUtils {

    public static List<ProductUploadForm> parseProductTsv(MultipartFile file) {

        List<String[]> rows = parse(file);

        if (rows.size() > 5000) {
            throw new ApiException(ApiStatus.BAD_REQUEST, "Maximum 5000 rows allowed");
        }

        List<ProductUploadForm> forms = new ArrayList<>();

        for (String[] r : rows) {

            ProductUploadForm form = new ProductUploadForm();

            form.setProductName(r.length > 0 ? r[0] : null);
            form.setMrp(parseBigDecimalSafely(r.length > 1 ? r[1] : null));
            form.setClientName(r.length > 2 ? r[2] : null);
            form.setBarcode(r.length > 3 ? r[3] : null);
            form.setImageUrl(r.length > 4 ? r[4] : null);

            if(!Objects.isNull(form.getProductName())){
                form.setProductName(normalize(form.getProductName()));
            }

            if(!Objects.isNull(form.getClientName())){
                form.setClientName(normalize(form.getClientName()));
            }

            if(!Objects.isNull(form.getBarcode())){
                form.setBarcode(normalize(form.getBarcode()));
            }

            forms.add(form);
        }

        return forms;
    }

    public static List<InventoryUploadForm> parseInventoryTsv(MultipartFile file) {

        List<String[]> rows = parse(file);

        if (rows.size() > 5000) {
            throw new ApiException(
                    ApiStatus.BAD_REQUEST,
                    "Maximum 5000 rows allowed"
            );
        }

        List<InventoryUploadForm> forms = new ArrayList<>();

        for (String[] r : rows) {

            InventoryUploadForm form = new InventoryUploadForm();

            form.setProductName(r.length > 0 ? r[0] : null);

            form.setQuantity(parseIntegerSafely(
                    r.length > 1 ? r[1] : null
            ));

            forms.add(form);
        }

        return forms;
    }

    private static List<String[]> parse(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

            return br.lines()
                    .skip(1) // skip header
                    .map(line -> line.split("\t", -1))
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse TSV file");
        }
    }

    private static BigDecimal parseBigDecimalSafely(String value) {
        try {
            return value == null ? null : new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parseIntegerSafely(String value) {
        try {
            return value == null ? null : Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

}
