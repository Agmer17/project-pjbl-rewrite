package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import app.model.projection.UserProfileProjection;
import app.service.UserService;
import io.jsonwebtoken.Claims;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService service;

    @GetMapping("/")
    public String getAdminDashboard(@SessionAttribute Claims creds, Model model) {

        UUID adminId = UUID.fromString(creds.get("id", String.class));

        UserProfileProjection currentAdminData = service.getUserProfileById(adminId, "/login");

        model.addAttribute("currentAdmin", currentAdminData);

        return "adminDashboard";

    }


}
