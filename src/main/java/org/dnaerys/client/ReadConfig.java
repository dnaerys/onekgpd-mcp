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

package org.dnaerys.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Config file utilities.
 *
 * @author Dmitry Degrave (info@dnaerys.org)
 * @version 1.0
 */
public class ReadConfig {
    private static final String propFileName = "dnaerys.properties";
    private static final Properties prop = readConfig();

    private static Properties readConfig() {
        Properties p = new Properties();
        // defaults for missing values
        p.setProperty("dnaerysHost","db.dnaerys.org");
        p.setProperty("dnaerysGRPCPort","443");
        p.setProperty("ssl","true");

        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propFileName)) {
            if (inputStream != null) {
                p.load(inputStream);
            }
        } catch (IOException e) {
            // discards all values from failed stream
            Properties pd = new Properties();
            pd.setProperty("dnaerysHost","db.dnaerys.org");
            pd.setProperty("dnaerysGRPCPort","443");
            pd.setProperty("ssl","true");
            return pd;
        }
        return p;
    }

    public static Properties getProp() {
        return prop;
    }
}