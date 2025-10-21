package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import app.exception.FieldValidationException;
import app.model.dto.LoginRequest;
import app.model.dto.SignUpRequest;
import app.model.dto.UpdatePasswordRequest;
import app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthController {
    @Autowired
    private AuthService service;

    @PostMapping(value = "/api/auth/login")
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

    @PostMapping(value = "/api/auth/sign-up")
    public String signUp(@Valid @ModelAttribute("formRequest") SignUpRequest entity, BindingResult bindingResult,
            RedirectAttributes redAttrs) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException("Harap isi data dengan benar", bindingResult, "/sign-up");
        }

        service.signUpRequest(entity);
        redAttrs.addFlashAttribute("successMsg", "berhasil membuat akun! silahkan login");
        return "redirect:/login";
    }

    @GetMapping("/api/auth/logout")
    public String logoutRequest(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redAttrs) {
        service.logoutService(request, response);

        redAttrs.addFlashAttribute("successMsg", "berhasil logout");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String getForgotPasswordPage() {
        return "forgotPassword";
    }

    @PostMapping("/api/auth/get-reset-password-code")
    public String postForgotPassword(HttpServletRequest request, @RequestParam String usernameOrEmail,
            RedirectAttributes redAttrs) {

        String baseUrl = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toString();

        service.sendRequetsCode(baseUrl, usernameOrEmail);

        redAttrs.addFlashAttribute("successMsg", "silahkan cek email anda untuk melanjutkan proses reset password");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String getUpdatePasswordPage(@RequestParam(name = "token", required = true) String token, Model model) {
        UpdatePasswordRequest UpdatePasswordRequest = new UpdatePasswordRequest();


        UpdatePasswordRequest.setToken(token);

        model.addAttribute("formRequest", UpdatePasswordRequest);

        return "resetPassword";
    }

    @PostMapping("/api/auth/update-my-password")
    public String postMethodName(@Valid @ModelAttribute UpdatePasswordRequest updatePasswordReqeust, 
    BindingResult bindingResult,
    RedirectAttributes redAttrs) {
        if (bindingResult.hasErrors()) {
            throw new FieldValidationException("password tidak valid", bindingResult, "/reset-password");
        }
        service.updateUserPassword(updatePasswordReqeust);


        redAttrs.addFlashAttribute("successMsg", "berhasil mengupdate password!");
        
        return "redirect:/login";
    }
    
    

    

}
