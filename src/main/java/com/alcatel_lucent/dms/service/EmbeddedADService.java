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
package com.alcatel_lucent.dms.service;


import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;


/**
 * A simple example exposing how to embed Apache Directory Server version 1.5.7
 * into an application.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class EmbeddedADService
{
    /** The directory service */
    private DirectoryService service;

    /** The LDAP server */
    private LdapServer server;


    /**
     * Add a new partition to the server
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition( String partitionId, String partitionDn ) throws Exception
    {
        // Create a new partition named 'foo'.
        JdbmPartition partition = new JdbmPartition();
        partition.setId( partitionId );
        partition.setPartitionDir( new File( service.getWorkingDirectory(), partitionId ) );
        partition.setSuffix( partitionDn );
        service.addPartition( partition );

        return partition;
    }


    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs The list of attributes to index
     */
    private void addIndex( Partition partition, String... attrs )
    {
        // Index some attributes on the apache partition
        HashSet<Index<?, ServerEntry, Long>> indexedAttributes = new HashSet<Index<?, ServerEntry, Long>>();

        for ( String attribute : attrs )
        {
            indexedAttributes.add( new JdbmIndex<String, ServerEntry>( attribute ) );
        }

        ( ( JdbmPartition ) partition ).setIndexedAttributes( indexedAttributes );
    }


    /**
     * initialize the schema manager and add the schema partition to diectory service
     *
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition() throws Exception
    {
        SchemaPartition schemaPartition = service.getSchemaService().getSchemaPartition();

        // Init the LdifPartition
        LdifPartition ldifPartition = new LdifPartition();
        String workingDirectory = service.getWorkingDirectory().getPath();
        ldifPartition.setWorkingDirectory( workingDirectory + "/schema" );

        // Extract the schema on disk (a brand new one) and load the registries
        File schemaRepository = new File( workingDirectory, "schema" );
        SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor( new File( workingDirectory ) );
        extractor.extractOrCopy( true );

        schemaPartition.setWrappedPartition( ldifPartition );

        SchemaLoader loader = new LdifSchemaLoader( schemaRepository );
        SchemaManager schemaManager = new DefaultSchemaManager( loader );
        service.setSchemaManager( schemaManager );

        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse 
        // and normalize their suffix DN
        schemaManager.loadAllEnabled();

        schemaPartition.setSchemaManager( schemaManager );

        List<Throwable> errors = schemaManager.getErrors();

        if ( errors.size() != 0 )
        {
            throw new Exception( "Schema load failed : " + errors );
        }
    }


    /**
     * Initialize the server. It creates the partition, adds the index, and
     * injects the context entries for the created partitions.
     *
     * @param workDir the directory to be used for storing the data
     * @throws Exception if there were some problems while initializing the system
     */
    private void initDirectoryService( File workDir ) throws Exception
    {
        // Initialize the LDAP service
        service = new DefaultDirectoryService();
        service.setWorkingDirectory( workDir );

        // first load the schema
        initSchemaPartition();

        // then the system partition
        // this is a MANDATORY partition
        Partition systemPartition = addPartition( "system", ServerDNConstants.SYSTEM_DN );
        service.setSystemPartition( systemPartition );

        // Disable the ChangeLog system
        service.getChangeLog().setEnabled( false );
        service.setDenormalizeOpAttrsEnabled( true );

        // Now we can create as many partitions as we need
        // Create some new partitions named 'foo', 'bar' and 'apache'.
        Partition fooPartition = addPartition( "foo", "dc=foo,dc=com" );
        Partition barPartition = addPartition( "bar", "dc=bar,dc=com" );
        Partition apachePartition = addPartition( "apache", "dc=apache,dc=org" );

        // Index some attributes on the apache partition
        addIndex( apachePartition, "objectClass", "ou", "uid" );

        // And start the service
        service.startup();

        // Inject the foo root entry if it does not already exist
        try
        {
            service.getAdminSession().lookup( fooPartition.getSuffixDn() );
        }
        catch ( LdapException lnnfe )
        {
            DN dnFoo = new DN( "dc=foo,dc=com" );
            ServerEntry entryFoo = service.newEntry( dnFoo );
            entryFoo.add( "objectClass", "top", "domain", "extensibleObject" );
            entryFoo.add( "dc", "foo" );
            service.getAdminSession().add( entryFoo );
        }

        // Inject the bar root entry
        try
        {
            service.getAdminSession().lookup( barPartition.getSuffixDn() );
        }
        catch ( LdapException lnnfe )
        {
            DN dnBar = new DN( "dc=bar,dc=com" );
            ServerEntry entryBar = service.newEntry( dnBar );
            entryBar.add( "objectClass", "top", "domain", "extensibleObject" );
            entryBar.add( "dc", "bar" );
            service.getAdminSession().add( entryBar );
        }

        // Inject the apache root entry
        if ( !service.getAdminSession().exists( apachePartition.getSuffixDn() ) )
        {
            DN dnApache = new DN( "dc=Apache,dc=Org" );
            ServerEntry entryApache = service.newEntry( dnApache );
            entryApache.add( "objectClass", "top", "domain", "extensibleObject" );
            entryApache.add( "dc", "Apache" );
            service.getAdminSession().add( entryApache );
        }

        // We are all done !
    }


    /**
     * Creates a new instance of EmbeddedADS. It initializes the directory service.
     *
     * @throws Exception If something went wrong
     */
    public EmbeddedADService(File workDir) throws Exception
    {
        initDirectoryService( workDir );
    }


    /**
     * starts the LdapServer
     *
     * @throws Exception
     */
    public void startServer() throws Exception
    {
        server = new LdapServer();
        int serverPort = 10389;
        server.setTransports( new TcpTransport( serverPort ) );
        server.setDirectoryService( service );

        server.start();
    }


    /**
     * Main class.
     *
     * @param args Not used. 
     */
    public static void main( String[] args )
    {
        try
        {
            File workDir = new File( System.getProperty( "java.io.tmpdir" ) + "/server-work" );
            workDir.mkdirs();

            // Create the server
            EmbeddedADService ads = new EmbeddedADService( workDir );

            // Read an entry
            Entry result = ads.service.getAdminSession().lookup( new DN( "dc=apache,dc=org" ) );

            // And print it if available
            System.out.println( "Found entry : " + result );

            // optionally we can start a server too
            ads.startServer();
        }
        catch ( Exception e )
        {
            // Ok, we have something wrong going on ...
            e.printStackTrace();
        }
    }
}