package com.community.api.entity;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;


@Entity
@Table(name = "CUSTOM_CUSTOMER")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomCustomer extends CustomerImpl {

    @Nullable
    @Column(name = "country_code")
    private String countryCode;

    @Nullable
    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Nullable
    @Column(name = "otp", unique = true)
    private String otp;

    @Nullable
    @Column(name = "pan_number")
    private String panNumber;


    @Nullable
    @Column(name = "father_name")
    private String fathersName;

    @Nullable
    @Column(name = "nationality")
    private String nationality;

    @Column(name = "mother_name")
    private String mothersName;

    @Nullable
    @Column(name = "date_of_birth")
    private String dob;

    @Nullable
    @Column(name = "gender")
    private String gender;

    @Nullable
    @Column(name = "adhar_number", unique = true)
    @Size(min = 12, max = 12)
    private String adharNumber;

    @Nullable
    @Column(name = "secondary_mobile_number")
    private String secondaryMobileNumber;

    @Nullable
    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Nullable
    @Column(name = "secondary_email")
    private String secondaryEmail;

    @Nullable
    @Column(name = "residential_address")
    private String residentialAddress;

    @Nullable
    @Column(name = "state")
    private String state;

    @Nullable
    @Column(name = "district")
    private String district;

    @Nullable
    @Column(name = "city")
    private String city;

    @Nullable
    @Column(name = "pincode")
    private String pincode;

    @Nullable
    private String token;


}