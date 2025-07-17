package org.example;

import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.models.RealmModel;
import org.jboss.logging.Logger;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.theme.Theme;

import java.text.SimpleDateFormat;
import java.net.URL;
import java.util.*;

public class TermsAcceptedListenerProvider implements EventListenerProvider{

    private static final Logger logger = Logger.getLogger(TermsAcceptedListenerProvider.class);
    private final KeycloakSession session;
    private final Set<String> notifiedUsers = new HashSet<>();

    public TermsAcceptedListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (event == null || event.getUserId() == null || event.getRealmId() == null) return;

        logger.infof("Evento detectado: %s", event.getType());

        String clientId = event.getClientId();
        if (!clientId.equals("super-rewards-derco-app") && !clientId.equals("82bb86b9-6e7e-40b0-8df3-c8f0ce4fbcc2")) return;

        if (event.getType() == EventType.EXECUTE_ACTIONS || event.getType() == EventType.UPDATE_PROFILE || event.getType() == EventType.CODE_TO_TOKEN) {
            RealmModel realm = session.realms().getRealm(event.getRealmId());
            if (realm == null) {
                logger.warn("Realm es null.");
                return;
            }

            UserModel user = session.users().getUserById(realm, event.getUserId());
            if (user == null) {
                logger.warn("User es null.");
                return;
            }

            Map<String, List<String>> allAttributes = user.getAttributes();
            logger.infof("Atributos del usuario %s: %s", user.getUsername(), allAttributes);

            String acceptedTimestamp = user.getFirstAttribute("terms_and_conditions");
            String alreadyNotified = user.getFirstAttribute("terms_notified");

            if (acceptedTimestamp != null && !"true".equals(alreadyNotified)) {
                logger.infof("[TermsAcceptedListener] Usuario %s aceptó términos en %s", user.getUsername(), acceptedTimestamp);
                notifiedUsers.add(user.getId());
                user.setSingleAttribute("terms_notified", "true");

                sendTermsAcceptedEmail(realm, user, acceptedTimestamp);
            }
        }
    }

    private void sendTermsAcceptedEmail(RealmModel realm, UserModel user, String timestamp) {
        Map<String, Object> attributes = new HashMap<>();
        try {
            String fullName = user.getFirstName() + " " + user.getLastName();
            String businessName = user.getFirstAttribute("businessName");
            String date = transformDate(timestamp);
            String hour = getHour(timestamp);
            String userEmail = user.getEmail();

            attributes.put("full_name", fullName);
            attributes.put("date", date);
            attributes.put("hour", hour);
            attributes.put("concessionaire", businessName);
            attributes.put("user_email", userEmail);

            EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class)
                    .setRealm(realm)
                    .setUser(user);

            emailProvider.send(
                    "Super Puntos: Aceptaste los T&C del programa",
                    "email-terms.ftl",
                    attributes
            );

        } catch (Exception e) {
            logger.error("Error al enviar el correo de notificación", e);
        }
    }

    public String transformDate(String timeStamp) {
        long seconds = Long.parseLong(timeStamp);
        long millis = seconds * 1000L;
        Date fecha = new Date(millis);

        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        sdfFecha.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

        return sdfFecha.format(fecha);
    }

    public String getHour(String timeStamp){
        long seconds = Long.parseLong(timeStamp);
        long millis = seconds * 1000L;
        Date fecha = new Date(millis);

        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss", new Locale("es", "CO"));
        sdfHora.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

        return sdfHora.format(fecha);
    }


    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        logger.info("Evento de administrador recibido: " + event.getOperationType());
        System.out.printf("Evento de administrador");
    }

    @Override
    public void close() {

    }
}
