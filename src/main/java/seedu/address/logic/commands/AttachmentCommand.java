package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FILENAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FILEPATH;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.attachment.Attachment;
import seedu.address.model.task.Task;

/**
 * Edits the details of an existing task in the deadline manager.
 */
public class AttachmentCommand extends Command {

    public static final String COMMAND_WORD = "attachment";
    public static final String COMMAND_ADD_ACTION = "add";
    public static final String COMMAND_GET_ACTION = "get";
    public static final String COMMAND_DELETE_ACTION = "delete";
    public static final String COMMAND_LIST_ACTION = "list";
    public static final String MESSAGE_USAGE =
        COMMAND_WORD + ": Modify and manages the attachments of the task identified "
            + "by the index number used in the displayed task list. "
            + "Parameters: INDEX (must be a positive integer) "
            + "[add|get|delete|list] "
            + "[" + PREFIX_FILEPATH + "FILEPATH] "
            + "[" + PREFIX_FILENAME + "FILENAME]\n"
            + "Example 1: " + COMMAND_WORD + " 1 " + COMMAND_ADD_ACTION + " "
            + PREFIX_FILEPATH + "D:\\Documents\\HelloWorld.docx\n"
            + "Example 2: " + COMMAND_WORD + " 2 " + COMMAND_GET_ACTION + " "
            + PREFIX_FILEPATH + "D:\\Documents\\TaskAttachments.zip\n"
            + "Example 3: " + COMMAND_WORD + " 1 " + COMMAND_LIST_ACTION + "\n"
            + "Example 4: " + COMMAND_WORD + " 1 " + COMMAND_DELETE_ACTION + " "
            + PREFIX_FILENAME + "HelloWorld.docx";


    public static final String MESSAGE_MISSING_ARGUMENTS = "Missing argument %1$s for %2$s action";

    private final Index index;
    private final AttachmentAction attachmentAction;

