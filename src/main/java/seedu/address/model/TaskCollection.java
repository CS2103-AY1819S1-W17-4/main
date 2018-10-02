package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.task.Task;
import seedu.address.model.task.exceptions.TaskNotFoundException;

/**
 * Wraps all data at the address-book level Duplicates are not allowed (by .isSameTask comparison)
 */
public class TaskCollection implements ReadOnlyTaskCollection {

    private final ObservableList<Task> tasks;

    public TaskCollection() {
        tasks = FXCollections.observableArrayList();
    }

    /**
     * Creates an TaskCollection using the Persons in the {@code toBeCopied}
     */
    public TaskCollection(ReadOnlyTaskCollection toBeCopied) {
        this(); // delegating construction to non-parameterized constructor
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the task list with {@code tasks}. {@code tasks} must not contain
     * duplicate tasks.
     */
    public void setTasks(List<Task> tasks) {
        this.tasks.setAll(tasks);
    }

    /**
     * Resets the existing data of this {@code TaskCollection} with {@code newData}.
     */
    public void resetData(ReadOnlyTaskCollection newData) {
        requireNonNull(newData);

        setTasks(newData.getTaskList());
    }

    //// task-level operations

    /**
     * Returns true if a task with the same identity as {@code task} exists in the deadline manager.
     */
    public boolean hasTask(Task task) {
        requireNonNull(task);
        return tasks.contains(task);
    }

    /**
     * Adds a task to the deadline manager. The task must not already exist in the deadline manager.
     */
    public void addPerson(Task task) {
        tasks.add(task);
    }

    /**
     * Replaces the given task {@code target} in the list with {@code editedTask}. {@code target}
     * must exist in the deadline manager. The task identity of {@code editedTask} must not be the same
     * as another existing task in the deadline manager.
     */
    public void updateTask(Task target, Task editedTask) {
        requireNonNull(editedTask);

        int index = tasks.indexOf(target);
        if (index == -1) {
            throw new TaskNotFoundException();
        }

        tasks.set(index, editedTask);
    }

    /**
     * Removes {@code key} from this {@code TaskCollection}. {@code key} must exist in the address
     * book.
     */
    public void removeTask(Task key) {
        tasks.remove(key);
    }

    /**
     * Sorts the ObservableList by custom comparator
     */
    public void sort(Comparator<Task> comparator) {
        FXCollections.sort(tasks, comparator);
    }

    //// util methods

    @Override
    public String toString() {
        return tasks.size() + " tasks";
        // TODO: refine later
    }

    @Override
    public ObservableList<Task> getTaskList() {
        return FXCollections.unmodifiableObservableList(tasks);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
            || (other instanceof TaskCollection // instanceof handles nulls
            && tasks.equals(((TaskCollection) other).tasks));
    }

    @Override
    public int hashCode() {
        return tasks.hashCode();
    }
}
