/*
 * Copyright 2019 Red Hat, Inc, and individual contributors.
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
package org.xstefank.check;

import javax.json.JsonObject;

import io.xstefank.Check;
import org.jboss.logging.Logger;
import org.xstefank.model.Utils;
import org.xstefank.model.yaml.FormatConfig;
import org.xstefank.model.yaml.Format;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class TemplateChecker {

    private static final Logger log = Logger.getLogger(TemplateChecker.class);

    private List<Check> checks;

    public TemplateChecker(FormatConfig config) {
        if (config == null || config.getFormat() == null) {
            throw new IllegalArgumentException("Input argument cannot be null");
        }
        checks = registerChecks(config.getFormat());
    }

    /**
     * Verifies the pull request payload against a set of defined checks
     *
     * @param payload the PR paylaod JSON received from GitHub
     * @return error message or empty string if there is no failure found
     */
    public String checkPR(JsonObject payload) {
        log.debug("checking PR" + Utils.LINE_SEPARATOR + payload);
        String errorMessage = "";

        if (checks.isEmpty()) {
            log.warn("No checks were requested in the configuration");
            return "";
        }

        for (Check check : checks) {
            String message = check.check(payload);
            if (message != null) {
                errorMessage = message;
                break;
            }
        }

        return errorMessage;
    }

    private static List<Check> registerChecks(Format format) {
        List<Check> checks = new ArrayList<>();

        if (format.getTitle() != null) {
            checks.add(new TitleCheck(format.getTitle()));
        }

        if (format.getDescription() != null) {
            checks.add(new RequiredRowsCheck(format.getDescription().getRequiredRows()));
        }

        if (format.getCommit() != null) {
            checks.add(new LatestCommitCheck(format.getCommit()));
        }

        checks.addAll(loadAdditionalChecks());

        System.out.println("XXXXXXXXXXXXXXXXXXX " + checks);
        return checks;
    }

    private static List<Check> loadAdditionalChecks() {
        List<Check> result = new ArrayList<>();
        String additionalChecksValue = System.getProperty("additional-checks");

        if (additionalChecksValue == null) {
            return result;
        }

        String[] split = additionalChecksValue.split(",");
        URL[] jarURLs = new URL[split.length];

        for (int i = 0; i < split.length; i++) {
            File file = new File(split[i]);

            if (!file.getPath().toLowerCase().endsWith(".jar")) {
                log.warn("Invalid file included for additional checks, must be a jar: " + file.getPath());
                continue;
            }

            try {
                jarURLs[i] = file.toURI().toURL();
            } catch (MalformedURLException e) {
                log.warn("Invalid file name value passed in additional checks", e);
            }
        }

        ServiceLoader<Check> serviceLoader = ServiceLoader.load(Check.class, new URLClassLoader(jarURLs, Thread.currentThread().getContextClassLoader()));
        serviceLoader.iterator().forEachRemaining(result::add);

        return result;
    }
}
