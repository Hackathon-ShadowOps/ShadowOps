package com.kluster.models;

public class Personal {
    private String id;
    private String name;
    private PersonalRole role;
    private boolean isActive;
    private String passwordHash;
    private Permission[] permissions;

    public Personal(String id, String name, String rank, PersonalRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.isActive = true;
    }

    public Personal(String id, String name, String rank, PersonalRole role, Permission[] permissions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.permissions = permissions;
        this.isActive = true;
    }

    public boolean hasPermission(Permission permission) {
        if (permissions != null) {
            for (Permission p : permissions) {
                if (p == permission) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public PersonalRole getRole() {
        return role;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setRole(PersonalRole role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
