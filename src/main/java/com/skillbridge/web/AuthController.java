package com.skillbridge.web;

import com.skillbridge.model.User;
import com.skillbridge.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/")
    String home() {
        return "index";
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/demo-login")
    String demoLogin(@RequestParam(defaultValue = "priya@skillbridge.edu") String email,
                     @RequestParam(defaultValue = "/dashboard") String target,
                     HttpServletRequest request,
                     HttpServletResponse response) {
        if (users.existsByEmail(email)) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        }
        return "redirect:" + (target.startsWith("/") ? target : "/dashboard");
    }

    @GetMapping("/register")
    String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    String register(@RequestParam String name,
                    @RequestParam String email,
                    @RequestParam String password,
                    @RequestParam(required = false) String department,
                    @RequestParam(required = false) String preferredRole,
                    @RequestParam(required = false) String bio,
                    Model model) {
        if (users.existsByEmail(email != null ? email.trim().toLowerCase() : "")) {
            model.addAttribute("error", "That email address is already registered.");
            return "register";
        }
        User u = new User();
        u.setName(name != null ? name.trim() : "");
        u.setEmail(email != null ? email.trim().toLowerCase() : "");
        u.setDepartment(department != null ? department.trim() : "");
        u.setPreferredRole(preferredRole != null ? preferredRole.trim() : "Developer");
        u.setBio(bio != null ? bio.trim() : "");
        u.setPassword(encoder.encode(password));
        users.save(u);
        return "redirect:/login?registered=true";
    }
}
