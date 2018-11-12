package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_TASKS;

import java.util.List;
import java.util.Set;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.attachment.Attachment;
import seedu.address.model.tag.Tag;
import seedu.address.model.task.Deadline;
import seedu.address.model.task.Frequency;
import seedu.address.model.task.Name;
import seedu.address.model.task.Priority;
import seedu.address.model.task.Task;

/**
 * Completes the details of an existing task in the deadline manager.
 */
public class CompleteCommand extends Command {

    public static final String COMMAND_WORD = "complete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Complete a task. If the task is not a recurring task, it will be deleted. "
            + "Otherwise, the deadline will be changed to the next occurrence of the task.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_COMPLETE_RECURRING_TASK_SUCCESS = "Completed. Next occurrence: %1$s";
    public static final String MESSAGE_COMPLETE_NON_RECURRING_TASK_SUCCESS = "Completed.";

    private final Index index;

    /**
     * @param index of the task in the filtered task list to complete
     */
    public CompleteCommand(Index index) {
        requireNonNull(index);

        this.index = index;
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToComplete} completed with {@code
     * completeTaskDescriptor}.
     */
    private static Task createCompletedTask(Task taskToComplete) {
        assert taskToComplete != null;

        Name updatedName = taskToComplete.getName();
        Priority updatedPriority = taskToComplete.getPriority();
        Frequency updatedFrequency = taskToComplete.getFrequency();
        Deadline updatedDeadline = taskToComplete.getDeadline().addDays(updatedFrequency.value);
        Set<Tag> updatedTags = taskToComplete.getTags();

        // Attachments are not modifiable via 'CompleteCommand'
        Set<Attachment> updatedAttachments = taskToComplete.getAttachments();

        return new Task(updatedName, updatedPriority, updatedFrequency, updatedDeadline, updatedTags,
                updatedAttachments);
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);
        List<Task> lastShownList = model.getFilteredTaskList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        Task taskToComplete = lastShownList.get(index.getZeroBased());
        if (taskToComplete.getFrequency().isZero()) {
            model.deleteTask(taskToComplete);
            model.commitTaskCollection();
            return new CommandResult(String.format(MESSAGE_COMPLETE_NON_RECURRING_TASK_SUCCESS, taskToComplete));
        } else {
            Task completedTask = createCompletedTask(taskToComplete);

            model.updateTask(taskToComplete, completedTask);
            model.updateFilteredTaskList(PREDICATE_SHOW_ALL_TASKS);
            model.commitTaskCollection();
            return new CommandResult(String.format(MESSAGE_COMPLETE_RECURRING_TASK_SUCCESS,
                    completedTask.getDeadline()));
        }
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CompleteCommand)) {
            return false;
        }

        // state check
        CompleteCommand e = (CompleteCommand) other;
        return index.equals(e.index);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("index: ").append(index);
        return builder.toString();
    }
}
