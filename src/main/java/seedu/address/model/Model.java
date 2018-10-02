package seedu.address.model;

import java.util.Comparator;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.model.task.Task;

/**
 * The API of the Model component.
 */
public interface Model {

    /**
     * {@code Predicate} that always evaluate to true
     */
    Predicate<Task> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Clears existing backing model and replaces with the provided new data.
     */
    void resetData(ReadOnlyTaskCollection newData);

    /**
     * Returns the TaskCollection
     */
    ReadOnlyTaskCollection getAddressBook();

    /**
     * Returns true if a task with the same identity as {@code task} exists in the deadline manager.
     */
    boolean hasPerson(Task task);

    /**
     * Deletes the given task. The task must exist in the deadline manager.
     */
    void deletePerson(Task target);

    /**
     * Adds the given task. {@code task} must not already exist in the deadline manager.
     */
    void addPerson(Task task);

    /**
     * Replaces the given task {@code target} with {@code editedTask}. {@code target} must exist in
     * the deadline manager. The task identity of {@code editedTask} must not be the same as another
     * existing task in the deadline manager.
     */
    void updatePerson(Task target, Task editedTask);

    /**
     * Returns an unmodifiable view of the filtered task list
     */
    ObservableList<Task> getFilteredPersonList();

    /**
     * Updates the filter of the filtered task list to filter by the given {@code predicate}.
     *
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Task> predicate);

    /**
     * Updates the sorted order of the tasks according by the given {@code comparator}.
     *
     * @throws NullPointerException if {@code comparator} is null.
     */
    void updateSortedPersonList(Comparator<Task> comparator);

    /**
     * Returns true if the model has previous deadline manager states to restore.
     */
    boolean canUndoAddressBook();

    /**
     * Returns true if the model has undone deadline manager states to restore.
     */
    boolean canRedoAddressBook();

    /**
     * Restores the model's deadline manager to its previous state.
     */
    void undoAddressBook();

    /**
     * Restores the model's deadline manager to its previously undone state.
     */
    void redoAddressBook();

    /**
     * Saves the current deadline manager state for undo/redo.
     */
    void commitAddressBook();
}
