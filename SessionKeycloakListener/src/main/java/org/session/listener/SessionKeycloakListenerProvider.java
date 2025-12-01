package org.session.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.time.*;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.session.listener.mapper.SessionUserMapper;
import org.session.listener.model.LoginRecord;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.net.http.HttpClient.*;

public class SessionKeycloakListenerProvider implements EventListenerProvider{

    private final KeycloakSession session;

    private static final Logger logger = Logger.getLogger(SessionKeycloakListenerProvider.class);
    private static final String keycloakBaseUrl = System.getenv("KEYCLOAK_BASE_URL");
    private static final String clientIdService = System.getenv("CLIENT_ID");
    private static final String clientSecretService = System.getenv("CLIENT_SECRET");
    private static final String apiUserHost = System.getenv("API_USER_HOST");


    public ObjectMapper mapper = new ObjectMapper();

    public SessionKeycloakListenerProvider(KeycloakSession session){
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (EventType.LOGIN.equals(event.getType())) {

            try{
                String userId = event.getUserId();

                Instant lastLogin = Instant.now();
                // LocalDateTime lastLoginForDB = LocalDateTime.ofInstant(lastLogin, ZoneOffset.UTC);
                ZonedDateTime lastLoginMexico = lastLogin.atZone(ZoneId.of("America/Mexico_City"));
                String lastLoginStr = lastLoginMexico.toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                RealmModel realm = session.realms().getRealm(event.getRealmId());
                UserModel user = session.users().getUserById(realm, userId);

                String realmId = realm.getId();
                String realmName = realm.getName();
                String acceptedTimestamp = user.getFirstAttribute("terms_and_conditions");
                String ip = event.getIpAddress();

                LoginRecord record = new LoginRecord(
                        userId,
                        lastLoginStr,
                        acceptedTimestamp,
                        realmId,
                        realmName,
                        ip,
                        "keycloak",
                        "America/Mexico_City"
                );

                Map<String, Object> payload = SessionUserMapper.toSnakeCase(record);

                // Asincronía para evitar deadlock
                CompletableFuture.runAsync(() -> {
                   try{
                       String access_token = generateToken(realmName);
                       sendInformation(access_token, payload);
                   } catch (Exception e){
                       logger.error("Error en el proceso asíncrono del SPI", e);
                   }
                });
            } catch (Exception e) {
                logger.error("Ha ocurrido un error en onEvent", e);
                throw new RuntimeException(e);
            }

        }
    }


    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
            System.out.println("Hola");
    }

    @Override
    public void close() {

    }

    public static String generateToken(String realm) throws IOException, InterruptedException {

        String tokenUrl = (keycloakBaseUrl.endsWith("/") ? keycloakBaseUrl : keycloakBaseUrl + "/")
                + "realms/" + realm + "/protocol/openid-connect/token";

        String form = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientIdService, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecretService, StandardCharsets.UTF_8);

        // Construcción de cliente HTTP
        HttpClient http = newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Error al generar token: " + response.statusCode() + " - " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> payload = mapper.readValue(response.body(), Map.class);

        return (String) payload.get("access_token");
    }

    public void sendInformation(String token, Map<String, Object> payload) throws IOException, InterruptedException {
        String jsonPayload = mapper.writeValueAsString(payload);

        logger.infof(jsonPayload);

        // String endpointUrl = "https://phx-api.sucrimsoft.com/users/api/v1/session/details/user";
        String endpointUrl = apiUserHost + "/users/api/v1/session/details/user";

        HttpClient client = newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logger.infof("Response status -> " + String.valueOf(response.statusCode()));
    }
}
