package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

import app.exception.FieldValidationException;
import app.model.custom.Gender;
import app.model.custom.UserRole;
import app.model.dto.AdminAddUserDto;
import app.model.dto.AdminUpdateUserDto;
import app.model.projection.UserProfileProjection;
import app.service.UserService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    @Autowired
    private UserService service;

    @PostMapping("/add")
    public String addNewUser(@Valid @ModelAttribute("formRequest") AdminAddUserDto request, BindingResult result, RedirectAttributes attributes) {

        if (result.hasErrors()) {
            throw new FieldValidationException("Masukan data user baru dengan benar!",
                    result,
                    "/admin/users/add");
        }

        service.saveNewUser(request);
        attributes.addFlashAttribute("successMsg", "berhasil menambahkan akun");

        return "redirect:/admin/users/add";
    }

    @GetMapping("/add")
    public String getAddNewUserPage(Model model) {
        AdminAddUserDto dto = new AdminAddUserDto();

        model.addAttribute("formRequest", dto);
        return "addNewuser";
    }

    @GetMapping("/")
    public String getAdminUsersPage(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, Model model) {

        Page<UserProfileProjection> pageable = service.findAllUsers(page, size);
        model.addAttribute("users", pageable.getContent());
        model.addAttribute("currentPage", pageable.getNumber());
        model.addAttribute("totalPages", pageable.getTotalPages());
        model.addAttribute("hasNext", pageable.hasNext());
        model.addAttribute("hasPrevious", pageable.hasPrevious());

        return "adminUsers";

    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") UUID deletedId) {

        service.deleteUsers(deletedId);

        return "redirect:/admin/users/";
    }

    @GetMapping("/detail/{id}")
    public String getUserDetail(@PathVariable("id") UUID id, Model model) {

        UserProfileProjection users = service.getUserProfileById(id, "/admin/users/");

        model.addAttribute("user", users);

        return "profileDetails";
    }

    @GetMapping("/update/{id}")
    public String getMethodName(@PathVariable("id") UUID id, Model model) {

        UserProfileProjection projection = service.getUserProfileById(id, "/admin/users/");


        model.addAttribute("formRequest", projection);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("roles", UserRole.values());

        return "updateUsers";
    }

    @PostMapping("/update/{id}")
    public String postMethodName(@Valid @ModelAttribute("formRequest") AdminUpdateUserDto updateUser,
    BindingResult bindingResult,
    UUID id, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            throw new FieldValidationException("Harap Isi data yang baru dengan benar", bindingResult, "/admin/users/update/" + id);
            
        }


        service.updateUserProfile(id, updateUser);
        redirectAttributes.addFlashAttribute("successMsg", "Berhasil memperbaharui user");
        return "redirect:/admin/users/update/"+id;
        
        
    }
    
    

}
