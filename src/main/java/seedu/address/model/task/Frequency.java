package seedu.address.model.task;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.function.Predicate;

import seedu.address.model.task.exceptions.InvalidPredicateOperatorException;
import seedu.address.model.task.exceptions.InvalidPredicateTestPhraseException;

/**
 * Represents a Task's frequency in the deadline manager. Guarantees: immutable; is valid as declared
 * in {@link #isValidFrequency(String)}
 */
public class Frequency implements Comparable<Frequency> {


    public static final String MESSAGE_FREQUENCY_CONSTRAINTS =
            "Frequency should only be a non-negative integer";
    public static final String FREQUENCY_VALIDATION_REGEX = "[0-9]+";
    public static final String NO_FREQUENCY = "0";
    public final int value;

    /**
     * Constructs a {@code Frequency}.
     *
     * @param frequency A valid frequency.
     */
    public Frequency(String frequency) {
        requireNonNull(frequency);
        checkArgument(isValidFrequency(frequency), MESSAGE_FREQUENCY_CONSTRAINTS);
        this.value = Integer.parseInt(frequency);
    }

    public Frequency(int frequency) {
        checkArgument(isValidFrequency(frequency), MESSAGE_FREQUENCY_CONSTRAINTS);
        this.value = frequency;
    }

    /**
     * Returns true if {@code value} is equal to 0.
     */
    public boolean isZero() {
        return value == 0;
    }

    /**
     * Returns true if a given integer is a valid frequency number.
     */
    public static boolean isValidFrequency(int test) {
        return test >= 0;
    }

    /**
     * Returns true if a given string is a valid frequency number.
     */
    public static boolean isValidFrequency(String test) {
        return test.matches(FREQUENCY_VALIDATION_REGEX);
    }

    /**
     * Constructs a predicate from the given operator and test phrase.
     *
     * @param operator   The operator for this predicate.
     * @param testPhrase The test phrase for this predicate.
     */
    public static Predicate<Frequency> makeFilter(FilterOperator operator, String testPhrase)
            throws InvalidPredicateTestPhraseException, InvalidPredicateOperatorException {
        Frequency tmpFrequency;
        try {
            tmpFrequency = new Frequency(testPhrase);
        } catch (IllegalArgumentException e) {
            throw new InvalidPredicateTestPhraseException(e);
        }
        switch (operator) {
        case EQUAL:
            return frequency -> frequency.equals(tmpFrequency);
        case CONVENIENCE: // convenience operator, works the same as "<"
            //Fallthrough
        case LESS:
            return frequency -> frequency.compareTo(tmpFrequency) <= 0;
        case GREATER:
            return frequency -> frequency.compareTo(tmpFrequency) >= 0;
        default:
            throw new InvalidPredicateOperatorException();
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Frequency // instanceof handles nulls
                && value == ((Frequency) other).value); // state check
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public int compareTo(Frequency other) {
        int booleanCompareResult = Boolean.compare(this.value == 0, other.value == 0);
        if (booleanCompareResult != 0) {
            return booleanCompareResult;
        }
        return Integer.compare(this.value, other.value);
    }
}
