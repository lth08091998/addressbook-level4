package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.commands.SelectEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

/**
 * Parses input arguments and creates a new SelectCommand object
 */
public class SelectEventCommandParser implements Parser<SelectEventCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the SelectEventCommand
     * and returns an SelectCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public SelectEventCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new SelectEventCommand(index);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));
        }
    }
}
