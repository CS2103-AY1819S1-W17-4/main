package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.KEY_ATTACHMENT_LONG;
import static seedu.address.logic.parser.CliSyntax.KEY_ATTACHMENT_SHORT;
import static seedu.address.logic.parser.CliSyntax.KEY_DEADLINE_LONG;
import static seedu.address.logic.parser.CliSyntax.KEY_DEADLINE_MEDIUM;
import static seedu.address.logic.parser.CliSyntax.KEY_DEADLINE_SHORT;
import static seedu.address.logic.parser.CliSyntax.KEY_FREQUENCY_LONG;
import static seedu.address.logic.parser.CliSyntax.KEY_FREQUENCY_SHORT;
import static seedu.address.logic.parser.CliSyntax.KEY_NAME_LONG;
import static seedu.address.logic.parser.CliSyntax.KEY_NAME_SHORT;
import static seedu.address.logic.parser.CliSyntax.KEY_PRIORITY_LONG;
import static seedu.address.logic.parser.CliSyntax.KEY_PRIORITY_SHORT;
import static seedu.address.logic.parser.CliSyntax.KEY_TAG_LONG;
import static seedu.address.logic.parser.CliSyntax.KEY_TAG_SHORT;
import static seedu.address.ui.ResultDisplay.TEXT_STYLE_CLASS_DEFAULT;
import static seedu.address.ui.ResultDisplay.TEXT_STYLE_CLASS_ERROR;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.logic.parser.tokenizer.BooleanExpressionParser;
import seedu.address.logic.parser.tokenizer.StringTokenizer;
import seedu.address.logic.parser.tokenizer.exceptions.BooleanExpressionInvalidOperatorException;
import seedu.address.logic.parser.tokenizer.exceptions.BooleanExpressionMismatchedLeftBracketException;
import seedu.address.logic.parser.tokenizer.exceptions.BooleanExpressionMismatchedRightBracketException;
import seedu.address.logic.parser.tokenizer.exceptions.BooleanExpressionUnexpectedBinaryOperatorException;
import seedu.address.logic.parser.tokenizer.exceptions.BooleanExpressionUnexpectedEndOfStringException;
import seedu.address.logic.parser.tokenizer.exceptions.BooleanExpressionUnexpectedRightBracketException;
import seedu.address.logic.parser.tokenizer.exceptions.TokenizationEndOfStringException;
import seedu.address.logic.parser.tokenizer.exceptions.TokenizationException;
import seedu.address.logic.parser.tokenizer.exceptions.TokenizationInvalidPredicateException;
import seedu.address.logic.parser.tokenizer.exceptions.TokenizationMismatchException;
import seedu.address.logic.parser.tokenizer.exceptions.TokenizationMissingEndQuoteException;
import seedu.address.logic.parser.tokenizer.exceptions.TokenizationNoMatchableCharacterException;
import seedu.address.logic.parser.tokenizer.exceptions.TokenizationUnexpectedQuoteException;
import seedu.address.model.attachment.Attachment;
import seedu.address.model.tag.Tag;
import seedu.address.model.task.Deadline;
import seedu.address.model.task.FilterOperator;
import seedu.address.model.task.Frequency;
import seedu.address.model.task.Name;
import seedu.address.model.task.Priority;
import seedu.address.model.task.Task;
import seedu.address.model.task.exceptions.InvalidPredicateException;
import seedu.address.model.task.exceptions.InvalidPredicateKeyException;
import seedu.address.model.task.exceptions.InvalidPredicateOperatorException;
import seedu.address.model.task.exceptions.InvalidPredicateSetKeyException;
import seedu.address.model.task.exceptions.InvalidPredicateSetOperatorException;
import seedu.address.model.task.exceptions.InvalidPredicateTestPhraseException;
import seedu.address.model.util.SetUtil;
import seedu.address.ui.ResultDisplay;

