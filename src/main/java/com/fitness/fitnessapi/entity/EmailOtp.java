package com.fitness.fitnessapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "email_otp")
public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    public EmailOtp() {}

    public EmailOtp(String email, String otp, boolean isVerified) {
        this.email = email;
        this.otp = otp;
        this.isVerified = isVerified;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}
