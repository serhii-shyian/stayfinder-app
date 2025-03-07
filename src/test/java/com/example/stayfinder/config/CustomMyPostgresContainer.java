package com.example.stayfinder.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class CustomMyPostgresContainer extends PostgreSQLContainer<CustomMyPostgresContainer> {
    private static final String DB_IMAGE = "postgres:15-alpine";

    private static CustomMyPostgresContainer myPostgresContainer;

    private CustomMyPostgresContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomMyPostgresContainer getInstance() {
        if (myPostgresContainer == null) {
            myPostgresContainer = new CustomMyPostgresContainer();
        }
        return myPostgresContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", myPostgresContainer.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", myPostgresContainer.getUsername());
        System.setProperty("TEST_DB_PASSWORD", myPostgresContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
