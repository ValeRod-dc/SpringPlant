package com.example.ms_discount.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_discount")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    @Column(unique = true, nullable = false)
    private String code;                  // código promocional (único)

    @Size(max = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Double discountValue;         // 15.0 para 15% o 10.0 para 10€

    private LocalDateTime validFrom;      // desde cuándo aplica este precio
    private LocalDateTime validUntil;     // hasta cuándo (inclusive o exclusivo)

    private Integer maxUses;              // número máximo de usos total (null = ilimitado)
    private Integer currentUses;          // contador de usos

    private Double minPurchaseAmount;     // monto mínimo de compra para aplicar (opcional)

    private Boolean active;               // si está activo manualmente

    @Column(length = 500)
    private String applicableProductIds;  // puede ser lista de IDs separados por coma (null = todos los productos)
}
