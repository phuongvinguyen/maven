/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.cling.invoker.mvn.resident;

import org.apache.maven.api.cli.InvokerException;
import org.apache.maven.api.cli.InvokerRequest;
import org.apache.maven.cling.invoker.mvn.MavenContext;

public class ResidentMavenContext extends MavenContext {

    protected ResidentMavenContext(InvokerRequest invokerRequest) {
        super(invokerRequest);
    }

    @Override
    public void close() throws InvokerException {
        // we are resident, we do not shut down here
    }

    public void shutDown() throws InvokerException {
        super.close();
    }

    public ResidentMavenContext copy(InvokerRequest invokerRequest) {
        if (invokerRequest == this.invokerRequest) {
            return this;
        }
        ResidentMavenContext shadow = new ResidentMavenContext(invokerRequest);

        shadow.logger = logger;
        shadow.loggerFactory = loggerFactory;
        shadow.loggerLevel = loggerLevel;
        shadow.containerCapsule = containerCapsule;
        shadow.lookup = lookup;

        shadow.interactive = interactive;
        shadow.localRepositoryPath = localRepositoryPath;
        shadow.installationSettingsPath = installationSettingsPath;
        shadow.projectSettingsPath = projectSettingsPath;
        shadow.userSettingsPath = userSettingsPath;
        shadow.effectiveSettings = effectiveSettings;

        shadow.mavenExecutionRequest = mavenExecutionRequest;
        shadow.eventSpyDispatcher = eventSpyDispatcher;
        shadow.mavenExecutionRequestPopulator = mavenExecutionRequestPopulator;
        shadow.modelProcessor = modelProcessor;
        shadow.maven = maven;

        return shadow;
    }
}
