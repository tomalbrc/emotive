package com.cobblemonislands.emotive.storage;

import org.jetbrains.annotations.Nullable;

/**
 * MongoDB connection settings.
 */
public record MongoSettings(
        boolean enabled,
        String host,
        int port,
        String database,
        @Nullable String username,
        @Nullable String password,
        @Nullable String authDatabase,
        boolean useSrv,          // for mongodb+srv URIs
        boolean sslEnabled,      // enable TLS/SSL
        int connectionPoolSize   // max connections in the pool
) {
    public MongoSettings {
        // defaults / validation
        if (host == null || host.isBlank()) {
            host = "localhost";
        }
        if (port <= 0) {
            port = 27017;
        }
        if (database == null || database.isBlank()) {
            database = "emotive";
        }
        if (connectionPoolSize <= 0) {
            connectionPoolSize = 10;
        }
    }

    public String toConnectionString() {
        String protocol = useSrv ? "mongodb+srv" : "mongodb";
        StringBuilder uri = new StringBuilder(protocol + "://");

        if (username != null && password != null) {
            uri.append(username).append(":").append(password).append("@");
        }

        uri.append(host);
        if (!useSrv) {
            uri.append(":").append(port);
        }

        uri.append("/").append(database);

        boolean firstParam = true;
        if (sslEnabled) {
            uri.append("?").append("ssl=true");
            firstParam = false;
        }
        if (authDatabase != null && !authDatabase.isBlank()) {
            uri.append(firstParam ? "?" : "&").append("authSource=").append(authDatabase);
        }

        return uri.toString();
    }
}
