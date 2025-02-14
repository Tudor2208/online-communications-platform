package org.sdi.usermanager.dtos;

public class AuthenticationResponse {

    private Long id;
    private String token;
    private String email;
    private String role;
    private String firstName;

    public AuthenticationResponse(Long id, String token, String email, String role, String firstName) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
