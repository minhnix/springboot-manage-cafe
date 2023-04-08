package com.nix.managecafe.model;

import com.nix.managecafe.model.audit.DateAudit;
import com.nix.managecafe.model.audit.UserAudit;
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
public class Notification extends UserAudit {
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
