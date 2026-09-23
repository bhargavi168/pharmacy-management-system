package com.pharmacy.controller;

import com.pharmacy.model.Medicine;
import com.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public String listMedicines(@RequestParam(required = false) String name,
                               @RequestParam(required = false) String category,
                               Model model) {
        List<Medicine> medicines = medicineService.searchMedicines(name, category);
        model.addAttribute("medicines", medicines);
        model.addAttribute("name", name);
        model.addAttribute("category", category);
        model.addAttribute("categories", getCategories());
        return "medicines";
    }

    @GetMapping("/add")
    public String showAddMedicineForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        return "medicine-form";
    }

    @PostMapping("/save")
    public String saveMedicine(@Valid @ModelAttribute("medicine") Medicine medicine,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "medicine-form";
        }

        medicineService.save(medicine);
        return "redirect:/medicines";
    }

    @GetMapping("/edit/{id}")
    public String showEditMedicineForm(@PathVariable Long id, Model model) {
        Medicine medicine = medicineService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid medicine Id: " + id));
        model.addAttribute("medicine", medicine);
        return "medicine-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable Long id) {
        medicineService.deleteById(id);
        return "redirect:/medicines";
    }

    private List<String> getCategories() {
        return List.of("Pain Relief", "Antibiotic", "Allergy", "Supplement", "Digestive", "Diabetes", "Respiratory", "Thyroid");
    }
}
