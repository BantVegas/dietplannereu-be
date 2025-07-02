package com.bantvegas.dietplanner.controller;

import com.bantvegas.dietplanner.dto.DietPlanRequest;
import com.bantvegas.dietplanner.dto.DietPlanResponse;
import com.bantvegas.dietplanner.service.AiPlanService;
import com.bantvegas.dietplanner.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/plan")
public class DietPlanController {

    @Autowired
    private AiPlanService aiPlanService;

    @Autowired
    private PdfService pdfService;

    @PostMapping
    public DietPlanResponse generatePlan(@RequestBody DietPlanRequest request) {
        DietPlanResponse response = aiPlanService.generatePlan(request);

        // Pridá meno do summary ak tam nie je
        String summary = response.getSummary();
        if (request.getName() != null && !request.getName().isEmpty()) {
            if (!summary.toLowerCase().contains(request.getName().toLowerCase())) {
                summary = "7-day meal plan for " + request.getName() + ": " + summary;
            }
        }

        byte[] pdfBytes = pdfService.generatePdf(
                summary,
                response.getDays(),
                response.getShoppingList(),
                request.getName()
        );

        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
        response.setPdf(pdfBase64);

        return response;
    }
}

