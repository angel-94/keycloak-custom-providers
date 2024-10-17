package com.portal.passwordless.resource;

import jakarta.ws.rs.core.HttpHeaders;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAdminResource {

	private static final Logger log = LoggerFactory.getLogger(AbstractAdminResource.class);

	protected final ClientConnection connection;
	protected final HttpHeaders headers;
	protected final KeycloakSession session;
	protected final RealmModel realm;

	protected AbstractAdminResource(KeycloakSession session) {
		this.session = session;
		this.realm = session.getContext().getRealm();
		this.headers = session.getContext().getRequestHeaders();
		this.connection = session.getContext().getConnection();
	}
}
