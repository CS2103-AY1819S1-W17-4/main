package seedu.address.storage.xmlstorage;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.model.ReadOnlyTaskCollection;
import seedu.address.storage.TaskCollectionReadStorage;
import seedu.address.storage.TaskCollectionWriteStorage;

/**
 * A class to access TaskCollection data stored as an xml file on the hard disk.
 */
public class XmlTaskCollectionStorage implements TaskCollectionReadStorage, TaskCollectionWriteStorage {

    private static final Logger logger = LogsCenter.getLogger(XmlTaskCollectionStorage.class);

    private Path filePath;

    public XmlTaskCollectionStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getTaskCollectionFilePath() {
        return filePath;
    }

    @Override
    public Optional<ReadOnlyTaskCollection> readTaskCollection()
        throws DataConversionException, IOException {
        return readTaskCollection(filePath);
    }

    /**
     * Similar to {@link #readTaskCollection()}
     *
     * @param filePath location of the data. Cannot be null
     * @throws DataConversionException if the file is not in the correct format.
     */
    public Optional<ReadOnlyTaskCollection> readTaskCollection(Path filePath)
        throws DataConversionException,
        FileNotFoundException {
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            logger.info("TaskCollection file " + filePath + " not found");
            return Optional.empty();
        }

        XmlSerializableTaskCollection xmlDeadlineManager = XmlFileStorage.loadDataFromSaveFile(filePath);
        try {
            return Optional.of(xmlDeadlineManager.toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataConversionException(ive);
        }
    }

    @Override
    public void saveTaskCollection(ReadOnlyTaskCollection taskCollection) throws IOException {
        saveTaskCollection(taskCollection, filePath);
    }

    /**
     * Similar to {@link #saveTaskCollection(ReadOnlyTaskCollection)}
     *
     * @param filePath location of the data. Cannot be null
     */
    public void saveTaskCollection(ReadOnlyTaskCollection taskCollection, Path filePath) throws IOException {
        requireNonNull(taskCollection);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        XmlFileStorage.saveDataToFile(filePath, new XmlSerializableTaskCollection(taskCollection));
    }

}
