package com.training.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "tbl_category",
        indexes = {
                @Index(name = "idx_category_name", columnList = "name")
        }
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
}
