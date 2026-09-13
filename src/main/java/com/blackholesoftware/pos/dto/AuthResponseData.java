package com.blackholesoftware.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseData {
    private String id;
    private String username;
    private String fullName;
    private String role; // ADMIN, MANAGER, CASHIER
}