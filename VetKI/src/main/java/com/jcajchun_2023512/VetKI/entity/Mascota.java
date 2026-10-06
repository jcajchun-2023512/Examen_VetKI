package com.jcajchun_2023512.VetKI.entity;

import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una mascota registrada por un cliente.
 */
@Entity
@Table(name = "mascotas", indexes = {
    @Index(name = "idx_mascota_cliente", columnList = "cliente_id"),
    @Index(name = "idx_mascota_especie", columnList = "especie")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Especie especie;

    @Column(length = 100)
    private String raza;

    @Column(nullable = false)
    private Integer edad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;
}
