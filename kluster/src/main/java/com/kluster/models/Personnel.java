package com.kluster.models;

public class Personnel {
    private int id;
    private String name;
    private PersonnelRole role;
    private boolean isActive;
    private String passwordHash;
    private Permission[] permissions;
    private int rank;

    public Personnel(int id, String name, int rank, PersonnelRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.isActive = true;
        this.rank = rank;
    }

    public Personnel(int id, String name, int rank, PersonnelRole role, Permission[] permissions) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.rank = rank;
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public PersonnelRole getRole() {
        return role;
    }

    public int getRank() {
        return rank;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setRole(PersonnelRole role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
