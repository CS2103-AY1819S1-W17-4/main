package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javafx.collections.ObservableList;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyTaskCollection;
import seedu.address.model.TaskCollection;
import seedu.address.model.task.Task;
import seedu.address.testutil.PersonBuilder;

public class AddCommandTest {

    private static final CommandHistory EMPTY_COMMAND_HISTORY = new CommandHistory();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private CommandHistory commandHistory = new CommandHistory();

    @Test
    public void constructor_nullPerson_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new AddCommand(null);
    }

    @Test
    public void execute_personAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Task validTask = new PersonBuilder().build();

        CommandResult commandResult = new AddCommand(validTask).execute(modelStub, commandHistory);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, validTask),
            commandResult.feedbackToUser);
        assertEquals(Arrays.asList(validTask), modelStub.personsAdded);
        assertEquals(EMPTY_COMMAND_HISTORY, commandHistory);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() throws Exception {
        Task validTask = new PersonBuilder().build();
        AddCommand addCommand = new AddCommand(validTask);
        ModelStub modelStub = new ModelStubWithPerson(validTask);

        thrown.expect(CommandException.class);
        thrown.expectMessage(AddCommand.MESSAGE_DUPLICATE_PERSON);
        addCommand.execute(modelStub, commandHistory);
    }

    @Test
    public void equals() {
        Task alice = new PersonBuilder().withName("Alice").build();
        Task bob = new PersonBuilder().withName("Bob").build();
        AddCommand addAliceCommand = new AddCommand(alice);
        AddCommand addBobCommand = new AddCommand(bob);

        // same object -> returns true
        assertTrue(addAliceCommand.equals(addAliceCommand));

        // same values -> returns true
        AddCommand addAliceCommandCopy = new AddCommand(alice);
        assertTrue(addAliceCommand.equals(addAliceCommandCopy));

        // different types -> returns false
        assertFalse(addAliceCommand.equals(1));

        // null -> returns false
        assertFalse(addAliceCommand.equals(null));

        // different task -> returns false
        assertFalse(addAliceCommand.equals(addBobCommand));
    }

    /**
     * A default model stub that have all of the methods failing.
     */
    private class ModelStub implements Model {

        @Override
        public void addPerson(Task task) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void resetData(ReadOnlyTaskCollection newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyTaskCollection getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Task task) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Task target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updatePerson(Task target, Task editedTask) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateSortedPersonList(Comparator<Task> comparator) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Task> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Task> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean canUndoAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean canRedoAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void undoAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void redoAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void commitAddressBook() {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single task.
     */
    private class ModelStubWithPerson extends ModelStub {

        private final Task task;

        ModelStubWithPerson(Task task) {
            requireNonNull(task);
            this.task = task;
        }

        @Override
        public boolean hasPerson(Task task) {
            requireNonNull(task);
            return this.task.equals(task);
        }
    }

    /**
     * A Model stub that always accept the task being added.
     */
    private class ModelStubAcceptingPersonAdded extends ModelStub {

        final ArrayList<Task> personsAdded = new ArrayList<>();

        @Override
        public boolean hasPerson(Task task) {
            requireNonNull(task);
            return personsAdded.contains(task);
        }

        @Override
        public void addPerson(Task task) {
            requireNonNull(task);
            personsAdded.add(task);
        }

        @Override
        public void commitAddressBook() {
            // called by {@code AddCommand#execute()}
        }

        @Override
        public ReadOnlyTaskCollection getAddressBook() {
            return new TaskCollection();
        }
    }

}
