package com.passport.mrz.model;

import lombok.Data;

@Data
public class UserInfo {

    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String nationality;
    private String documentNumber;
    private String dateOfExpiry;
    private String issuingState;

}
