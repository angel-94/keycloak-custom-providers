package org.session.listener.model;

public class LoginRecord {
    public String userId;
    public String lastLogin;
    public String acceptedTimestamp;
    public String realmId;
    public String realmName;
    public String accessIp;
    public String source;
    public String zoneId;

    public LoginRecord(String userId,
                       String lastLogin,
                       String acceptedTimestamp,
                       String realmId,
                       String realmName,
                       String accessIp,
                       String source,
                       String zoneId
    ){
        this.userId = userId;
        this.lastLogin = lastLogin;
        this.acceptedTimestamp = acceptedTimestamp;
        this.realmId = realmId;
        this.realmName = realmName;
        this.accessIp = accessIp;
        this.source = source;
        this.zoneId = zoneId;
    }

    @Override
    public String toString() {
        return "LoginRecord{" +
                "userId='" + userId + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                ", termsAcceptedAt='" + acceptedTimestamp + '\'' +
                ", realmId='" + realmId + '\'' +
                ", realmName='" + realmName + '\'' +
                ", accessIp'" + accessIp + '\'' +
                ", source'" + source + '\'' +
                ", zoneId'" + zoneId + '\'' +
                '}';
    }
}
