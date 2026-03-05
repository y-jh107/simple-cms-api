package com.malgn.security;

import com.malgn.entity.Role;
import org.springframework.security.core.GrantedAuthority;

public class CmsGrantedAuthority implements GrantedAuthority {
    private final Role role;

    public CmsGrantedAuthority(Role role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CmsGrantedAuthority) {
            return role.equals(((CmsGrantedAuthority) o).role);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return role.hashCode();
    }

    @Override
    public String toString() {
        return this.role.name();
    }
}
