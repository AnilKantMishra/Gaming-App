package com.community.api.endpoint.avisoft.controller.Color;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping("/image")
public class ColorExtractorController {

    @PostMapping("/upload")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> colorMap = new HashMap<>();
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            List<String> allColors = extractAllColors(image);
            List<String> allColorNames = convertHexToColorNames(allColors);
            String exteriorColor = determineExteriorColor(allColors);
            String interiorColor = determineInteriorColor(allColors);

            colorMap.put("allColors", allColors);
            colorMap.put("allColorNames", allColorNames);
            colorMap.put("exteriorColor", exteriorColor);
            colorMap.put("interiorColor", interiorColor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return colorMap;
    }

    private List<String> extractAllColors(BufferedImage image) {
        Map<String, Integer> colorCounts = new HashMap<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                String colorHex = String.format("#%06x", rgb & 0xFFFFFF);
                colorCounts.put(colorHex, colorCounts.getOrDefault(colorHex, 0) + 1);
            }
        }
        return new ArrayList<>(colorCounts.keySet());
    }

    private String determineExteriorColor(List<String> allColors) {
        return allColors.stream()
                .filter(this::isLightColor)
                .findFirst()
                .orElse(allColors.isEmpty() ? null : allColors.get(0));
    }

    private String determineInteriorColor(List<String> allColors) {
        return allColors.stream()
                .filter(this::isDarkColor)
                .findFirst()
                .orElse(allColors.isEmpty() ? null : allColors.get(allColors.size() - 1));
    }

    private boolean isLightColor(String colorHex) {
        Color color = Color.decode(colorHex);
        double brightness = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        return brightness > 186;
    }

    private boolean isDarkColor(String colorHex) {
        return !isLightColor(colorHex);
    }

    private List<String> convertHexToColorNames(List<String> hexColors) {
        List<String> colorNames = new ArrayList<>();
        for (String hex : hexColors) {
            colorNames.add(hexToColorName(hex));
        }
        return colorNames;
    }

    private String hexToColorName(String hex) {
        // A simple mapping of hex codes to color names
        Map<String, String> colorMap = new HashMap<>() {{
            put("#f00f1d", "Bright Red");
            put("#ed0f1c", "Bright Red");
            put("#ec0e1b", "Bright Red");
            put("#ee101d", "Bright Red");
            put("#ef111e", "Bright Red");
            // Add more mappings as needed
            put("#ffffff", "White");
            put("#000000", "Black");
            put("#ff0000", "Red");
            put("#00ff00", "Green");
            put("#0000ff", "Blue");
            put("#ffff00", "Yellow");
            put("#ff00ff", "Magenta");
            put("#00ffff", "Cyan");
        }};
        return colorMap.getOrDefault(hex, "Unknown Color");
    }
}