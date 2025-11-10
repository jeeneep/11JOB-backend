package com.the11job.backend.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}