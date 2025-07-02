package com.bantvegas.dietplanner.model;

import java.util.List;

public class DayPlan {
    private int day;           // deň v mesiaci (1-30)
    private List<Meal> meals;  // zoznam 5 jedál na deň

    // --- Gettery a settery ---
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public List<Meal> getMeals() { return meals; }
    public void setMeals(List<Meal> meals) { this.meals = meals; }
}

