package com.training.demo.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.training.demo.utils.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "tbl_user")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    private UserStatus status;

    @Column(name = "verify_email")
    private boolean verifyEmail;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            mappedBy = "user",
            orphanRemoval = true
    )
    @JsonManagedReference("user-userHasRoles")
    @Builder.Default
    private Set<UserHasRole> userHasRoles = new HashSet<>();

    public Set<Role> getRoles() {
        return userHasRoles.stream()
                .map(UserHasRole::getRole)
                .collect(Collectors.toSet());
    }
}
