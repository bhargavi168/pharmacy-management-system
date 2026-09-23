package com.pharmacy.controller;

import com.pharmacy.service.BillingService;
import com.pharmacy.service.MedicineService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final MedicineService medicineService;
    private final BillingService billingService;

    public DashboardController(MedicineService medicineService, BillingService billingService) {
        this.medicineService = medicineService;
        this.billingService = billingService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalMedicines", medicineService.countAll());
        model.addAttribute("lowStockCount", medicineService.countLowStock());
        model.addAttribute("expiredCount", medicineService.countExpired());
        model.addAttribute("todaySalesCount", billingService.getTodaySalesCount());
        model.addAttribute("todaySalesAmount", billingService.getTodaySalesAmount());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        return "dashboard";
    }
}