/**
 * Parses input arguments and creates a new FilterCommand object
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    private static final Predicate<Task> ALWAYS_FALSE = task -> false;

    // used to match things like:
    // due=1/10/2018
    // test<blah
    // name>"hello world"
    // note: '/' is necessary for dates, ',' is necessary for tags
    private static final Predicate<Character> ALLOWED_UNQUOTED_CHARACTER_PREDICATE =
            ch -> (ch >= 'A' && ch <= 'Z')
                    || (ch >= 'a' && ch <= 'z')
                    || (ch >= '0' && ch <= '9')
                    || ch == '_'
                    || ch == '-'
                    || ch == '/'
                    || ch == '\\'
                    || ch == ','
                    || ch == '.';

    private static final Predicate<Character> ALLOWED_KEY_CHARACTER_PREDICATE =
            ch -> (ch >= 'A' && ch <= 'Z')
                    || (ch >= 'a' && ch <= 'z');

    private static final Pattern FILTER_OPERATOR_PATTERN = Pattern.compile("[\\=\\<\\>\\:]");

    private static final Logger logger = LogsCenter.getLogger(FilterCommandParser.class);

    /**
     * Creates a predicate that filters by name.
     */
    private static Predicate<Task> createNamePredicate(FilterOperator operator, String testPhrase)
            throws InvalidPredicateOperatorException {
        logger.info("Making filter for name " + operator.toString() + ' ' + testPhrase);
        Predicate<Name> namePredicate = Name.makeFilter(operator, testPhrase);
        return task -> namePredicate.test(task.getName());
    }

    /**
     * Creates a predicate that filters by deadline.
     */
    private static Predicate<Task> createDeadlinePredicate(FilterOperator operator, String testPhrase)
            throws InvalidPredicateTestPhraseException, InvalidPredicateOperatorException {
        logger.info("Making filter for deadline " + operator.toString() + ' ' + testPhrase);
        Predicate<Deadline> deadlinePredicate = Deadline.makeFilter(operator, testPhrase);
        return task -> deadlinePredicate.test(task.getDeadline());
    }

    /**
     * Creates a predicate that filters by priority.
     */
    private static Predicate<Task> createPriorityPredicate(FilterOperator operator, String testPhrase)
            throws InvalidPredicateTestPhraseException, InvalidPredicateOperatorException {
        logger.info("Making filter for priority " + operator.toString() + ' ' + testPhrase);
        Predicate<Priority> priorityPredicate = Priority.makeFilter(operator, testPhrase);
        return task -> priorityPredicate.test(task.getPriority());
    }

    /**
     * Creates a predicate that filters by frequency.
     */
    private static Predicate<Task> createFrequencyPredicate(FilterOperator operator, String testPhrase)
            throws InvalidPredicateTestPhraseException, InvalidPredicateOperatorException {
        logger.info("Making filter for frequency " + operator.toString() + ' ' + testPhrase);
        Predicate<Frequency> frequencyPredicate = Frequency.makeFilter(operator, testPhrase);
        return task -> frequencyPredicate.test(task.getFrequency());
    }

    /**
     * Creates a predicate that filters by tags.
     */
    private static Predicate<Task> createTagsPredicate(FilterOperator setOperator, FilterOperator fieldOperator,
                                                       String testPhrase)
            throws InvalidPredicateTestPhraseException, InvalidPredicateOperatorException,
            InvalidPredicateSetOperatorException {
        logger.info("Making filter for tag " + setOperator.toString()
                + ' ' + fieldOperator.toString() + ' ' + testPhrase);
        Predicate<Set<Tag>> tagsPredicate = SetUtil.makeFilter(Tag.class, setOperator, fieldOperator, testPhrase);
        return task -> tagsPredicate.test(task.getTags());
    }

    /**
     * Creates a predicate that filters by tags.
     */
    private static Predicate<Task> createAttachmentsPredicate(FilterOperator setOperator, FilterOperator fieldOperator,
                                                              String testPhrase)
            throws InvalidPredicateTestPhraseException, InvalidPredicateOperatorException,
            InvalidPredicateSetOperatorException {
        logger.info("Making filter for attachment " + setOperator.toString()
                + ' ' + fieldOperator.toString() + ' ' + testPhrase);
        Predicate<Set<Attachment>> attachmentsPredicate = SetUtil.makeFilter(Attachment.class,
                setOperator, fieldOperator, testPhrase);
        return task -> attachmentsPredicate.test(task.getAttachments());
    }

    /**
     * A functional interface that represents a supplier than can throw InvalidPredicateException.
     */
    @FunctionalInterface
    private interface ExceptionalSupplier<T> {
        T get() throws InvalidPredicateException;
    }

    /**
     * A functional interface that represents a supplier than can throw InvalidPredicateOperatorException.
     */
    @FunctionalInterface
    private interface PredicateOperatorExceptionalSupplier<T> {
        T get() throws InvalidPredicateOperatorException;
    }

    /**
     * A functional interface that represents a supplier than can throw InvalidPredicateOperatorException.
     */
    @FunctionalInterface
    private interface TokenizationExceptionalSupplier<T> {
        T get() throws TokenizationException;
    }

    /**
     * Invokes the supplier, and converts an InvalidPredicateException to a predicate that always returns false.
     *
     * @param supplier The supplier that either produces a predicate or throws an exception.
     * @return On success returns the predicate that is returned by the supplier,
     * on failure returns a predicate that is always false.
     */
    private static Predicate<Task> silencePredicateException(ExceptionalSupplier<Predicate<Task>> supplier) {
        try {
            return supplier.get();
        } catch (InvalidPredicateException e) {
            return ALWAYS_FALSE;
        }
    }

    /**
     * Creates a predicate from the specified key, operator, and test phrase.
     *
     * @param key        The key that refers to the specific field in a task.
     * @param operator   The filter operator.
     * @param testPhrase The test phrase to compare with the specific field of each task.
     * @return The predicate that is created.
     */
    private static Predicate<Task> createPredicate(String key, FilterOperator operator, String testPhrase)
            throws InvalidPredicateKeyException, InvalidPredicateOperatorException,
            InvalidPredicateSetOperatorException, InvalidPredicateTestPhraseException {

        switch (key) {
        case KEY_NAME_SHORT: // Fallthrough
        case KEY_NAME_LONG:
            return createNamePredicate(operator, testPhrase);
        case KEY_DEADLINE_SHORT: // Fallthrough
        case KEY_DEADLINE_MEDIUM: // Fallthrough
        case KEY_DEADLINE_LONG:
            return createDeadlinePredicate(operator, testPhrase);
        case KEY_PRIORITY_SHORT: // Fallthrough
        case KEY_PRIORITY_LONG:
            return createPriorityPredicate(operator, testPhrase);
        case KEY_FREQUENCY_SHORT: // Fallthrough
        case KEY_FREQUENCY_LONG:
            return createFrequencyPredicate(operator, testPhrase);
        case KEY_TAG_SHORT: // Fallthrough
        case KEY_TAG_LONG:
            return createTagsPredicate(FilterOperator.CONVENIENCE, operator, testPhrase);
        case KEY_ATTACHMENT_SHORT: // Fallthrough
        case KEY_ATTACHMENT_LONG:
            return createAttachmentsPredicate(FilterOperator.CONVENIENCE, operator, testPhrase);
        default:
            throw new InvalidPredicateKeyException();
        }
    }

    /**
     * Creates a predicate from the specified key, two operators, and test phrase.
     * This overload is used for filters of set-based fields (e.g. tags and attachments.
     * The set operator and the field-specific operator can be specified independently.
     *
     * @param key           The key that refers to the specific field in a task.
     * @param setOperator   The set filter operator.
     * @param fieldOperator The field-specific filter operator.
     * @param testPhrase    The test phrase to compare with the specific field of each task.
     * @return The predicate that is created.
     */
    private static Predicate<Task> createPredicate(String key, FilterOperator setOperator,
                                                   FilterOperator fieldOperator, String testPhrase)
            throws InvalidPredicateKeyException, InvalidPredicateSetKeyException, InvalidPredicateOperatorException,
            InvalidPredicateSetOperatorException, InvalidPredicateTestPhraseException {

        switch (key) {
        case KEY_TAG_SHORT: // Fallthrough
        case KEY_TAG_LONG:
            return createTagsPredicate(setOperator, fieldOperator, testPhrase);
        case KEY_ATTACHMENT_SHORT: // Fallthrough
        case KEY_ATTACHMENT_LONG:
            return createAttachmentsPredicate(setOperator, fieldOperator, testPhrase);
        default:
            if (isValidKey(key)) {
                throw new InvalidPredicateSetKeyException();
            } else {
                throw new InvalidPredicateKeyException();
            }
        }
    }

    /**
     * Checks if the given string represents a valid field identifier.
     *
     * @param key The field identifier to check.
     * @return True if the field identifier is valid, false otherwise.
     */
    private static boolean isValidKey(String key) {
        switch (key) {
        case KEY_NAME_SHORT:
        case KEY_NAME_LONG:
        case KEY_DEADLINE_SHORT:
        case KEY_DEADLINE_MEDIUM:
        case KEY_DEADLINE_LONG:
        case KEY_PRIORITY_SHORT:
        case KEY_PRIORITY_LONG:
        case KEY_FREQUENCY_SHORT:
        case KEY_FREQUENCY_LONG:
        case KEY_TAG_SHORT:
        case KEY_TAG_LONG:
        case KEY_ATTACHMENT_SHORT:
        case KEY_ATTACHMENT_LONG:
            return true;
        default:
            return false;
        }
    }

    /**
     * Creates a predicate matches any textual or date field in a task, using the convenience operator.
     * It does not match numeric fields because they clutter the results and are usually not intended by the user.
     *
     * @param testPhrase The test phrase to compare with the specific field of each task.
     */
    private static Predicate<Task> createPredicateAny(String testPhrase) {
        return ALWAYS_FALSE
                .or(silencePredicateException(() -> createNamePredicate(FilterOperator.CONVENIENCE, testPhrase)))
                .or(silencePredicateException(() -> createDeadlinePredicate(FilterOperator.CONVENIENCE, testPhrase)))
                .or(silencePredicateException(() -> createTagsPredicate(
                        FilterOperator.CONVENIENCE, FilterOperator.CONVENIENCE, testPhrase)))
                .or(silencePredicateException(() -> createAttachmentsPredicate(
                        FilterOperator.CONVENIENCE, FilterOperator.CONVENIENCE, testPhrase)));
    }


    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand and returns an
     * FilterCommand object for execution.
     *
     * @param args The filter expression to parse.
     * @throws ParseException if the user input does not conform the expected format.
     */
    @Override
    public FilterCommand parse(String args) throws ParseException {
        assert args != null;
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE),
                    TEXT_STYLE_CLASS_DEFAULT);
        }

        return wrapTokenizationExceptions(trimmedArgs, () -> {
            BooleanExpressionParser<Task> expressionParser =
                    new BooleanExpressionParser<>((tokenizer, reservedCharPredicate) -> {
                        final Predicate<Character> effectiveUnquotedCharacterPredicate = reservedCharPredicate.negate()
                                .and(ALLOWED_UNQUOTED_CHARACTER_PREDICATE);

                        return createFilterUnit(tokenizer, effectiveUnquotedCharacterPredicate,
                                ALLOWED_KEY_CHARACTER_PREDICATE);
                    });
            Predicate<Task> predicate = expressionParser.parse(trimmedArgs);

            assert predicate != null : "Predicate returned from expression parser was null!";
            logger.info("Parse successful");

            return new FilterCommand(predicate);
        });
    }

    /**
     * Converts all kinds of thrown TokenizationException to ParseException.
     *
     * @throws ParseException if the user input does not conform the expected format.
     */
    private FilterCommand wrapTokenizationExceptions(String trimmedArgs,
                                                     TokenizationExceptionalSupplier<FilterCommand> supplier)
            throws ParseException {
        try {
            return supplier.get();
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid filter expression: " + e.getMessage(), TEXT_STYLE_CLASS_DEFAULT);
        } catch (TokenizationMissingEndQuoteException e) {
            throw createParseException(trimmedArgs, e, "Matching end quote is missing!");
        } catch (TokenizationUnexpectedQuoteException e) {
            throw createParseException(trimmedArgs, e, "Unexpected quote in textual keyword!");
        } catch (TokenizationNoMatchableCharacterException e) {
            throw createParseException(trimmedArgs, e, "This character is invalid here!");
        } catch (TokenizationEndOfStringException | BooleanExpressionUnexpectedEndOfStringException e) {
            throw createParseException(trimmedArgs,
                    new TokenizationMismatchException(trimmedArgs.length(), trimmedArgs.length(),
                            "Unexpected end of tokenizer string"),
                    "Unexpected end of filter expression!");
        } catch (BooleanExpressionInvalidOperatorException e) {
            throw createParseException(trimmedArgs, e, "Unknown operator!");
        } catch (BooleanExpressionMismatchedLeftBracketException e) {
            throw createParseException(trimmedArgs, e,
                    "Expected a right bracket to match an existing left bracket!");
        } catch (BooleanExpressionMismatchedRightBracketException e) {
            throw createParseException(trimmedArgs, e, "Mismatched right bracket!");
        } catch (BooleanExpressionUnexpectedBinaryOperatorException e) {
            throw createParseException(trimmedArgs, e, "Logical operator not expected here!");
        } catch (BooleanExpressionUnexpectedRightBracketException e) {
            throw createParseException(trimmedArgs, e, "Unexpected right bracket after an operator!");
        } catch (TokenizationInvalidPredicateException e) {
            throw createParseExceptionFromInvalidPredicate(trimmedArgs, e);
        } catch (TokenizationException e) {
            throw createDefaultParseException(trimmedArgs, "Invalid filter expression!");
        }
    }

    /**
     * Converts all kinds of TokenizationInvalidPredicateException to ParseException.
     *
     * @throws ParseException if the user input does not conform the expected format.
     */
    private ParseException createParseExceptionFromInvalidPredicate(String trimmedArgs,
                                                                    TokenizationInvalidPredicateException e) {
        final InvalidPredicateException predicateException = e.getPredicateException();
        if (predicateException instanceof InvalidPredicateKeyException) {
            return createParseException(trimmedArgs, e, "Invalid field identifier key!");
        } else if (predicateException instanceof InvalidPredicateSetKeyException) {
            return createParseException(trimmedArgs, e, "Cannot use set-based filter predicate on this field!");
        } else if (predicateException instanceof InvalidPredicateOperatorException) {
            return createParseException(trimmedArgs, e, "Invalid filter operator!");
        } else if (predicateException instanceof InvalidPredicateSetOperatorException) {
            return createParseException(trimmedArgs, e, "Invalid set filter operator!");
        } else if (predicateException instanceof InvalidPredicateTestPhraseException) {
            return createParseException(trimmedArgs, e, "Invalid test phrase!");
        } else {
            return createDefaultParseException(trimmedArgs, "Invalid predicate expression!");
        }
    }

    /**
     * Constructs a ParseException that does not highlight any substring.
     */
    private ParseException createDefaultParseException(String trimmedArgs, String message) {
        assert trimmedArgs != null;
        assert message != null;
        return new ParseException(FilterCommand.COMMAND_WORD + ' ' + trimmedArgs + '\n' + message,
                TEXT_STYLE_CLASS_DEFAULT);
    }

    /**
     * Constructs a ParseException from the error substring denoted by the TokenMismatchException.
     */
    private ParseException createParseException(String input, TokenizationMismatchException e, String message) {
        assert input != null;
        assert message != null;
        List<ResultDisplay.StyledText> parts = new ArrayList<>();
        parts.add(new ResultDisplay.StyledText(FilterCommand.COMMAND_WORD + ' ', TEXT_STYLE_CLASS_DEFAULT));
        if (e.getBeginIndex() > 0) {
            parts.add(new ResultDisplay.StyledText(input.substring(0, e.getBeginIndex()), TEXT_STYLE_CLASS_DEFAULT));
        }
        int effectiveEndIndex = Math.min(Math.max(e.getEndIndex(), e.getBeginIndex() + 1), input.length());
        if (e.getBeginIndex() < effectiveEndIndex) {
            parts.add(new ResultDisplay.StyledText(input.substring(e.getBeginIndex(), effectiveEndIndex),
                    TEXT_STYLE_CLASS_ERROR));
        } else {
            parts.add(new ResultDisplay.StyledText("......", TEXT_STYLE_CLASS_ERROR));
        }
        if (effectiveEndIndex < input.length()) {
            parts.add(new ResultDisplay.StyledText(input.substring(effectiveEndIndex), TEXT_STYLE_CLASS_DEFAULT));
        }
        parts.add(new ResultDisplay.StyledText("\n" + message, TEXT_STYLE_CLASS_DEFAULT));
        return new ParseException(parts);
    }

    /**
     * Reads and returns a predicate from the given string tokenizer.
     * There are three allowed forms for filter units:
     * [phrase]
     * [key][op][phrase]
     * [key][setOp][fieldOp][phrase]
     * This method determines which option should be chosen (based on the input),
     * and creates the correct predicate for it.
     *
     * @param tokenizer                           The string tokenizer.
     * @param effectiveUnquotedCharacterPredicate A predicate that specifies the allowable characters
     *                                            for unquoted test phrases.
     * @param allowedKeyCharacterPredicate        A predicate that specifies the allowable characters
     *                                            for filter field identifiers (i.e. keys).
     */
    private static Predicate<Task> createFilterUnit(StringTokenizer tokenizer,
                                                    Predicate<Character> effectiveUnquotedCharacterPredicate,
                                                    Predicate<Character> allowedKeyCharacterPredicate)
            throws TokenizationMissingEndQuoteException, TokenizationUnexpectedQuoteException,
            TokenizationNoMatchableCharacterException, TokenizationEndOfStringException,
            TokenizationInvalidPredicateException {

        // many indices are saved (using tokenizer.getLocation()) because they are used to report an error
        int keyStartIndex = tokenizer.getLocation(); // get the key start location
        final String key = tokenizer.tryNextString(allowedKeyCharacterPredicate);
        int keyEndIndex = tokenizer.getLocation(); // get the key end location
        String opString;
        if (key != null && tokenizer.hasNextToken()
                && (opString = tokenizer.tryNextPattern(FILTER_OPERATOR_PATTERN)) != null) {
            int opEndIndex = tokenizer.getLocation();
            final FilterOperator operator = wrapPredicateOperatorException(keyEndIndex,
                    opEndIndex, () -> FilterOperator.parse(opString));
            String opString2 = tokenizer.tryNextPattern(FILTER_OPERATOR_PATTERN); // could be null
            int op2EndIndex = tokenizer.getLocation();
            final String testPhrase = tokenizer.nextString(effectiveUnquotedCharacterPredicate);
            int testPhraseEndIndex = tokenizer.getLocation();
            if (opString2 == null) {
                // has only one filter operator
                return wrapPredicateException(keyStartIndex, keyEndIndex, keyEndIndex, opEndIndex,
                        testPhraseEndIndex, () -> createPredicate(key, operator, testPhrase));
            } else {
                // has two filter operators
                final FilterOperator operator2 = wrapPredicateSetOperatorException(opEndIndex,
                        op2EndIndex, () -> FilterOperator.parse(opString2));
                return wrapPredicateException(keyStartIndex, keyEndIndex, opEndIndex, op2EndIndex,
                        testPhraseEndIndex, () -> createPredicate(key, operator, operator2, testPhrase));
            }
        } else {
            tokenizer.setLocation(keyStartIndex); // rewind the tokenizer
            final String testPhrase = tokenizer.nextString(effectiveUnquotedCharacterPredicate);
            return createPredicateAny(testPhrase);
        }
    }

    /**
     * Converts a thrown InvalidPredicateOperatorException to a wrapped TokenizationInvalidPredicateException.
     */
    private static FilterOperator wrapPredicateOperatorException(int startIndex, int endIndex,
            PredicateOperatorExceptionalSupplier<FilterOperator> supplier)
            throws TokenizationInvalidPredicateException {
        try {
            return supplier.get();
        } catch (InvalidPredicateOperatorException e) {
            throw new TokenizationInvalidPredicateException(startIndex, endIndex, e.getMessage(), e);
        }
    }

    /**
     * Converts a thrown InvalidPredicateOperatorException to a wrapped TokenizationInvalidPredicateException,
     * but converts the wrapped exception to InvalidPredicateSetOperatorException.
     */
    private static FilterOperator wrapPredicateSetOperatorException(int startIndex, int endIndex,
            PredicateOperatorExceptionalSupplier<FilterOperator> supplier)
            throws TokenizationInvalidPredicateException {
        try {
            return supplier.get();
        } catch (InvalidPredicateOperatorException e) {
            throw new TokenizationInvalidPredicateException(startIndex, endIndex, e.getMessage(),
                    new InvalidPredicateSetOperatorException());
        }
    }

    /**
     * Converts any thrown InvalidPredicateException to a wrapped TokenizationInvalidPredicateException.
     */
    private static Predicate<Task> wrapPredicateException(int keyStartIndex, int keyEndIndex, int fieldOpStartIndex,
                                                          int fieldOpEndIndex, int testPhraseEndIndex,
                                                          ExceptionalSupplier<Predicate<Task>> supplier)
            throws TokenizationInvalidPredicateException {
        try {
            return supplier.get();
        } catch (InvalidPredicateKeyException | InvalidPredicateSetKeyException e) {
            throw new TokenizationInvalidPredicateException(keyStartIndex, keyEndIndex, e.getMessage(), e);
        } catch (InvalidPredicateOperatorException e) {
            throw new TokenizationInvalidPredicateException(fieldOpStartIndex, fieldOpEndIndex, e.getMessage(), e);
        } catch (InvalidPredicateSetOperatorException e) {
            throw new TokenizationInvalidPredicateException(keyEndIndex, fieldOpStartIndex, e.getMessage(), e);
        } catch (InvalidPredicateTestPhraseException e) {
            throw new TokenizationInvalidPredicateException(fieldOpEndIndex, testPhraseEndIndex, e.getMessage(), e);
        } catch (InvalidPredicateException e) {
            throw new TokenizationInvalidPredicateException(keyStartIndex, testPhraseEndIndex, e.getMessage(), e);
        }
    }
}
