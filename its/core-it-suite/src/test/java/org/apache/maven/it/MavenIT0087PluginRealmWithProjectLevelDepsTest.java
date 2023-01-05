package org.apache.maven.it;

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

import org.apache.maven.shared.verifier.util.ResourceExtractor;
import org.apache.maven.shared.verifier.Verifier;

import java.io.File;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * This is a test set for <a href="https://issues.apache.org/jira/browse/MNG-1898">MNG-1898</a>.
 *
 * @author John Casey
 *
 */
public class MavenIT0087PluginRealmWithProjectLevelDepsTest
    extends AbstractMavenIntegrationTestCase
{

    public MavenIT0087PluginRealmWithProjectLevelDepsTest()
    {
        super( "(2.0.2,)" );
    }

    /**
     * Verify that a project-level plugin dependency class/resource can be loaded from both the plugin classloader
     * and the context classloader available to the plugin.
     *
     * @throws Exception in case of failure
     */
    @Test
    public void testit0087()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/it0087" );
        Verifier verifier = newVerifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "target" );
        verifier.deleteArtifacts( "org.apache.maven.its.it0087" );
        verifier.filterFile( "settings-template.xml", "settings.xml", "UTF-8" );
        verifier.addCliArgument( "--settings" );
        verifier.addCliArgument( "settings.xml" );
        verifier.addCliArgument( "validate" );
        verifier.execute();
        verifier.verifyErrorFreeLog();

        Properties pclProps = verifier.loadProperties( "target/pcl.properties" );
        assertNotNull( pclProps.getProperty( "org.apache.maven.plugin.coreit.ClassA" ) );
        assertNotNull( pclProps.getProperty( "org.apache.maven.plugin.coreit.ClassB" ) );
        assertNotNull( pclProps.getProperty( "org.apache.maven.its.it0087.IT0087" ) );
        assertNotNull( pclProps.getProperty( "src/main/java/org/apache/maven/its/it0087/IT0087.java" ) );
        assertEquals( "1", pclProps.getProperty( "src/main/java/org/apache/maven/its/it0087/IT0087.java.count" ) );

        Properties tcclProps = verifier.loadProperties( "target/tccl.properties" );
        assertEquals( pclProps, tcclProps );
    }

}
