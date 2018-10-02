package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

import seedu.address.logic.CommandHistory;
import seedu.address.model.Model;
import seedu.address.model.task.Task;

/**
 * Sort all tasks in the deadline manager by their name (aka title).
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE =
            COMMAND_WORD + ": Sort the tasks using a user defined custom operator "
                    + "and displays them as a list with index numbers.\n"
                    + "Parameters: SORT_COMPARATOR [SORT_COMPARATORS]...\n"
                    + "Example 1: " + COMMAND_WORD + " name> due<\n"
                    + "Example 2: " + COMMAND_WORD + " d>\n";

    public static final String MESSAGE_SUCCESS = "Sorted list.";

    private final Comparator<Task> comparator;

    public SortCommand(Comparator<Task> comparator) {
        this.comparator = comparator;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) {
        requireNonNull(model);
        model.updateSortedPersonList(comparator);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