    /**
     * @param index            of the task in the filtered task list to edit
     * @param attachmentAction action that is to be performed on the task
     */
    public AttachmentCommand(Index index, AttachmentAction attachmentAction) {
        requireNonNull(index);
        //requireNonNull(attachmentAction);

        this.index = index;
        this.attachmentAction = attachmentAction;
        //this.AttachmentAction = attachmentCommandDescriptor;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);
        List<Task> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Task taskToEdit = lastShownList.get(index.getZeroBased());
        Task editedTask = attachmentAction.perform(taskToEdit);

        model.updatePerson(taskToEdit, editedTask);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        model.commitAddressBook();
        //return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, editedTask));
        return new CommandResult(attachmentAction.resultMessage());
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToEdit} edited with {@code
     * editPersonDescriptor}.
     */
    private static Task createEditedTask(Task taskToEdit,
                                         AttachmentAction attachmentAction) {
        assert taskToEdit != null;


        return new Task(taskToEdit.getName(), taskToEdit.getPhone(), taskToEdit.getPriority(), taskToEdit.getEmail(),
            taskToEdit.getDeadline(), taskToEdit.getAddress(), taskToEdit.getTags(), taskToEdit.getAttachments());
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AttachmentCommand)) {
            return false;
        }

        // state check
        AttachmentCommand e = (AttachmentCommand) other;
        return index.equals(e.index)
            && attachmentAction.equals(e.attachmentAction);
    }

    /**
     * Stores the actions to perform on a task's attachment with.
     */
    public abstract static class AttachmentAction {
        public static final String MESSAGE_ATTACHMENT_ACTION_COMPELTED = "Action Completed.";

        public abstract Task perform(Task taskToEdit) throws CommandException;

        public String resultMessage() {
            return MESSAGE_ATTACHMENT_ACTION_COMPELTED;
        }
    }


    /**
     * Action to add attachment
     */
    public static class AddAttachmentAction extends AttachmentAction {
        public static final String MESSAGE_ADD_NOT_A_FILE = "%1$s is not a valid file.";
        public static final String MESSAGE_SUCCESS = "%1$s added as attachment.";

        private String resultMessage = "Action Not Performed";
        private final String filePath;

        public AddAttachmentAction(String filePath) {
            requireAllNonNull(filePath);
            this.filePath = filePath;
        }


        @Override
        public Task perform(Task taskToEdit) throws CommandException {
            assert taskToEdit != null;
            HashSet<Attachment> updatedAttachments = new HashSet<>(taskToEdit.getAttachments());
            updatedAttachments.add(buildAttachment());
            return new Task(taskToEdit.getName(), taskToEdit.getPhone(), taskToEdit.getPriority(),
                taskToEdit.getEmail(), taskToEdit.getDeadline(),
                taskToEdit.getAddress(), taskToEdit.getTags(), updatedAttachments);

        }

        // TODO: move file to appropriate place in storage
        private File moveFileToStorage(File file) {
            return file;
        }

        /**
         * Generates the Attachment Object from the file path.
         * Also performs checks to see if file exists.
         *
         * @return Arrachment that is generated
         * @throws CommandException
         */
        private Attachment buildAttachment() throws CommandException {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                throw new CommandException(String.format(MESSAGE_ADD_NOT_A_FILE, filePath));
            }
            file = moveFileToStorage(file);
            resultMessage = String.format(MESSAGE_SUCCESS, file.getName());
            return new Attachment(new File(filePath));
        }

        public String resultMessage() {
            return resultMessage;
        }
    }

    /**
     * Action to list all attachments.
     */
    public static class ListAttachmentAction extends AttachmentAction {
        private String resultMessage = "Action Not Performed";

        @Override
        public Task perform(Task taskToEdit) throws CommandException {
            assert taskToEdit != null;
            Set<Attachment> attachments = taskToEdit.getAttachments();
            resultMessage = String.format("%d attachments in total.\n", attachments.size());
            int indexCounter = 0;
            for (Attachment attachment : attachments) {
                indexCounter++;
                resultMessage += String.format("%d) %s\n", indexCounter, attachment.toString());
            }
            return taskToEdit;
        }

        public String resultMessage() {
            return resultMessage;
        }
    }

    /**
     * Action to delete all attachment.
     */
    public static class DeleteAttachmentAction extends AttachmentAction {
        public static final String MESSAGE_NAME_NOT_FOUND = "%1$s is not an attachment.";
        public static final String MESSAGE_SUCCESS = "%1$s deleted from attachments.";

        private String resultMessage = "Action Not Performed";
        private final String nameToDelete;

        public DeleteAttachmentAction(String nameToDelete) {
            requireAllNonNull(nameToDelete);
            this.nameToDelete = nameToDelete;
        }

        //TODO: Remove from storage
        private void deleteFileFromStorage(File file) {
            return;
        }

        @Override
        public Task perform(Task taskToEdit) throws CommandException {
            assert taskToEdit != null;
            HashSet<Attachment> updatedAttachments = new HashSet<>(taskToEdit.getAttachments());

            Map<String, Attachment> attachmentNameMap = updatedAttachments.stream()
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
            if (!attachmentNameMap.containsKey(nameToDelete)) {
                throw new CommandException(String.format(MESSAGE_NAME_NOT_FOUND, nameToDelete));
            }
            Attachment attachmentToDelete = attachmentNameMap.get(nameToDelete);
            updatedAttachments.remove(attachmentToDelete);
            deleteFileFromStorage(attachmentToDelete.file);
            resultMessage = String.format(MESSAGE_SUCCESS, nameToDelete);
            return new Task(taskToEdit.getName(), taskToEdit.getPhone(), taskToEdit.getPriority(),
                taskToEdit.getEmail(), taskToEdit.getDeadline(),
                taskToEdit.getAddress(), taskToEdit.getTags(), updatedAttachments);
        }

        public String resultMessage() {
            return resultMessage;
        }
    }

    /**
     * Action to get/retrieve attachment.
     */
    public static class GetAttachmentAction extends AttachmentAction {
        public static final String MESSAGE_NAME_NOT_FOUND = "%1$s is not an attachment.";
        public static final String MESSAGE_GET_FAILED = "Failed to save to %1$s.";
        public static final String MESSAGE_SUCCESS = "%1$s is now saved to %2$s.";
        public static final String MESSAGE_GET_NOT_A_FILE = "%1$s is not a valid file.";

        private String resultMessage = "Action Not Performed";
        private final String nameToGet;
        private final String savePath;

        public GetAttachmentAction(String nameToGet, String savePath) {
            requireAllNonNull(nameToGet, savePath);
            this.nameToGet = nameToGet;
            this.savePath = savePath;
        }

        /**
         * Copies the file from file to saveFile, overwriting the destination if a file exists.
         *
         * @param sourceFile     source file
         * @param destFile destination file
         */
        private File copyFileToDestination(File sourceFile, File destFile) throws CommandException {
            Path sourcePath = sourceFile.toPath();
            if (!sourceFile.exists() || !sourceFile.isFile()) {
                throw new CommandException(String.format(MESSAGE_GET_NOT_A_FILE, sourceFile));
            }
            Path savePath = destFile.toPath();
            try {
                File copiedFile = Files.copy(sourcePath, savePath, StandardCopyOption.REPLACE_EXISTING).toFile();
                return copiedFile;
            } catch (IOException ioe) {
                throw new CommandException(String.format(MESSAGE_GET_FAILED, destFile.getPath()));
            }
        }

        @Override
        public Task perform(Task taskToEdit) throws CommandException {
            assert taskToEdit != null;
            Map<String, Attachment> attachmentNameMap = taskToEdit.getAttachments().stream()
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
            if (!attachmentNameMap.containsKey(nameToGet)) {
                throw new CommandException(String.format(MESSAGE_NAME_NOT_FOUND, nameToGet));
            }
            Attachment attachmentToGet = attachmentNameMap.get(nameToGet);
            File saveFile = new File(savePath);
            copyFileToDestination(attachmentToGet.file, saveFile);

            resultMessage = String.format(MESSAGE_SUCCESS, attachmentToGet.getName(), saveFile.getPath());

            return taskToEdit;
        }

        public String resultMessage() {
            return resultMessage;
        }
    }
}
