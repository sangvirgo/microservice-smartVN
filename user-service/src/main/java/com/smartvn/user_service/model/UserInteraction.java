package com.smartvn.user_service.model;

import com.smartvn.user_service.enums.InteractionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_interactions")
@Getter
@Setter
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;  // Nullable cho guest

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @Column(nullable = false)
    private Float weight = 1.0f;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}