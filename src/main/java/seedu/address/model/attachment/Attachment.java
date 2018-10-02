package seedu.address.model.attachment;

import static java.util.Objects.requireNonNull;

import java.io.File;

/**
 * Represents a Attachment in the deadline manager. Guarantees: immutable;
 */
public class Attachment {
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
            && file.equals(((Attachment) other).file)); // state check
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
