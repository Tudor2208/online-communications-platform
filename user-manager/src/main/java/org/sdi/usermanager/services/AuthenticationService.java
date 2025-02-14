package org.sdi.usermanager.services;

import org.sdi.usermanager.dtos.AuthenticationRequest;
import org.sdi.usermanager.dtos.AuthenticationResponse;
import org.sdi.usermanager.dtos.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest registerRequest);
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
}
