package com.bantvegas.dietplanner.dto;

import com.bantvegas.dietplanner.model.DayPlan;
import com.bantvegas.dietplanner.model.ShoppingItem;
import java.util.List;

public class DietPlanResponse {
    private String summary;
    private List<DayPlan> days;
    private List<ShoppingItem> shoppingList;
    private String pdf; // <--- DÔLEŽITÉ: String (BASE64)

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<DayPlan> getDays() { return days; }
    public void setDays(List<DayPlan> days) { this.days = days; }
    public List<ShoppingItem> getShoppingList() { return shoppingList; }
    public void setShoppingList(List<ShoppingItem> shoppingList) { this.shoppingList = shoppingList; }
    public String getPdf() { return pdf; }
    public void setPdf(String pdf) { this.pdf = pdf; }
}

