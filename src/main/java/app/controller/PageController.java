package app.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import app.model.dto.LoginRequest;
import app.model.dto.SignUpRequest;


@Controller
public class PageController {

    @GetMapping("/")
    public String homePageRedirect() {
        return "redirect:/home";
    }
    

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
    

    @GetMapping("/login")
    public String loginPage(Model model) {

        if (!model.containsAttribute("formRequest")) {
        model.addAttribute("formRequest", new LoginRequest());
    }
        
        return "login";
    }

    @GetMapping("/sign-up")
    public String signUpPage(Model model) {
        if (!model.containsAttribute("formRequest")) {
            model.addAttribute("formRequest", new SignUpRequest());
        }
        return "signup";
    }
    
    
    
    
}
