package com.jcajchun_2023512.VetKI.repository;

import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRolOrderByNombreAsc(Rol rol);

    long countByRol(Rol rol);
}
