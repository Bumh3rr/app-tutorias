package com.bumh3r.service.impl;

import com.bumh3r.service.SessionRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SessionRegistryServiceImpl implements SessionRegistryService {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Override
    public void invalidarSesionesDeUsuario(String username) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(p -> p instanceof UserDetails && ((UserDetails) p).getUsername().equals(username))
                .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
                .forEach(SessionInformation::expireNow);
    }
}
