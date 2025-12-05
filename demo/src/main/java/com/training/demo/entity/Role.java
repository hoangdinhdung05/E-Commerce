package com.training.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.training.demo.utils.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_role")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true)
    private RoleType name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    @Builder.Default
    private Set<UserHasRole> userHasRoles = new HashSet<>();

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    @Builder.Default
    private Set<RoleHasPermission> roles = new HashSet<>();
}