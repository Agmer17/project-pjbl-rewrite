package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.exception.FieldValidationException;
import app.model.custom.Gender;
import app.model.dto.UpdateProfileRequest;
import app.model.projection.UserProfileProjection;
import app.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/my-profile")
    public String getMyProfile(@SessionAttribute("creds") Claims creds, Model model) {
        UUID userId = UUID.fromString(creds.get("id", String.class));

        UserProfileProjection userProfile = userService.getUserProfileById(userId);

        model.addAttribute("user", userProfile);
        model.addAttribute("formRequest", new UpdateProfileRequest(userProfile.getUsername(),
                userProfile.getEmail(), userProfile.getFullName(),
                null, userProfile.getPhoneNumber(),
                Gender.valueOf(userProfile.getGender())));

        return "myProfile";
    }

    @GetMapping("/update-my-profile")
    public String updateMyProfilePage(@SessionAttribute("creds") Claims creds, Model model) {

        UUID userId = UUID.fromString(creds.get("id", String.class));

        UserProfileProjection profileProjection = userService.getUserProfileById(userId);

        model.addAttribute("formRequest", profileProjection);
        model.addAttribute("genders", Gender.values());

        return "updateProfile";
    }

    @PostMapping("/update-my-profile")
    public String updateMyProfile(@SessionAttribute("creds") Claims creds,
            @Valid @ModelAttribute("formRequest") UpdateProfileRequest updateRequest,
            BindingResult bindRes,
            RedirectAttributes redAttrs) {

        if (bindRes.hasErrors()) {
            throw new FieldValidationException("Data yang dimasukkan tidak valid", bindRes, "/user/update-my-profile");
        }
        UUID userId = UUID.fromString(creds.get("id", String.class));

        userService.updateUserProfile(userId, updateRequest);

        redAttrs.addFlashAttribute("successMessage", "profil berhasil diperbaharui");

        return "redirect:/user/my-profile";

    };

}
