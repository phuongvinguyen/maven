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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

/**
 * Provides common services for all mojos of this plugin.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 */
public abstract class AbstractDependencyMojo
    extends AbstractMojo
{

    /**
     * The current Maven project.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Writes the specified artifacts to the given output file.
     * 
     * @param pathname The path to the output file, relative to the project base directory, may be <code>null</code> or
     *            empty if the output file should not be written.
     * @param artifacts The list of artifacts to write to the file, may be <code>null</code>.
     * @throws MojoExecutionException If the output file could not be written.
     */
    protected void writeArtifacts( String pathname, List artifacts )
        throws MojoExecutionException
    {
        if ( pathname == null || pathname.length() <= 0 )
        {
            return;
        }

        // NOTE: We don't want to test path translation here so resolve relative path manually for robustness
        File file = new File( pathname );
        if ( !file.isAbsolute() )
        {
            file = new File( project.getBasedir(), pathname );
        }

        getLog().info( "[MAVEN-CORE-IT-LOG] Dumping artifact list: " + file );

        BufferedWriter writer = null;
        try
        {
            file.getParentFile().mkdirs();

            writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );

            if ( artifacts != null )
            {
                for ( Iterator it = artifacts.iterator(); it.hasNext(); )
                {
                    Artifact artifact = (Artifact) it.next();
                    writer.write( artifact.getId() );
                    writer.newLine();
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write artifact list", e );
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close();
                }
                catch ( IOException e )
                {
                    // just ignore
                }
            }
        }
    }

    /**
     * Writes the specified class path elements to the given output file.
     * 
     * @param pathname The path to the output file, relative to the project base directory, may be <code>null</code> or
     *            empty if the output file should not be written.
     * @param classPath The list of class path elements to write to the file, may be <code>null</code>.
     * @throws MojoExecutionException If the output file could not be written.
     */
    protected void writeClassPath( String pathname, List classPath )
        throws MojoExecutionException
    {
        if ( pathname == null || pathname.length() <= 0 )
        {
            return;
        }

        // NOTE: We don't want to test path translation here so resolve relative path manually for robustness
        File file = new File( pathname );
        if ( !file.isAbsolute() )
        {
            file = new File( project.getBasedir(), pathname );
        }

        getLog().info( "[MAVEN-CORE-IT-LOG] Dumping class path: " + file );

        BufferedWriter writer = null;
        try
        {
            file.getParentFile().mkdirs();

            writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );

            if ( classPath != null )
            {
                for ( Iterator it = classPath.iterator(); it.hasNext(); )
                {
                    Object element = it.next();
                    writer.write( element.toString() );
                    writer.newLine();
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write class path list", e );
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close();
                }
                catch ( IOException e )
                {
                    // just ignore
                }
            }
        }
    }

}
