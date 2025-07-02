package com.bantvegas.dietplanner.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public class Meal {
    @JsonAlias("mealName")
    private String name;
    private int calories;
    private List<Ingredient> ingredients;
    private String instructions; // optional

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}

