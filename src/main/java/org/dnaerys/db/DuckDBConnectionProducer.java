/*
 * Copyright © 2026 Dmitry Degrave
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dnaerys.db;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@ApplicationScoped
public class DuckDBConnectionProducer {

    private static final Logger LOG = Logger.getLogger(DuckDBConnectionProducer.class);
    private static final String KGPE_RESOURCE = "kgpe.json";
    private static final String TABLE_NAME = "sample_meta";

    private Connection connection;
    private Path tempJsonFile;

    @PostConstruct
    void init() {
        try {
            connection = DriverManager.getConnection("jdbc:duckdb:");

            tempJsonFile = Files.createTempFile("kgpe-", ".json");
            try (InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(KGPE_RESOURCE)) {
                if (is == null) {
                    throw new RuntimeException("Resource not found: " + KGPE_RESOURCE);
                }
                Files.copy(is, tempJsonFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE TABLE " + TABLE_NAME + " AS " +
                    "SELECT * FROM read_json_auto('" +
                    tempJsonFile.toAbsolutePath() + "')"
                );
            }

            try (Statement stmt = connection.createStatement();
                 var rs = stmt.executeQuery("SELECT count(*) FROM " + TABLE_NAME)) {
                rs.next();
                LOG.infof("DuckDB initialized: loaded %d records into '%s'",
                    rs.getInt(1), TABLE_NAME);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DuckDB", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @PreDestroy
    void cleanup() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            if (tempJsonFile != null) {
                Files.deleteIfExists(tempJsonFile);
            }
        } catch (Exception e) {
            LOG.warn("Error during DuckDB cleanup", e);
        }
    }
}
