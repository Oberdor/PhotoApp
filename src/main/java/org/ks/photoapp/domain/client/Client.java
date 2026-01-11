package org.ks.photoapp.domain.client;

import jakarta.persistence.*;
import lombok.Data;
import org.ks.photoapp.domain.photosession.PhotoSession;

import java.util.ArrayList;
import java.util.List;
@Data
@Entity
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String firstName;
    String lastName;
    String email;
    Long phoneNumber;
    @OneToMany(mappedBy = "client", cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    List<PhotoSession> photoSessions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<PhotoSession> getPhotoSessions() {
        return photoSessions;
    }

    public void setPhotoSessions(List<PhotoSession> photoSessions) {
        this.photoSessions = photoSessions;
    }
}
