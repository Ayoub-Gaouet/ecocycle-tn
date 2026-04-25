package tn.ecocycle.ecocycletn.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    private String governorate;

    @Column(nullable = false)
    private int ecoPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected User() {
    }

    public User(String email, String password, String fullName, Role role) {
        this(email, password, fullName, null, null, 0, role);
    }

    public User(
            String email,
            String password,
            String fullName,
            String phone,
            String governorate,
            int ecoPoints,
            Role role
    ) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.governorate = governorate;
        this.ecoPoints = ecoPoints;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getGovernorate() {
        return governorate;
    }

    public int getEcoPoints() {
        return ecoPoints;
    }

    public Role getRole() {
        return role;
    }

    public void updateProfile(String fullName, String phone, String governorate) {
        this.fullName = fullName;
        this.phone = phone;
        this.governorate = governorate;
    }
}
