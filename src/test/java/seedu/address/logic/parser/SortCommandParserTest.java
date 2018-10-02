package seedu.address.logic.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SortCommandParserTest {

    private SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ",
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_doesNotThrow() {
        assertParseSuccess(parser, "d<");
        assertParseSuccess(parser, "name>");
        assertParseSuccess(parser, "d< name>");
        assertParseSuccess(parser, "d> d<");
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseThrowsException(parser, "d=");
        assertParseThrowsException(parser, "dues<");
        assertParseThrowsException(parser, "name>>");
        assertParseThrowsException(parser, "name>>  due<");
        assertParseThrowsException(parser, "d>  name<");
        assertParseThrowsException(parser, "name~");
    }

    /**
     * Asserts that the parse was successful.
     */
    private void assertParseSuccess(SortCommandParser parser, String str) {
        try {
            SortCommand command = parser.parse(str);
            assertNotNull("Expected not null", command);
        } catch (ParseException e) {
            fail("Expected no parse error");
        }
    }

    /**
     * Asserts that the parse throws a ParseException.
     */
    private void assertParseThrowsException(SortCommandParser parser, String str) {
        try {
            parser.parse(str);
            fail("Expected a parse error");
        } catch (ParseException ignore) {
            // Do not do anything, because we just want to make sure that an exception is thrown
        }
    }

}
