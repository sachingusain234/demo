package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/csv")
public class PublicController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCSVFile(@RequestParam("file") MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file.");
        }

        // Validate MIME type for CSV files
        String contentType = file.getContentType();
        if (!(contentType != null &&
                (contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only CSV files are allowed (MIME type).");
        }

        // Validate file extension
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only .csv files are allowed (file extension).");
        }

        // Step 1: Validate gender column from MultipartFile input stream
        List<String> invalidRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return ResponseEntity.badRequest().body("CSV file is empty.");
            }
            String[] headers = headerLine.split(",");
            int genderIdx = Arrays.asList(headers).indexOf("gender");
            if (genderIdx == -1) {
                return ResponseEntity.badRequest().body("\"gender\" column not found.");
            }

            String line;
            int rowNum = 2; // Because header is line 1
            while ((line = reader.readLine()) != null) {
                // Use comma split. For complex CSVs, consider a CSV parsing library!
                String[] fields = line.split(",");
                if (fields.length <= genderIdx) {
                    invalidRows.add("Row " + rowNum + ": missing gender field.");
                } else {
                    String genderValue = fields[genderIdx].trim().toLowerCase();
                    if (!genderValue.equals("male") && !genderValue.equals("female")) {
                        invalidRows.add("Row " + rowNum + ": invalid gender value '" + fields[genderIdx] + "'.");
                    }
                }
                rowNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read the uploaded file.");
        }

        // If there are invalid gender values, return error and do NOT save the file
        if (!invalidRows.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Invalid gender values found:\n" + String.join("\n", invalidRows));
        }

        // Step 2: Save the validated file to disk
        String uploadDir = "C:\\uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        File savedFile = new File(dir, fileName);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save the file.");
        }

        return ResponseEntity.ok("CSV file uploaded, validated, and saved successfully.");
    }
}
