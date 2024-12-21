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
package org.apache.maven.it;

import java.io.File;
import java.util.Collection;

import org.apache.maven.shared.verifier.util.ResourceExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is a test set for <a href="https://issues.apache.org/jira/browse/MNG-1233">MNG-1233</a>.
 *
 * @author Brett Porter
 *
 */
public class MavenITmng1233WarDepWithProvidedScopeTest extends AbstractMavenIntegrationTestCase {
    public MavenITmng1233WarDepWithProvidedScopeTest() {
        super(ALL_MAVEN_VERSIONS);
    }

    /**
     * Verify that overriding a transitive compile time dependency as provided in a WAR ensures it is not included.
     *
     * @throws Exception in case of failure
     */
    @Test
    public void testitMNG1233() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/mng-1233");

        Verifier verifier = newVerifier(testDir.getAbsolutePath());
        verifier.setAutoclean(false);
        verifier.deleteDirectory("target");
        verifier.deleteArtifacts("org.apache.maven.its.it0083");
        verifier.filterFile("settings-template.xml", "settings.xml");
        verifier.addCliArgument("--settings");
        verifier.addCliArgument("settings.xml");
        verifier.addCliArgument("validate");
        verifier.execute();
        verifier.verifyErrorFreeLog();

        Collection<String> compileArtifacts = verifier.loadLines("target/compile.txt");
        assertTrue(
                compileArtifacts.contains("org.apache.maven.its.it0083:direct-dep:jar:0.1"),
                compileArtifacts.toString());
        assertTrue(
                compileArtifacts.contains("org.apache.maven.its.it0083:trans-dep:jar:0.1"),
                compileArtifacts.toString());

        Collection<String> runtimeArtifacts = verifier.loadLines("target/runtime.txt");
        assertTrue(
                runtimeArtifacts.contains("org.apache.maven.its.it0083:direct-dep:jar:0.1"),
                runtimeArtifacts.toString());
        assertFalse(
                runtimeArtifacts.contains("org.apache.maven.its.it0083:trans-dep:jar:0.1"),
                runtimeArtifacts.toString());
    }
}