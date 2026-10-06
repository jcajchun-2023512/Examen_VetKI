package com.jcajchun_2023512.VetKI.security;

import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilidades para obtener el usuario autenticado del contexto de seguridad.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new UnauthorizedException("Usuario no autenticado en el sistema");
        }
        return usuario;
    }

    public static Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }
}
