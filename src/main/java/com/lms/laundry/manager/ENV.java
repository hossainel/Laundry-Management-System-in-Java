package com.lms.laundry.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class ENV {
    public ENV() {}
    public static String getValue(String env_value) {
        String filePath = "src/main/.env";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Optional<String> result = reader.lines()
                    .filter(line -> line.contains("="))
                    .map(line -> line.split("=", 2))
                    .filter(parts -> parts[0].equals(env_value))
                    .map(parts -> parts[1].replace("\n", "").replace("\r", ""))
                    .findFirst();
            return result.orElse(null);
        } catch (IOException e) {
            P.pf("ENV File not found or unreadable!");
            return null;
        }
    }
}
