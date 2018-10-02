package seedu.address.model.task;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.model.task.exceptions.InvalidPredicateOperatorException;

/**
 * Represents a Task's name in the deadline manager. Guarantees: immutable; is valid as declared in
 * {@link #isValidName(String)}
 */
public class Name implements Comparable<Name> {

    public static final String MESSAGE_NAME_CONSTRAINTS =
        "Names should only contain alphanumeric characters and spaces, and it should not be blank";

    /*
     * The first character of the address must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String NAME_VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public final String value;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_NAME_CONSTRAINTS);
        value = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(NAME_VALIDATION_REGEX);
    }

    /**
     * Constructs a predicate from the given operator and test phrase.
     *
     * @param operator   The operator for this predicate.
     * @param testPhrase The test phrase for this predicate.
     */
    public static Predicate<Name> makeFilter(FilterOperator operator, String testPhrase)
            throws InvalidPredicateOperatorException {
        switch (operator) {
        case EQUAL:
            return name -> name.value.equals(testPhrase);
        case LESS:
            return name -> StringUtil.containsFragmentIgnoreCase(testPhrase, name.value);
        case CONVENIENCE: // convenience operator, works the same as ">"
        case GREATER:
            return name -> StringUtil.containsFragmentIgnoreCase(name.value, testPhrase);
        default:
            throw new InvalidPredicateOperatorException();
        }
    }


    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
            || (other instanceof Name // instanceof handles nulls
            && value.equals(((Name) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public int compareTo(Name other) {
        return this.value.compareTo(other.value);
    }

}
