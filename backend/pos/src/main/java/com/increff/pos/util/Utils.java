package com.increff.pos.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class Utils {

    public static String normalize(String value) {
        return value.trim().toLowerCase();
    }

    public static List<String[]> parse(MultipartFile file) {
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
}
