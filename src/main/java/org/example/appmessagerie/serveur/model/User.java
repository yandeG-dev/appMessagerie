package org.example.appmessagerie.serveur.model;

import lombok.Getter;
import lombok.Setter;
import org.example.appmessagerie.serveur.model.enums.UserStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime dateCreation;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.status = UserStatus.OFFLINE;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public UserStatus getStatus() { return status; }

    public void setStatus(UserStatus status) { this.status = status; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
