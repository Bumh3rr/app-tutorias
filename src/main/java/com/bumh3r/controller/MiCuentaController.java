package com.bumh3r.controller;

import com.bumh3r.entity.Usuario;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("mi-cuenta")
public class MiCuentaController {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String verMiCuenta(Authentication auth, Model model) {
        Usuario u = usuarioRepository.findByUsername(auth.getName()).orElse(null);
        model.addAttribute("usuario", u);
        return "mi-cuenta";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(Authentication auth,
                                   @RequestParam String passwordActual,
                                   @RequestParam String passwordNueva,
                                   @RequestParam String passwordConfirm,
                                   RedirectAttributes attrs) {
        Usuario u = usuarioRepository.findByUsername(auth.getName()).orElse(null);
        if (u == null) {
            attrs.addFlashAttribute("msg_error", "Sesión inválida.");
            return "redirect:/mi-cuenta";
        }
        if (!passwordEncoder.matches(passwordActual, u.getPasswordHash())) {
            attrs.addFlashAttribute("msg_error", "La contraseña actual es incorrecta.");
            return "redirect:/mi-cuenta";
        }
        if (!passwordNueva.equals(passwordConfirm)) {
            attrs.addFlashAttribute("msg_error", "La nueva contraseña y la confirmación no coinciden.");
            return "redirect:/mi-cuenta";
        }
        if (passwordNueva.length() < 6) {
            attrs.addFlashAttribute("msg_error", "La nueva contraseña debe tener al menos 6 caracteres.");
            return "redirect:/mi-cuenta";
        }
        usuarioService.cambiarPassword(u.getId(), passwordNueva);
        attrs.addFlashAttribute("msg_success", "Contraseña actualizada correctamente.");
        return "redirect:/mi-cuenta";
    }
}
