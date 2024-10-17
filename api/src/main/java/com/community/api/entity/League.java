package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.core.lang.Nullable;
import lombok.*;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;


    @OneToMany(mappedBy = "league", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Team> teams;

    private LocalDateTime createdate;

    @PrePersist
    protected void onCreate() {
        createdate = LocalDateTime.now();
    }

}

