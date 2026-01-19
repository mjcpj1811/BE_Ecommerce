package com.example.BE_E_commerce.security.jwt;

import com.example.BE_E_commerce.entity.User;
import com.example.BE_E_commerce.enums.UserRole;
import com.example.BE_E_commerce.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails, OAuth2User {

    private Long id;
    private String username;
    private String email;
    private String fullName;

    @JsonIgnore
    private String password;

    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;

    private Collection<? extends GrantedAuthority> authorities;
    
    private Map<String, Object> attributes;

    /**
     * Build UserDetailsImpl from User entity
     */
    public static UserDetailsImpl build(User user) {
        // Convert role to GrantedAuthority
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", user.getId());
        attributes.put("email", user.getEmail());
        attributes.put("name", user.getFullName());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus(),
                user.getEmailVerified(),
                authorities,
                attributes
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Use email as username
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return email;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
