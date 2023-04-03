package com.nix.managecafe.model;

import com.nix.managecafe.model.audit.DateAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Notification extends DateAudit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String message;
    private String type;
    @ManyToOne
    private User toUser;
    private String slug;
    private boolean watched = Boolean.FALSE;
}
