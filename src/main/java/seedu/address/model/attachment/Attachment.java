package seedu.address.model.attachment;

import static java.util.Objects.requireNonNull;

import java.io.File;

/**
 * Represents a Attachment in the deadline manager. Guarantees: immutable;
 */
public class Attachment {
    public static final String MESSAGE_DUPLICATE_ATTACHMENT_NAME = "There cannot be more than one attachment" +
        "with the same file name. Please rename one of them.";
    public final File file;

    public Attachment(File file) {
        requireNonNull(file);
        this.file = file;
    }

    /**
     * @return Name of the attachment, which is the file name.
     */
    public String getName() {
        return file.getName();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
            || (other instanceof Attachment // instanceof handles nulls
            && file.getAbsolutePath().equals(((Attachment) other).file.getAbsolutePath())); // state check
    }

    @Override
    public int hashCode() {
        return file.getAbsolutePath().hashCode();
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
