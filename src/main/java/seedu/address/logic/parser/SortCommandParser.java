package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Comparator;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.task.Task;

/**
 * Parses input arguments and creates a new SortCommand object
 */
public class SortCommandParser implements Parser<SortCommand> {

    private static final String MESSAGE_INVALID_KEY_FORMAT = "Invalid key: %1$s";

    /**
     * Parses the given {@code String} of arguments in the context of the SortCommand and returns an
     * SortCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public SortCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        }

        String[] splitedArgs = trimmedArgs.split(" ");

        // pattern that matches things like:
        // due< name>
        // n<

        Comparator<Task> comparator = Comparator.comparing(Task::getClass, (a, b) -> {
            return 0; // a default comparator which compares every task equal
        });

        for (String element: splitedArgs) {

            if (element.isEmpty()) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
            }

            final String taskField = element.substring(0, element.length() - 1);
            final char comparisonCharacter = element.charAt(element.length() - 1);

            if (comparisonCharacter != '>' && comparisonCharacter != '<') {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_KEY_FORMAT, element));
            }

            switch (taskField) {
            case "n": // fallthrough
            case "name": {
                if (comparisonCharacter == '<') {
                    comparator = comparator.thenComparing(Task::getName);
                } else {
                    comparator = comparator.thenComparing(Task::getName, Comparator.reverseOrder());
                }
                break;
            }
            case "d": // fallthrough
            case "due": {
                if (comparisonCharacter == '<') {
                    comparator = comparator.thenComparing(Task::getDeadline);
                } else {
                    comparator = comparator.thenComparing(Task::getDeadline, Comparator.reverseOrder());
                }
                break;
            }
            default: {
                throw new ParseException(String.format(MESSAGE_INVALID_KEY_FORMAT, element));
            }
            }
        }

        return new SortCommand(comparator);
    }

}
