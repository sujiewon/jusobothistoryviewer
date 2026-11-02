package com.sujiewon.history.web;

import com.sujiewon.history.config.AppProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AppProperties appProperties;

    public HomeController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tablePrefix", appProperties.getTablePrefix());
        model.addAttribute("maxMonths", appProperties.getMaxMonths());
        return "index";
    }
}
