package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.TaskCollectionChangedEvent;
import seedu.address.model.task.Task;

/**
 * Represents the in-memory model of the deadline manager data.
 */
public class ModelManager extends ComponentManager implements Model {

    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedTaskCollection versionedAddressBook;
    private final FilteredList<Task> filteredTasks;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyTaskCollection addressBook, UserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine(
            "Initializing with deadline manager: " + addressBook + " and user prefs " + userPrefs);

        versionedAddressBook = new VersionedTaskCollection(addressBook);
        filteredTasks = new FilteredList<>(versionedAddressBook.getTaskList());
    }

    public ModelManager() {
        this(new TaskCollection(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyTaskCollection newData) {
        versionedAddressBook.resetData(newData);
        indicateAddressBookChanged();
    }

    @Override
    public ReadOnlyTaskCollection getAddressBook() {
        return versionedAddressBook;
    }

    /**
     * Raises an event to indicate the model has changed
     */
    private void indicateAddressBookChanged() {
        raise(new TaskCollectionChangedEvent(versionedAddressBook));
    }

    @Override
    public boolean hasPerson(Task task) {
        requireNonNull(task);
        return versionedAddressBook.hasTask(task);
    }

    @Override
    public void deletePerson(Task target) {
        versionedAddressBook.removeTask(target);
        indicateAddressBookChanged();
    }

    @Override
    public void addPerson(Task task) {
        versionedAddressBook.addPerson(task);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        indicateAddressBookChanged();
    }

    @Override
    public void updatePerson(Task target, Task editedTask) {
        requireAllNonNull(target, editedTask);

        versionedAddressBook.updateTask(target, editedTask);
        indicateAddressBookChanged();
    }

    @Override
    public void updateSortedPersonList(Comparator<Task> comparator) {
        requireNonNull(comparator);
        versionedAddressBook.sort(comparator);
    }

    //=========== Filtered Task List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Task} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Task> getFilteredPersonList() {
        return FXCollections.unmodifiableObservableList(filteredTasks);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Task> predicate) {
        requireNonNull(predicate);
        filteredTasks.setPredicate(predicate);
    }

    //=========== Undo/Redo =================================================================================

    @Override
    public boolean canUndoAddressBook() {
        return versionedAddressBook.canUndo();
    }

    @Override
    public boolean canRedoAddressBook() {
        return versionedAddressBook.canRedo();
    }

    @Override
    public void undoAddressBook() {
        versionedAddressBook.undo();
        indicateAddressBookChanged();
    }

    @Override
    public void redoAddressBook() {
        versionedAddressBook.redo();
        indicateAddressBookChanged();
    }

    @Override
    public void commitAddressBook() {
        versionedAddressBook.commit();
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return versionedAddressBook.equals(other.versionedAddressBook)
            && filteredTasks.equals(other.filteredTasks);
    }

}
