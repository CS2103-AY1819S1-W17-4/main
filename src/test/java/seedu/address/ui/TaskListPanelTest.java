package seedu.address.ui;

import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static seedu.address.testutil.EventsUtil.postNow;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_TASK;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;
import static seedu.address.ui.testutil.GuiTestAssert.assertCardDisplaysTask;
import static seedu.address.ui.testutil.GuiTestAssert.assertCardEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import guitests.guihandles.TaskCardHandle;
import guitests.guihandles.TaskListPanelHandle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.events.ui.JumpToListRequestEvent;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.XmlUtil;
import seedu.address.model.task.Task;
import seedu.address.storage.XmlSerializableTaskCollection;

public class TaskListPanelTest extends GuiUnitTest {

    private static final ObservableList<Task> TYPICAL_TASKS =
        FXCollections.observableList(getTypicalPersons());

    private static final JumpToListRequestEvent JUMP_TO_SECOND_EVENT = new JumpToListRequestEvent(
        INDEX_SECOND_TASK);

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "sandbox");

    private static final long CARD_CREATION_AND_DELETION_TIMEOUT = 5000;

    private TaskListPanelHandle taskListPanelHandle;

    @Test
    public void display() {
        initUi(TYPICAL_TASKS);

        for (int i = 0; i < TYPICAL_TASKS.size(); i++) {
            taskListPanelHandle.navigateToCard(TYPICAL_TASKS.get(i));
            Task expectedTask = TYPICAL_TASKS.get(i);
            TaskCardHandle actualCard = taskListPanelHandle.getTaskCardHandle(i);

            assertCardDisplaysTask(expectedTask, actualCard);
            assertEquals(Integer.toString(i + 1) + ". ", actualCard.getId());
        }
    }

    @Test
    public void handleJumpToListRequestEvent() {
        initUi(TYPICAL_TASKS);
        postNow(JUMP_TO_SECOND_EVENT);
        guiRobot.pauseForHuman();

        TaskCardHandle expectedTask = taskListPanelHandle
            .getTaskCardHandle(INDEX_SECOND_TASK.getZeroBased());
        TaskCardHandle selectedTask = taskListPanelHandle.getHandleToSelectedCard();
        assertCardEquals(expectedTask, selectedTask);
    }

    /**
     * Verifies that creating and deleting large number of persons in {@code TaskListPanel}
     * requires lesser than {@code CARD_CREATION_AND_DELETION_TIMEOUT} milliseconds to execute.
     */
    @Test
    public void performanceTest() throws Exception {
        ObservableList<Task> backingList = createBackingList(10000);

        assertTimeoutPreemptively(ofMillis(CARD_CREATION_AND_DELETION_TIMEOUT), () -> {
            initUi(backingList);
            guiRobot.interact(backingList::clear);
        }, "Creation and deletion of task cards exceeded time limit");
    }

    /**
     * Returns a list of persons containing {@code personCount} persons that is used to populate the
     * {@code TaskListPanel}.
     */
    private ObservableList<Task> createBackingList(int personCount) throws Exception {
        Path xmlFile = createXmlFileWithTasks(personCount);
        XmlSerializableTaskCollection xmlAddressBook =
            XmlUtil.getDataFromFile(xmlFile, XmlSerializableTaskCollection.class);
        return FXCollections.observableArrayList(xmlAddressBook.toModelType().getTaskList());
    }

    /**
     * Returns a .xml file containing {@code taskCount} persons. This file will be deleted when
     * the JVM terminates.
     */
    private Path createXmlFileWithTasks(int taskCount) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        builder.append("<taskcollection>\n");
        for (int i = 0; i < taskCount; i++) {
            builder.append("<tasks>\n");
            builder.append("<name>").append(i).append("a</name>\n");
            builder.append("<priority>1</priority>\n");
            builder.append("<deadline>1/10/2018</deadline>\n");
            builder.append("</tasks>\n");
        }
        builder.append("</taskcollection>\n");

        Path manyTasksFile = Paths.get(TEST_DATA_FOLDER + "manyPersons.xml");
        FileUtil.createFile(manyTasksFile);
        FileUtil.writeToFile(manyTasksFile, builder.toString());
        manyTasksFile.toFile().deleteOnExit();
        return manyTasksFile;
    }

    /**
     * Initializes {@code taskListPanelHandle} with a {@code TaskListPanel} backed by {@code
     * backingList}. Also shows the {@code Stage} that displays only {@code TaskListPanel}.
     */
    private void initUi(ObservableList<Task> backingList) {
        TaskListPanel taskListPanel = new TaskListPanel(backingList);
        uiPartRule.setUiPart(taskListPanel);
        taskListPanelHandle = new TaskListPanelHandle(getChildNode(taskListPanel.getRoot(),
            TaskListPanelHandle.TASK_LIST_VIEW_ID));
    }
}
