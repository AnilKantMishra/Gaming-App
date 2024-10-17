package com.community.api.entity;

import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Tournament name is required.")
    private String name;

    @FutureOrPresent(message = "Start date must be in the future.")
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future.")
    private LocalDateTime endDate;
    private String status; //    status value can be like this  "ongoing", "completed"
    private LocalDateTime createdate;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    private List<Prize> prizes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdate = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    private List<Participation> participations = new ArrayList<>();

}