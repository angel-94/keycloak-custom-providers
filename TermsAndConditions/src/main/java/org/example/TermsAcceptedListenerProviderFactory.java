package org.example;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;

public class TermsAcceptedListenerProviderFactory implements EventListenerProviderFactory{

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new TermsAcceptedListenerProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "terms-accepted-listener";
    }
}
