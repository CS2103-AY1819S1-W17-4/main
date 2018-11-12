package seedu.address.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static seedu.address.testutil.TypicalTasks.getTypicalTaskCollections;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.address.commons.events.BaseEvent;
import seedu.address.commons.events.model.ExportRequestEvent;
import seedu.address.commons.events.model.ImportRequestEvent;
import seedu.address.commons.events.model.TaskCollectionChangedEvent;
import seedu.address.commons.events.storage.DataSavingExceptionEvent;
import seedu.address.commons.events.storage.ImportDataAvailableEvent;
import seedu.address.commons.events.storage.ImportExportExceptionEvent;
import seedu.address.model.ReadOnlyTaskCollection;
import seedu.address.model.TaskCollection;
import seedu.address.model.UserPrefs;
import seedu.address.storage.xmlstorage.XmlTaskCollectionStorage;
import seedu.address.testutil.Assert;
import seedu.address.ui.testutil.EventsCollectorRule;

public class StorageManagerTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();

    private StorageManager storageManager;

    @Before
    public void setUp() {
        XmlTaskCollectionStorage taskCollectionStorage = new XmlTaskCollectionStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        storageManager = new StorageManager(taskCollectionStorage, userPrefsStorage);
    }

    private Path getTempFilePath(String fileName) {
        return testFolder.getRoot().toPath().resolve(fileName);
    }


    @Test
    public void prefsReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonUserPrefsStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonUserPrefsStorageTest} class.
         */
        UserPrefs original = new UserPrefs();
        original.setGuiSettings(300, 600, 4, 6);
        storageManager.saveUserPrefs(original);
        UserPrefs retrieved = storageManager.readUserPrefs().get();
        assertEquals(original, retrieved);
    }

    @Test
    public void taskCollectionReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link XmlTaskCollectionStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link XmlTaskCollectionStorageTest} class.
         */
        TaskCollection original = getTypicalTaskCollections();
        storageManager.saveTaskCollection(original);
        ReadOnlyTaskCollection retrieved = storageManager.readTaskCollection().get();
        assertEquals(original, new TaskCollection(retrieved));
    }

    @Test
    public void getTaskCollectionFilePath() {
        assertNotNull(storageManager.getTaskCollectionFilePath());
    }

    @Test
    public void handleTaskCollectionChangedEvent_exceptionThrown_eventRaised() {
        // Create a StorageManager while injecting a stub that  throws an exception when the save method is called
        Storage storage = new StorageManager(
            new XmlTaskCollectionStorageExceptionThrowingStub(Paths.get("dummy")),
            new JsonUserPrefsStorage(Paths.get("dummy")));
        storage.handleTaskCollectionChangedEvent(new TaskCollectionChangedEvent(new TaskCollection()));
        assertTrue(eventsCollectorRule.eventsCollector
            .getMostRecent() instanceof DataSavingExceptionEvent);
    }

    @Test
    public void exportOnExistingFile_exceptionThrown() throws IOException {
        // Exporting with file name equal to the working file should throw IllegalValueException.
        TaskCollection original = getTypicalTaskCollections();
        storageManager.saveTaskCollection(original);
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_WRITE_FILE_EXISTS_ERROR, storageManager.getTaskCollectionFilePath()), () ->
            storageManager.exportTaskCollection(original, storageManager.getTaskCollectionFilePath(),
                false, false));

        // Exporting with CSV file name equal to the working file should throw IllegalValueException.
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_WRITE_FILE_EXISTS_ERROR, storageManager.getTaskCollectionFilePath()), () ->
                storageManager.exportTaskCollection(original, storageManager.getTaskCollectionFilePath(),
                    false, true));
    }

    @Test
    public void exportNewFile_success() {
        TaskCollection original = getTypicalTaskCollections();
        try {
            storageManager.exportTaskCollection(original, getTempFilePath("exportNewNonExistent"),
                false, false);
            storageManager.exportTaskCollection(original, getTempFilePath("exportNewNonExistent2"),
                false, true);
        } catch (IOException ioe) {
            throw new AssertionError("Export on non-existent file should not throw.");
        }
    }

    @Test
    public void overwriteExportOnExistingFile_success() throws IOException {
        // Exporting with file name equal to the working file should throw IllegalValueException.
        TaskCollection original = getTypicalTaskCollections();
        storageManager.saveTaskCollection(original);
        try {
            storageManager.exportTaskCollection(original, storageManager.getTaskCollectionFilePath(),
                true, false);
            storageManager.exportTaskCollection(original, storageManager.getTaskCollectionFilePath(),
                true, true);
        } catch (IOException ioe) {
            throw new AssertionError("Export on non-existent file should not throw.");
        }
    }

    @Test
    public void overwriteExportOnUnwritableFile_exceptionThrown() throws IOException {
        // Exporting with file name "." should throw as directory is unwritable.
        TaskCollection original = getTypicalTaskCollections();
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_WRITE_FILE_NO_PERMISSION_ERROR, getTempFilePath(".")), () ->
            storageManager.exportTaskCollection(original, getTempFilePath("."),
                false, false));
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_WRITE_FILE_NO_PERMISSION_ERROR, getTempFilePath(".")), () ->
                storageManager.exportTaskCollection(original, getTempFilePath("."),
                    true, false));
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_WRITE_FILE_NO_PERMISSION_ERROR, getTempFilePath(".")), () ->
                storageManager.exportTaskCollection(original, getTempFilePath("."),
                    false, true));
    }

    @Test
    public void exportExistingFileCapitalisation_unix_success() {
        org.junit.Assume.assumeFalse(System.getProperty("os.name").startsWith("Windows"));
        TaskCollection original = getTypicalTaskCollections();
        try {
            storageManager.exportTaskCollection(original, getTempFilePath("exportNewNonExistent"),
                false, false);
            storageManager.exportTaskCollection(original, getTempFilePath("exportNewNonEXISTENT"),
                false, true);
        } catch (IOException ioe) {
            throw new AssertionError("Export on non-existent file should not throw.");
        }
    }

    @Test
    public void exportExistingFileCapitalisation_windows_exceptionThrown() throws IOException {
        org.junit.Assume.assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        TaskCollection original = getTypicalTaskCollections();
        storageManager.exportTaskCollection(original, getTempFilePath("exportNewNonExistent"),
            false, false);
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_WRITE_FILE_EXISTS_ERROR, getTempFilePath("exportNewNonEXISTENT")), () ->
                storageManager.exportTaskCollection(original, getTempFilePath("exportNewNonEXISTENT"),
                    false, true));
    }

    @Test
    public void importNonExistingFile_exceptionThrown() {
        Assert.assertThrows(IOException.class, Storage.MESSAGE_READ_FILE_MISSING_ERROR, () ->
                storageManager.importTaskCollection(getTempFilePath("nonExistent")));
    }

    @Test
    public void importUnreadableFile_exceptionThrown() throws IOException {
        String rubbishFile = "rubbishFile";
        createTempFile(rubbishFile);
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_READ_FILE_PARSE_ERROR, getTempFilePath(rubbishFile)), () ->
                storageManager.importTaskCollection(getTempFilePath(rubbishFile)));
        removeTempFile(rubbishFile);
    }

    private void removeTempFile(String filename) {
        File delete = new File(getTempFilePath(filename).toString());
        delete.delete();
    }

    private void createTempFile(String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(getTempFilePath(filename).toString());
        fileWriter.write("garbage");
        fileWriter.close();
    }

    @Test
    public void importExistingFile_success() {
        TaskCollection original = getTypicalTaskCollections();
        try {
            storageManager.saveTaskCollection(original);
            ReadOnlyTaskCollection read = storageManager
                    .importTaskCollection(storageManager.getTaskCollectionFilePath()).get();
        } catch (IOException e) {
            throw new AssertionError("Import on existent file should not throw.");
        }
    }


    @Test
    public void handleImportRequestEvent_validEvent_importDataAvailableEventRaised() {
        TaskCollection original = getTypicalTaskCollections();
        try {
            storageManager.saveTaskCollection(original);
            storageManager.handleImportRequestEvent(
                new ImportRequestEvent(storageManager.getTaskCollectionFilePath().toString()));
            BaseEvent event = eventsCollectorRule.eventsCollector.getMostRecent();
            assertTrue(event instanceof ImportDataAvailableEvent);
            assertTrue(((ImportDataAvailableEvent) event).data.equals(original));
        } catch (IOException e) {
            throw new AssertionError("Import on existent file should not throw.");
        }
    }

    @Test
    public void handleImportRequestEvent_nullEvent_importExportExceptionEventRaised() {
        storageManager.handleImportRequestEvent(null);
        BaseEvent event = eventsCollectorRule.eventsCollector.getMostRecent();
        assertTrue(event instanceof ImportExportExceptionEvent);
    }

    @Test
    public void handleImportRequestEvent_invalidFilePath_importExportExceptionEventRaised() {
        storageManager.handleImportRequestEvent(
            new ImportRequestEvent(getTempFilePath("nonExistent").toString()));
        BaseEvent event = eventsCollectorRule.eventsCollector.getMostRecent();
        assertTrue(event instanceof ImportExportExceptionEvent);
    }

    @Test
    public void handleExportRequestEvent_validEvent_success() {
        TaskCollection original = getTypicalTaskCollections();
        storageManager.handleExportRequestEvent(
            new ExportRequestEvent(original, getTempFilePath("exportNewNonExistent").toString(),
                false, false));
        //Should not throw nor return anything.
    }

    @Test
    public void handleExportRequestEvent_nullEvent_importExportExceptionEventRaised() {
        storageManager.handleExportRequestEvent(null);
        BaseEvent event = eventsCollectorRule.eventsCollector.getMostRecent();
        assertTrue(event instanceof ImportExportExceptionEvent);
    }

    @Test
    public void handleExportRequestEvent_invalidFilePath_importExportExceptionEventRaised() {
        TaskCollection original = getTypicalTaskCollections();
        storageManager.handleExportRequestEvent(
            new ExportRequestEvent(original, getTempFilePath(".").toString(),
                false, false));
        BaseEvent event = eventsCollectorRule.eventsCollector.getMostRecent();
        assertTrue(event instanceof ImportExportExceptionEvent);
    }

    @Test
    public void taskCollectionExportImport() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link XmlTaskCollectionStorage} class.
         * More extensive testing of importing exporting is done in {@link XmlTaskCollectionStorageTest} class.
         */
        TaskCollection original = getTypicalTaskCollections();
        storageManager.exportTaskCollection(original, getTempFilePath("dummyExport"),
            false, false);
        ReadOnlyTaskCollection retrieved = storageManager.importTaskCollection(getTempFilePath("dummyExport"))
                                                         .get();
        assertEquals(original, new TaskCollection(retrieved));
        Assert.assertThrows(IOException.class,
            String.format(Storage.MESSAGE_WRITE_FILE_EXISTS_ERROR, getTempFilePath("dummyExport")), () ->
            storageManager.exportTaskCollection(original, getTempFilePath("dummyExport"),
                false, false));

        try {
            storageManager.exportTaskCollection(original, storageManager.getTaskCollectionFilePath(),
                true, false);
        } catch (IOException ioe) {
            throw new AssertionError("Export on non-existent file should not throw.");
        }

    }


    /**
     * A Stub class to throw an exception when the save method is called
     */
    class XmlTaskCollectionStorageExceptionThrowingStub extends XmlTaskCollectionStorage {

        public XmlTaskCollectionStorageExceptionThrowingStub(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveTaskCollection(ReadOnlyTaskCollection taskCollection, Path filePath)
            throws IOException {
            throw new IOException("dummy exception");
        }
    }


}
