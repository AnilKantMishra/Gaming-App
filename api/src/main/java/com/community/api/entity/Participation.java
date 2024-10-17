package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import javax.persistence.Entity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "customer_id")

    private CustomCustomer customCustomer;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    private String status; // status value can be like this., "active", "completed"

}
