/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosfile.zosmf.manager.internal.properties.DirectoryListMaxItems;
import dev.galasa.zosfile.zosmf.manager.internal.properties.RestrictZosmfToImage;
import dev.galasa.zosfile.zosmf.manager.internal.properties.UnixFilePermissions;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestrictZosmfToImage.class, DirectoryListMaxItems.class, UnixFilePermissions.class})
public class TestZosFileHandlerImpl {
    
    private ZosFileHandlerImpl zosFileHandler;

    private ZosFileHandlerImpl zosFileHandlerSpy;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private ZosmfManagerImpl zosmfManagerMock;
    
    @Mock
    private IZosmfRestApiProcessor zosmfApiProcessorMock;
    
    @Mock
    private ZosDatasetImpl zosDatasetImplMock;
    
    @Mock
    private ZosVSAMDatasetImpl zosVSAMDatasetImplMock;
    
    @Mock
    private ZosUNIXFileImpl zosUNIXFileImplMock;

    private static final String DATASET_NAME = "DATA.SET.NAME";

    private static final String UNIX_FILE_NAME = "/unix/file";
    
    @Before
    public void setup() throws Exception {
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        PowerMockito.mockStatic(RestrictZosmfToImage.class);

        PowerMockito.mockStatic(DirectoryListMaxItems.class);
        PowerMockito.mockStatic(UnixFilePermissions.class);
        
        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictZosmfToImage.get(zosImageMock.getImageID()))).thenReturn(zosmfApiProcessorMock);
        ZosFileManagerImpl.setZosmfManager(zosmfManagerMock);

        zosFileHandler = new ZosFileHandlerImpl();
        zosFileHandlerSpy = Mockito.spy(zosFileHandler);
    }
    
    @Test
    public void testConstructor() {
        Assert.assertEquals("Constructor should return ", zosFileHandler.toString(), new ZosFileHandlerImpl("INTERNAL").toString());
    }
    
    @Test
    public void testNewDataset() throws ZosDatasetException {
        Object obj = zosFileHandlerSpy.newDataset(DATASET_NAME, zosImageMock);
        Assert.assertTrue("Error in newDataset() method", obj instanceof ZosDatasetImpl);
    }
    
    @Test
    public void testNewUNIXFile() throws Exception {
        Object obj = zosFileHandlerSpy.newUNIXFile(UNIX_FILE_NAME, zosImageMock);
        Assert.assertTrue("Error in newUNIXFile() method", obj instanceof ZosUNIXFileImpl);
    }
    
    @Test
    public void testNewVSAMDataset() throws ZosVSAMDatasetException {
        Object obj = zosFileHandlerSpy.newVSAMDataset(DATASET_NAME, zosImageMock);
        Assert.assertTrue("Error in newVSAMDataset() method", obj instanceof ZosVSAMDatasetImpl);
    }
    
    @Test
    public void testCleanupMethods() throws Exception {
        Mockito.doNothing().when(zosFileHandlerSpy).cleanupDatasets(Mockito.anyBoolean());
        Mockito.doNothing().when(zosFileHandlerSpy).cleanupVsamDatasets(Mockito.anyBoolean());
        Mockito.doNothing().when(zosFileHandlerSpy).cleanupUnixFiles(Mockito.anyBoolean());
        zosFileHandlerSpy.cleanup(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasets", Mockito.anyBoolean());
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
    }
    
    @Test
    public void testCleanupDatasets() throws Exception {
        List<ZosDatasetImpl> zosDatasets = new ArrayList<>();        
        Mockito.doReturn(false).when(zosDatasetImplMock).created();
        Mockito.doReturn(false).when(zosDatasetImplMock).exists();
        zosDatasets.add(zosDatasetImplMock);
        Whitebox.setInternalState(zosFileHandlerSpy, "zosDatasets", zosDatasets);
        zosFileHandlerSpy.cleanupDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(false).when(zosDatasetImplMock).created();
        Mockito.doReturn(true).when(zosDatasetImplMock).exists();        
        zosDatasets.add(zosDatasetImplMock);
        zosFileHandlerSpy.cleanupDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosDatasetImplMock).created();
        Mockito.doReturn(false).when(zosDatasetImplMock).exists();        
        zosDatasets.add(zosDatasetImplMock);
        zosFileHandlerSpy.cleanupDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosDatasetImplMock).created();
        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
        Mockito.doReturn(false).when(zosDatasetImplMock).isTemporary();
        Mockito.doReturn(false).when(zosDatasetImplMock).retainToTestEnd();
        Mockito.doReturn(true).when(zosDatasetImplMock).delete();
        zosDatasets.add(zosDatasetImplMock);
        zosFileHandlerSpy.cleanupDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosDatasetImplMock).created();
        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
        Mockito.doReturn(true).when(zosDatasetImplMock).isTemporary();
        Mockito.doNothing().when(zosDatasetImplMock).saveToResultsArchive();
        Mockito.doReturn(true).when(zosDatasetImplMock).retainToTestEnd();
        zosDatasets.add(zosDatasetImplMock);
        zosFileHandlerSpy.cleanupDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupDatasets", Mockito.anyBoolean());
        
        Mockito.doNothing().when(zosFileHandlerSpy).cleanupDatasetsTestComplete();
        zosFileHandlerSpy.cleanupDatasets(true);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupDatasetsTestComplete");
    }
    
    @Test
    public void testCleanupDatasetsTestComplete() throws Exception {
        List<ZosDatasetImpl> zosDatasets = new ArrayList<>();        
        Mockito.doReturn(false).when(zosDatasetImplMock).created();
        Mockito.doReturn(false).when(zosDatasetImplMock).exists();
        zosDatasets.add(zosDatasetImplMock);
        Whitebox.setInternalState(zosFileHandlerSpy, "zosDatasetsForCleanup", zosDatasets);
        zosFileHandlerSpy.cleanupDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupDatasetsTestComplete");
        
        Mockito.doReturn(false).when(zosDatasetImplMock).created();
        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
        zosDatasets.add(zosDatasetImplMock);
        zosFileHandlerSpy.cleanupDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupDatasetsTestComplete");
        
        Mockito.doReturn(true).when(zosDatasetImplMock).created();
        Mockito.doReturn(false).when(zosDatasetImplMock).exists();
        zosDatasets.add(zosDatasetImplMock);
        zosFileHandlerSpy.cleanupDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupDatasetsTestComplete");
        
        Mockito.doReturn(true).when(zosDatasetImplMock).created();
        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
        Mockito.doNothing().when(zosDatasetImplMock).saveToResultsArchive();
        Mockito.doReturn(true).when(zosDatasetImplMock).retainToTestEnd();
        zosDatasets.add(zosDatasetImplMock);
        zosFileHandlerSpy.cleanupDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupDatasetsTestComplete");
    }
    
    @Test
    public void testCleanupVsamDatasets() throws Exception {
        List<ZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();        
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        Whitebox.setInternalState(zosFileHandlerSpy, "zosVsamDatasets", zosVsamDatasets);
        zosFileHandlerSpy.cleanupVsamDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        zosFileHandlerSpy.cleanupVsamDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupVsamDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        zosFileHandlerSpy.cleanupVsamDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupVsamDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).retainToTestEnd();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).delete();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        zosFileHandlerSpy.cleanupVsamDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupVsamDatasets", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
        Mockito.doNothing().when(zosVSAMDatasetImplMock).saveToResultsArchive();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).retainToTestEnd();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        zosFileHandlerSpy.cleanupVsamDatasets(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupVsamDatasets", Mockito.anyBoolean());
        
        Mockito.doNothing().when(zosFileHandlerSpy).cleanupVsamDatasetsTestComplete();
        zosFileHandlerSpy.cleanupVsamDatasets(true);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasetsTestComplete");
    }
    
    @Test
    public void testCleanupVsamDatasetsTestComplete() throws Exception {
        List<ZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();        
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        Whitebox.setInternalState(zosFileHandlerSpy, "zosVsamDatasetsForCleanup", zosVsamDatasets);
        zosFileHandlerSpy.cleanupVsamDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasetsTestComplete");
        
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        zosFileHandlerSpy.cleanupVsamDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupVsamDatasetsTestComplete");
        
        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        zosFileHandlerSpy.cleanupVsamDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupVsamDatasetsTestComplete");
        
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
        Mockito.doNothing().when(zosVSAMDatasetImplMock).saveToResultsArchive();
        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).retainToTestEnd();
        zosVsamDatasets.add(zosVSAMDatasetImplMock);
        zosFileHandlerSpy.cleanupVsamDatasetsTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupVsamDatasetsTestComplete");
    }
    
    @Test
    public void testCleanupUnixFiles() throws Exception {
        List<ZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();        
        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
        zosUnixFiles.add(zosUNIXFileImplMock);
        Whitebox.setInternalState(zosFileHandlerSpy, "zosUnixFiles", zosUnixFiles);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(6)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(7)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).retainToTestEnd();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).delete();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(8)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
        Mockito.doNothing().when(zosUNIXFileImplMock).saveToResultsArchive();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).retainToTestEnd();
        zosUnixFiles.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFiles(false);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(9)).invoke("cleanupUnixFiles", Mockito.anyBoolean());
        
        Mockito.doNothing().when(zosFileHandlerSpy).cleanupUnixFilesTestComplete();
        zosFileHandlerSpy.cleanupUnixFiles(true);
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFilesTestComplete");
    }
    
    @Test
    public void testCleanupUnixFilesTestComplete() throws Exception {
        List<ZosUNIXFileImpl> zosUnixFilesForCleanup = new ArrayList<>();        
        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
        zosUnixFilesForCleanup.add(zosUNIXFileImplMock);
        Whitebox.setInternalState(zosFileHandlerSpy, "zosUnixFilesForCleanup", zosUnixFilesForCleanup);
        zosFileHandlerSpy.cleanupUnixFilesTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFilesTestComplete");
        
        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
        zosUnixFilesForCleanup.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFilesTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupUnixFilesTestComplete");
        
        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
        zosUnixFilesForCleanup.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFilesTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupUnixFilesTestComplete");
        
        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
        Mockito.doNothing().when(zosUNIXFileImplMock).saveToResultsArchive();
        Mockito.doReturn(true).when(zosUNIXFileImplMock).retainToTestEnd();
        zosUnixFilesForCleanup.add(zosUNIXFileImplMock);
        zosFileHandlerSpy.cleanupUnixFilesTestComplete();
        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupUnixFilesTestComplete");
    }
}
