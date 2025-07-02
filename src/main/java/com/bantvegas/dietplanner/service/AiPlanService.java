package com.bantvegas.dietplanner.service;

import com.bantvegas.dietplanner.dto.DietPlanRequest;
import com.bantvegas.dietplanner.dto.DietPlanResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class AiPlanService {

    @Autowired
    private OpenAiService openAiService;

    public DietPlanResponse generatePlan(DietPlanRequest request) {
        // Height a weight už očakávame v US jednotkách (inch, lbs)
        int heightInches = request.getHeight();
        int weightLbs = request.getWeight();

        String prompt = String.format(
                "Generate a detailed 7-day meal plan in ENGLISH for a %s named %s, age %d, height %d inches, weight %d lbs, goal: %s, preferences: %s, allergies: %s. " +
                        "Each day must have exactly 5 meals named Breakfast, Snack, Lunch, Snack, Dinner. " +
                        "Each meal must be a real cooked dish (e.g. 'Vegetable stir-fry with tofu'), not just a list of foods. " +
                        "For each meal include these JSON fields: " +
                        "\"name\" (dish name), \"calories\" (integer kcal), \"ingredients\" (array of {\"name\":..., \"amount\":...} in US units), and \"instructions\" (1-2 sentences). " +
                        "Also include a \"shoppingList\" array of {\"name\":..., \"amount\":...} in US units. " +
                        "Use US units only (oz, cups, fl oz, pieces, lbs, inches). " +
                        "Return ONLY a valid single JSON object, no markdown, no extra text. " +
                        "Exact format:\n" +
                        "{\n" +
                        "  \"summary\": \"...\",\n" +
                        "  \"days\": [\n" +
                        "    { \"day\": 1, \"meals\": [\n" +
                        "        { \"name\": \"Chicken Caesar Salad\", \"calories\": 350, \"ingredients\": [ { \"name\": \"Chicken breast\", \"amount\": \"4 oz\" } ], \"instructions\": \"Grill the chicken and serve over lettuce.\" },\n" +
                        "        ... 4 more meals ...\n" +
                        "    ] },\n" +
                        "    ... 6 more days ...\n" +
                        "  ],\n" +
                        "  \"shoppingList\": [\n" +
                        "    { \"name\": \"Chicken breast\", \"amount\": \"28 oz\" },\n" +
                        "    ...\n" +
                        "  ]\n" +
                        "}\n",
                request.getGender(),
                request.getName() != null ? request.getName() : "",
                request.getAge(),
                heightInches,
                weightLbs,
                request.getGoal(),
                request.getPreferences() == null ? "" : String.join(", ", request.getPreferences()),
                request.getAllergies() == null ? "" : String.join(", ", request.getAllergies())
        );

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                // Zavolanie OpenAI
                String aiJson = openAiService.getDietPlan(prompt);
                if (aiJson == null || aiJson.isBlank())
                    throw new RuntimeException("AI returned empty response");

                // Vystrihnúť od prvej { do poslednej }
                int first = aiJson.indexOf("{"), last = aiJson.lastIndexOf("}");
                String clean = aiJson.substring(first, last + 1)
                        .replaceAll("(?m)^\\s*//.*", "")
                        .replaceAll(",\\s*([}\\]])", "$1")
                        .trim();

                ObjectMapper mapper = new ObjectMapper();
                DietPlanResponse response = mapper.readValue(clean, DietPlanResponse.class);

                validatePlan(response);
                return response;

            } catch (Exception e) {
                if (attempt == 3) {
                    System.err.println("AI RAW OUTPUT (fail!):\n" + e.getMessage());
                    throw new RuntimeException("AI parsing failed after 3 attempts: " + e.getMessage(), e);
                }
            }
        }

        throw new RuntimeException("AI failed after 3 attempts");
    }

    private void validatePlan(DietPlanResponse response) {
        if (response.getDays() == null || response.getDays().size() != 7)
            throw new RuntimeException("Expected 7 days, got " +
                    (response.getDays() == null ? 0 : response.getDays().size()));

        Set<Integer> seen = new HashSet<>();
        response.getDays().forEach(day -> {
            if (!seen.add(day.getDay()))
                throw new RuntimeException("Duplicate day: " + day.getDay());
            if (day.getMeals() == null || day.getMeals().size() != 5)
                throw new RuntimeException("Day " + day.getDay() + " must have 5 meals");
            day.getMeals().forEach(meal -> {
                if (meal.getName() == null || meal.getName().isEmpty())
                    throw new RuntimeException("Meal without name in day " + day.getDay());
            });
        });

        if (response.getShoppingList() == null || response.getShoppingList().isEmpty())
            throw new RuntimeException("Missing shopping list");
    }
}



