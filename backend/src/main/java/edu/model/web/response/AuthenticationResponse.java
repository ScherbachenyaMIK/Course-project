package edu.model.web.response;

import lombok.Data;

@Data
public class AuthenticationResponse {
    boolean success = true;
    String cause = "";
}
