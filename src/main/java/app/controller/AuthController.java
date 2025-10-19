package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;

import app.exception.FieldValidationException;
import app.model.dto.LoginRequest;
import app.model.dto.SignUpRequest;
import app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @PostMapping(value = "/login")
    public String postLogin(@Valid @ModelAttribute("formRequest") LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpServletResponse response,
            HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException("Harap isi data dengan benar", bindingResult, "/login");
        }

        service.loginService(loginRequest, response, request);

        return "redirect:/home";
    }

    @PostMapping(value = "/sign-up")
    public String signUp(@Valid @ModelAttribute("formRequest") SignUpRequest entity, BindingResult bindingResult,
            RedirectAttributes redAttrs) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException("Harap isi data dengan benar", bindingResult, "/sign-up");
        }

        service.signUpRequest(entity);
        redAttrs.addFlashAttribute("successMsg", "berhasil membuat akun! silahkan login");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logoutRequest(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redAttrs) {
        service.logoutService(request, response);

        redAttrs.addFlashAttribute("successMsg", "berhasil logout");
        return "redirect:/login";
    }

}
