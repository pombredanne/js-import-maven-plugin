package org.codehaus.mojo.jsimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.matchers.JUnitMatchers.either;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.junit.Test;

public class JsFileArtifactHandlerTest
{
    @Test
    public void testGetFileFromJsArtifact()
        throws IOException
    {
        Artifact artifact = mock( Artifact.class );
        File artifactFile = mock( File.class );
        File targetFolder = mock( File.class );
        File workFolder = mock( File.class );
        boolean allowJSResources = false;

        when( artifact.getType() ).thenReturn( "js" );
        when( artifact.getFile() ).thenReturn( artifactFile );

        JsFileArtifactHandler handler = new JsFileArtifactHandler( artifact, targetFolder, workFolder, allowJSResources );

        verifyZeroInteractions( targetFolder );
        verifyZeroInteractions( workFolder );

        List<File> jsFiles = handler.getFiles();
        assertEquals( 1, jsFiles.size() );
        assertEquals( artifactFile, jsFiles.get( 0 ) );

        assertNull( handler.getExpansionFolder() );
    }

    @Test
    public void testGetFilesFromWwwZipArtifact()
        throws IOException, URISyntaxException
    {
        Artifact artifact = mock( Artifact.class );
        File artifactFile =
            new File( JsFileArtifactHandlerTest.class.getResource( "bootstrap-amd-1.4.0-SNAPSHOT-www.zip" ).toURI() );
        File targetFolder = new File( System.getProperty( "java.io.tmpdir" ) );
        File workFolder = new File( System.getProperty( "java.io.tmpdir" ) );
        File expansionFolder =
            new File( targetFolder, "www-zip" + File.separator + "bootstrap-amd-1.4.0-SNAPSHOT-www.zip" );
        boolean allowJSResources = false;

        FileUtils.deleteQuietly( expansionFolder );

        expansionFolder.delete();

        when( artifact.getType() ).thenReturn( "zip" );
        when( artifact.getClassifier() ).thenReturn( "www" );
        when( artifact.getGroupId() ).thenReturn( "some.groupId" );
        when( artifact.getArtifactId() ).thenReturn( "anArtifactId" );
        when( artifact.getVersion() ).thenReturn( "1.0-SNAPSHOT" );
        when( artifact.getFile() ).thenReturn( artifactFile );

        JsFileArtifactHandler handler = new JsFileArtifactHandler( artifact, targetFolder, workFolder, allowJSResources );

        List<File> jsFiles = handler.getFiles();
        assertEquals( 2, jsFiles.size() );
        File jsFile = jsFiles.get( 0 );
        assertThat(jsFile.getName(), either(containsString("bootstrap-amd-1.4.0-SNAPSHOT.js")).or(containsString("bootstrap-amd-1.4.0-SNAPSHOT-min.js")));
        jsFile = jsFiles.get( 1 );
        assertThat(jsFile.getName(), either(containsString("bootstrap-amd-1.4.0-SNAPSHOT.js")).or(containsString("bootstrap-amd-1.4.0-SNAPSHOT-min.js")));
        jsFile = jsFile.getParentFile();
        assertEquals( "1.0-SNAPSHOT", jsFile.getName() );
        jsFile = jsFile.getParentFile();
        assertEquals( "anArtifactId", jsFile.getName() );
        jsFile = jsFile.getParentFile();
        assertEquals( "groupId", jsFile.getName() );
        jsFile = jsFile.getParentFile();
        assertEquals( "some", jsFile.getName() );

//        File cssFolder = new File( targetFolder, "css" );
//        File[] cssFiles = cssFolder.listFiles();
//        assertEquals( 1, cssFiles.length );
//        assertEquals( "bootstrap.css", cssFiles[0].getName() );

        Collection<File> expandedJsFiles = FileUtils.listFiles( expansionFolder, null, true );
        assertEquals( 2, expandedJsFiles.size() );
        File expandedJsFile = expandedJsFiles.iterator().next();
        assertThat(expandedJsFile.getName(), either(containsString("bootstrap-amd-1.4.0-SNAPSHOT.js")).or(containsString("bootstrap-amd-1.4.0-SNAPSHOT-min.js")));
        expandedJsFile = expandedJsFiles.iterator().next();
        assertThat(expandedJsFile.getName(), either(containsString("bootstrap-amd-1.4.0-SNAPSHOT.js")).or(containsString("bootstrap-amd-1.4.0-SNAPSHOT-min.js")));
        expandedJsFile = expandedJsFile.getParentFile();
        assertEquals( "1.0-SNAPSHOT", expandedJsFile.getName() );
        expandedJsFile = expandedJsFile.getParentFile();
        assertEquals( "anArtifactId", expandedJsFile.getName() );
        expandedJsFile = expandedJsFile.getParentFile();
        assertEquals( "groupId", expandedJsFile.getName() );
        expandedJsFile = expandedJsFile.getParentFile();
        assertEquals( "some", expandedJsFile.getName() );

        assertEquals( expansionFolder, handler.getExpansionFolder() );

        // Run the expansion again ensuring that we use what we've previously expanded i.e. don't inflate again.
        long jsExpansionFolderTime = expansionFolder.lastModified();
        new JsFileArtifactHandler( artifact, targetFolder, workFolder, allowJSResources );
        assertEquals( jsExpansionFolderTime, expansionFolder.lastModified() );
    }
}
