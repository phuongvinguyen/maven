package org.apache.maven.plugin.coreit;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Resolves user-specified artifacts transitively. As an additional exercise, the resolution happens in a forked thread
 * to test access to any shared session state.
 * 
 * @goal resolve-transitive
 * 
 * @author Benjamin Bentmann
 */
public class ResolveTransitiveMojo
    extends AbstractMojo
{

    /**
     * The local repository.
     * 
     * @parameter default-value="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * The remote repositories of the current Maven project.
     * 
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List remoteRepositories;

    /**
     * The artifact resolver.
     * 
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * The artifact factory.
     * 
     * @component
     */
    private ArtifactFactory factory;

    /**
     * The metadata source.
     * 
     * @component
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * The dependencies to resolve.
     * 
     * @parameter
     */
    private Dependency[] dependencies;

    /**
     * The path to a properties file to store the resolved artifact paths in.
     * 
     * @parameter
     */
    private File propertiesFile;

    /**
     * Runs this mojo.
     * 
     * @throws MojoExecutionException If the artifacts couldn't be resolved.
     */
    public void execute()
        throws MojoExecutionException
    {
        getLog().info( "[MAVEN-CORE-IT-LOG] Resolving artifacts" );

        ResolverThread thread = new ResolverThread();
        thread.start();
        while ( thread.isAlive() )
        {
            try
            {
                thread.join();
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        if ( thread.error != null )
        {
            throw new MojoExecutionException( "Failed to resolve artifacts: " + thread.error.getMessage(),
                                              thread.error );
        }

        if ( propertiesFile != null )
        {
            getLog().info( "[MAVEN-CORE-IT-LOG] Creating properties file " + propertiesFile );

            try
            {
                propertiesFile.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream( propertiesFile );
                try
                {
                    thread.props.store( fos, "MAVEN-CORE-IT" );
                }
                finally
                {
                    fos.close();
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to create properties file: " + e.getMessage(), e );
            }
        }
    }

    private String getId( Artifact artifact )
    {
        artifact.isSnapshot(); // decouple from MNG-2961
        return artifact.getId();
    }

    class ResolverThread
        extends Thread
    {

        Properties props = new Properties();

        Exception error;

        public void run()
        {
            if ( dependencies != null )
            {
                try
                {
                    Set artifacts = new LinkedHashSet();

                    for ( int i = 0; i < dependencies.length; i++ )
                    {
                        Dependency dependency = dependencies[i];

                        Artifact artifact =
                            factory.createArtifactWithClassifier( dependency.getGroupId(), dependency.getArtifactId(),
                                                                  dependency.getVersion(), dependency.getType(),
                                                                  dependency.getClassifier() );

                        getLog().info( "[MAVEN-CORE-IT-LOG] Resolving "
                                        + ResolveTransitiveMojo.this.getId( artifact ) );

                        artifacts.add( artifact );
                    }

                    Artifact origin = factory.createArtifact( "it", "it", "0.1", null, "pom" );

                    artifacts =
                        resolver.resolveTransitively( artifacts, origin, remoteRepositories, localRepository,
                                                      metadataSource ).getArtifacts();

                    for ( Iterator it = artifacts.iterator(); it.hasNext(); )
                    {
                        Artifact artifact = (Artifact) it.next();

                        if ( artifact.getFile() != null )
                        {
                            props.setProperty( ResolveTransitiveMojo.this.getId( artifact ),
                                               artifact.getFile().getPath() );
                        }

                        getLog().info( "[MAVEN-CORE-IT-LOG]   " + artifact.getFile() );
                    }
                }
                catch ( Exception e )
                {
                    error = e;
                }
            }
        }
    }

}
