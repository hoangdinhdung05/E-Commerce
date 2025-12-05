package com.training.demo.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

//    @PrePersist
//    public void handleBeforeCreate() {
//        this.createdBy = JwtTokenProvider.getCurrentUserLogin().isPresent()
//                ? JwtTokenProvider.getCurrentUserLogin().get()
//                : "";
//    }
//
//    @PreUpdate
//    public void handleBeforeUpdate() {
//        this.updatedBy = JwtTokenProvider.getCurrentUserLogin().isPresent()
//                ? JwtTokenProvider.getCurrentUserLogin().get()
//                : "";
//    }
}
