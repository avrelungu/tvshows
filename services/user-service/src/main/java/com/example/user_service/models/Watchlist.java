package com.example.user_service.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "watchlist")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserProfile userProfile;

    @Column(name = "show_id", unique = false, nullable = false)
    private Integer showId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image_medium")
    private String imageMedium;

    @Column(name = "image_original")
    private String imageOriginal;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();
}
