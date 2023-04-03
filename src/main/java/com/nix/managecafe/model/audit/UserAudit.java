package com.nix.managecafe.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@MappedSuperclass
@JsonIgnoreProperties(
        value = {"createdBy", "updatedBy"},
        allowGetters = true
)
@Getter
@Setter
public abstract class UserAudit extends DateAudit {
    @CreatedBy
    @JsonIgnore
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @JsonIgnore
    private Long updatedBy;
}
