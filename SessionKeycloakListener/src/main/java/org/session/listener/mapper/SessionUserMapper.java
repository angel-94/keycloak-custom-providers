package org.session.listener.mapper;

import java.util.HashMap;
import java.util.Map;
import org.session.listener.model.LoginRecord;

public class SessionUserMapper {

    private SessionUserMapper() {
        // Evita instanciación
    }

    public static Map<String, Object> toSnakeCase(LoginRecord record) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("user_id", record.userId);
        dto.put("last_login", record.lastLogin);
        dto.put("terms_accepted_stamp", record.acceptedTimestamp);
        dto.put("realm_id", record.realmId);
        dto.put("realm_name", record.realmName);
        dto.put("access_ip", record.accessIp);
        dto.put("source", record.source);
        dto.put("zone_id", record.zoneId);
        return dto;
    }
}
