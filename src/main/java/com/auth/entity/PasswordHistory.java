package com.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.auth.config.LocalDateTimeConverter;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistory {
    public static final int MAX_DELETIONS_AT_ONCE = 10000;

    @Id
    @Column(name = "password_id", insertable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "password_history_password_id_seq")
    @SequenceGenerator(name = "password_history_password_id_seq", sequenceName = "password_history_password_id_seq", allocationSize = 1)
    private Long passwordId;

    @Column(name = "uuid", nullable = false)
    @NotNull
    private UUID uuid;

    @ApiModelProperty(example = "2014-10-09T16:23:00")
    @Column(name = "date_added", nullable = false)
    @Convert(converter = LocalDateTimeConverter.class)
    @NotNull
    private LocalDateTime dateAdded;

    @Column(name = "password_hash", nullable = false)
    @NotNull
    private String passwordHash;

}
