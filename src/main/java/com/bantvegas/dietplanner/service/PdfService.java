package com.bantvegas.dietplanner.service;

import com.bantvegas.dietplanner.model.DayPlan;
import com.bantvegas.dietplanner.model.Meal;
import com.bantvegas.dietplanner.model.Ingredient;
import com.bantvegas.dietplanner.model.ShoppingItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    private static final float MARGIN = 48;
    private static final float TOP_MARGIN = 56; // ~2cm from top
    private static final float START_Y = PDRectangle.LETTER.getHeight() - TOP_MARGIN;
    private static final float LINE_HEIGHT = 19;
    private static final float SECTION_SPACE = 30;

    public byte[] generatePdf(String summary,
                              List<DayPlan> days,
                              List<ShoppingItem> shoppingList,
                              String name) {
        try (PDDocument document = new PDDocument()) {
            // ===== ÚVODNÍ STRANA =====
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            float y = START_Y;

            // Titulek
            content.setNonStrokingColor(new Color(24, 58, 107));
            content.setFont(PDType1Font.HELVETICA_BOLD, 32);
            drawText(content, "7-DAY MEAL PLAN", MARGIN, y);
            y -= LINE_HEIGHT + 12;

            // Jméno
            if (name != null && !name.isBlank()) {
                content.setNonStrokingColor(new Color(24, 58, 107));
                content.setFont(PDType1Font.HELVETICA_BOLD, 22);
                drawText(content, "for " + name, MARGIN, y);
                y -= LINE_HEIGHT + 8;
            }

            // Summary
            content.setNonStrokingColor(new Color(34, 34, 34));
            content.setFont(PDType1Font.HELVETICA_BOLD, 17);
            drawText(content, "Summary:", MARGIN, y);
            y -= LINE_HEIGHT;
            content.setNonStrokingColor(new Color(68, 68, 68));
            content.setFont(PDType1Font.HELVETICA, 13);
            for (String lineText : wrapText(summary, 95)) {
                drawText(content, lineText, MARGIN, y);
                y -= LINE_HEIGHT - 2;
            }
            y -= SECTION_SPACE;

            // Welcome blok
            content.setNonStrokingColor(new Color(24, 58, 107));
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            drawText(content, "Welcome to your personalized nutrition journey!", MARGIN, y);
            y -= LINE_HEIGHT;
            content.setNonStrokingColor(new Color(85, 85, 85));
            content.setFont(PDType1Font.HELVETICA, 12);
            String welcome = "This plan was created just for you, based on your information and goals. " +
                    "Inside, you'll find a complete 7-day menu, shopping list, and practical tips for every day. " +
                    "All ingredients are calculated in US units and the plan is designed for your personal success. " +
                    "Stay consistent, follow the meal plan, and enjoy your progress – your journey starts now!";
            for (String wline : wrapText(welcome, 95)) {
                drawText(content, wline, MARGIN, y);
                y -= LINE_HEIGHT - 2;
            }
            content.close();

            // ===== JÍDELNÍ PLÁNY =====
            for (DayPlan day : days) {
                page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                content = new PDPageContentStream(document, page);
                y = START_Y;

                // Den
                content.setNonStrokingColor(new Color(243, 114, 44));
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                drawText(content, "Day " + day.getDay(), MARGIN, y);
                y -= LINE_HEIGHT + 2;

                // Každé jídlo
                for (Meal meal : day.getMeals()) {
                    // Název
                    content.setNonStrokingColor(new Color(24, 58, 107));
                    content.setFont(PDType1Font.HELVETICA_BOLD, 13);
                    String title = capitalize(meal.getName());
                    drawText(content, title, MARGIN + 10, y);

                    // Šířka názvu v bodech
                    float titleWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000f) * 13;
                    float calX = MARGIN + 10 + titleWidth + 20;

                    // Kalorie
                    content.setNonStrokingColor(new Color(26, 159, 83));
                    content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    drawText(content, "(" + meal.getCalories() + " kcal):", calX, y);
                    y -= LINE_HEIGHT;

                    // Ingredience
                    for (Ingredient ing : meal.getIngredients()) {
                        content.setNonStrokingColor(new Color(85, 85, 85));
                        content.setFont(PDType1Font.HELVETICA, 11);
                        drawText(content, "• " + capitalize(ing.getName()) + ":", MARGIN + 30, y);
                        content.setNonStrokingColor(new Color(243, 114, 44));
                        content.setFont(PDType1Font.HELVETICA_BOLD, 11);
                        drawText(content, ing.getAmount(), MARGIN + 180, y);
                        y -= LINE_HEIGHT - 6;
                    }

                    // Mezera před instrukcemi
                    y -= LINE_HEIGHT;

                    // Instrukce
                    content.setNonStrokingColor(new Color(24, 58, 107));
                    content.setFont(PDType1Font.HELVETICA_OBLIQUE, 11);
                    drawText(content, "Instructions: " + meal.getInstructions(), MARGIN + 30, y);
                    y -= LINE_HEIGHT + 4;
                }
                content.close();
            }

            // ===== NÁKUPNÍ SEZNAM =====
            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = START_Y;

            content.setNonStrokingColor(new Color(24, 58, 107));
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            drawText(content, "Shopping List (US units for 7 days):", MARGIN, y);
            y -= LINE_HEIGHT + 4;

            for (ShoppingItem item : shoppingList) {
                if (y < LINE_HEIGHT * 2) {
                    content.close();
                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = START_Y;
                }
                content.setNonStrokingColor(new Color(34, 34, 34));
                content.setFont(PDType1Font.HELVETICA, 12);
                drawText(content, "• " + capitalize(item.getName()) + ": " + item.getAmount(), MARGIN + 10, y);
                y -= LINE_HEIGHT;
            }
            content.close();

            // Uložení
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void drawText(PDPageContentStream cs, String text, float x, float y) throws java.io.IOException {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private static java.util.List<String> wrapText(String text, int maxLen) {
        var lines = new java.util.ArrayList<String>();
        if (text == null) return lines;
        for (String part : text.split("\n")) {
            while (part.length() > maxLen) {
                int space = part.lastIndexOf(" ", maxLen);
                if (space < 0) space = maxLen;
                lines.add(part.substring(0, space));
                part = part.substring(space).trim();
            }
            if (!part.isEmpty()) lines.add(part);
        }
        return lines;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}





