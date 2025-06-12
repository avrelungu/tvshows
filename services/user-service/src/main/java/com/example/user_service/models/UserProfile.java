package com.example.user_service.models;

import com.example.user_service.enums.MemberType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50, name = "username")
    private String username;

    @Column(nullable = false, length = 50, name = "first_name")
    private String firstName;

    @Column(nullable = false, length = 50, name = "last_name")
    private String lastName;

    @Column(nullable = false, length = 50, name = "email")
    private String email;

    @Column(name = "member_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberType memberType = MemberType.FREE;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<Watchlist> watchlists;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
