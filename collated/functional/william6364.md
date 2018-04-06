# william6364
###### /java/seedu/address/logic/parser/ToggleAttendanceCommandParser.java
``` java
/**
 * Parses input arguments and creates a new ToggleAttendanceCommand object
 */
public class ToggleAttendanceCommandParser implements Parser<ToggleAttendanceCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ToggleAttendanceCommand
     * and returns an ToggleAttendanceCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public ToggleAttendanceCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new ToggleAttendanceCommand(index);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ToggleAttendanceCommand.MESSAGE_USAGE));
        }
    }
}
```
###### /java/seedu/address/logic/parser/AddEventCommandParser.java
``` java
/**
 * Parses input arguments and creates a new AddPersonCommand object
 */
public class AddEventCommandParser implements Parser<AddEventCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddEventCommand
     * and returns an AddEventCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddEventCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
        }

        try {
            Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME)).get();
            Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

            EpicEvent event = new EpicEvent(name, tagList);

            return new AddEventCommand(event);
        } catch (IllegalValueException ive) {
            throw new ParseException(ive.getMessage(), ive);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
```
###### /java/seedu/address/logic/commands/AddEventCommand.java
``` java
/**
 * Adds an event to the event planner.
 */
public class AddEventCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "add-event";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an event to the event planner. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "AY201718 Graduation Ceremony "
            + PREFIX_TAG + "graduation ";

    public static final String MESSAGE_SUCCESS = "New event added: %1$s";
    public static final String MESSAGE_DUPLICATE_EVENT = "This event already exists in the event planner";

    private final EpicEvent toAdd;

    /**
     * Creates an AddEventCommand to add the specified {@code EpicEvent}
     */
    public AddEventCommand(EpicEvent event) {
        requireNonNull(event);
        toAdd = event;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        try {
            model.addEvent(toAdd);
            return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
        } catch (DuplicateEventException e) {
            throw new CommandException(MESSAGE_DUPLICATE_EVENT);
        }
    }

```
###### /java/seedu/address/logic/commands/AddEventCommand.java
``` java

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddEventCommand // instanceof handles nulls
                && toAdd.equals(((AddEventCommand) other).toAdd));
    }
}
```
###### /java/seedu/address/logic/commands/ToggleAttendanceCommand.java
``` java
/**
 * Marks attendance of a participant to an event.
 */
public class ToggleAttendanceCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "toggle";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Toggles the attendance of the person identified by the index number used in the last attendee listing"
            + " to a particular event.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Toggled attendance of person %1$s for event %2$s";

    private Index targetIndex;
    private Attendance attendanceToToggle;

    public ToggleAttendanceCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    public ToggleAttendanceCommand(Attendance attendanceToToggle) {
        this.attendanceToToggle = attendanceToToggle;
    }

    public Attendance getAttendanceToToggle() {
        return attendanceToToggle;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(attendanceToToggle);
        try {
            model.toggleAttendance(attendanceToToggle.getPerson(), attendanceToToggle.getEvent());
            EventsCenter.getInstance().post(new AttendanceCardToggleEvent(targetIndex.getZeroBased()));
        } catch (EventNotFoundException e) {
            throw new AssertionError("The target event cannot be missing");
        } catch (PersonNotFoundInEventException e) {
            throw new CommandException(MESSAGE_PERSON_NOT_IN_EVENT);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, attendanceToToggle.getPerson().getName(),
                attendanceToToggle.getEvent().getName()));
    }

    @Override
    protected void preprocessUndoableCommand() throws CommandException {
        List<Attendance> lastShownList = model.getSelectedEpicEvent().getEpicEvent().getAttendanceList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_ATTENDANCE_DISPLAYED_INDEX);
        }
        attendanceToToggle = lastShownList.get(targetIndex.getZeroBased());
    }

    @Override
    protected void generateOppositeCommand() {
        oppositeCommand = new ToggleAttendanceCommand(attendanceToToggle);
    }


    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ToggleAttendanceCommand // instanceof handles nulls
                && this.targetIndex.equals(((ToggleAttendanceCommand) other).targetIndex) // state check
                && Objects.equals(this.attendanceToToggle, ((ToggleAttendanceCommand) other).attendanceToToggle));
    }
}
```
###### /java/seedu/address/model/attendance/UniqueAttendanceList.java
``` java
/**
 * A list of attendance objects that enforces uniqueness between the persons inside the object and does not allow nulls.
 *
 * Supports a minimal set of list operations.
 *
 * @see Attendance#equals(Object)
 * @see CollectionUtil#elementsAreUnique(Collection)
 */
public class UniqueAttendanceList {

    private final ObservableList<Attendance> internalList = FXCollections.observableArrayList();

    /**
     * Returns true if the list contains an equivalent person as the given argument.
     */
    public boolean contains(Person toCheck, EpicEvent event) {
        requireAllNonNull(toCheck, event);
        return contains(new Attendance(toCheck, event));
    }

    /**
     * Returns true if the list contains an equivalent attendee as the given argument.
     */
    public boolean contains(Attendance toCheck) {
        requireNonNull(toCheck);
        return internalList.contains(toCheck);
    }

    /**
     * Adds a person to the attendance list.
     *
     * @throws DuplicateAttendanceException if the person to add is a duplicate of an existing person in the list.
     */
    public void add(Person toAdd, EpicEvent event) throws DuplicateAttendanceException {
        requireAllNonNull(toAdd, event);
        add(new Attendance(toAdd, event));
    }

    /**
     * Adds an attendance object to the list.
     *
     * @throws DuplicateAttendanceException if the attendee to add is a duplicate of an existing attendee in the list.
     */
    public void add(Attendance toAdd) throws DuplicateAttendanceException {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateAttendanceException();
        }
        internalList.add(toAdd);
    }

    /**
     * Toggles the attendance of {@code person} in the list.
     *
     * @throws PersonNotFoundInEventException if {@code person} could not be found in the list.
     */
    public void toggleAttendance(Person person, EpicEvent event) throws PersonNotFoundInEventException {
        requireAllNonNull(person, event);

        int index = internalList.indexOf(new Attendance(person, event));
        if (index == -1) {
            throw new PersonNotFoundInEventException();
        }

        Attendance currentAttendance = internalList.get(index);
        internalList.get(index).setAttendance(new Attendance(currentAttendance.getPerson(),
                currentAttendance.getEvent(), !currentAttendance.hasAttended()));
    }

    /**
     * Removes the equivalent attendee from the list.
     *
     * @throws PersonNotFoundInEventException if no such attendee could be found in the list.
     */
    public boolean remove(Person toRemove, EpicEvent event) throws PersonNotFoundInEventException {
        requireAllNonNull(toRemove, event);
        return remove(new Attendance(toRemove, event));
    }

    /**
     * Removes the equivalent attendee from the list.
     *
     * @throws PersonNotFoundInEventException if no such attendee could be found in the list.
     */
    public boolean remove(Attendance toRemove) throws PersonNotFoundInEventException {
        requireNonNull(toRemove);
        final boolean attendeeFoundAndDeleted = internalList.remove(toRemove);
        if (!attendeeFoundAndDeleted) {
            throw new PersonNotFoundInEventException();
        }
        return attendeeFoundAndDeleted;
    }

```
###### /java/seedu/address/model/attendance/UniqueAttendanceList.java
``` java

    public void setAttendanceList(UniqueAttendanceList replacement) {
        this.internalList.setAll(replacement.internalList);
    }

    public void setAttendanceList(List<Attendance> attendanceList) throws DuplicateAttendanceException {
        requireAllNonNull(attendanceList);
        final UniqueAttendanceList replacement = new UniqueAttendanceList();
        for (final Attendance attendance : attendanceList) {
            replacement.add(attendance);
        }
        setAttendanceList(replacement);
    }

    /**
     * Decrements the numberOfEventsRegisteredFor of all Persons in this AttendanceList by 1.
     * Called only when the event this AttendanceList belongs to is being deleted
     */
    public void handleDeleteEvent() {
        for (Attendance attendance: internalList) {
            attendance.getPerson().decrementNumberOfEventsRegisteredFor();
        }
    }

    /**
     * Decrements the numberOfEventsRegisteredFor of all Persons in this AttendanceList by 1.
     * Called only when the event this AttendanceList belongs to is being added.
    * Required to properly maintain numberOfPersonsRegisteredFor for these persons
     * when an undo of a delete operation is called
     */
    public void handleAddEvent() {
        for (Attendance attendance: internalList) {
            attendance.getPerson().incrementNumberOfEventsRegisteredFor();
        }
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Attendance> asObservableList() {
        return FXCollections.unmodifiableObservableList(internalList);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniqueAttendanceList // instanceof handles nulls
                && this.internalList.equals(((UniqueAttendanceList) other).internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }
}
```
###### /java/seedu/address/model/attendance/Attendance.java
``` java
/**
 * Represents the attendance of a person to an event in the event planner.
 * Guarantees: person is immutable and not null
 */
public class Attendance {

    private Person attendee;
    private EpicEvent event;
    private boolean hasAttendedEvent;

    /**
     * Person must be not be null
     * @param attendee
     * @param event
     */
    public Attendance(Person attendee, EpicEvent event) {
        Objects.requireNonNull(attendee);
        Objects.requireNonNull(event);
        this.attendee = attendee;
        this.event = event;
        this.hasAttendedEvent = false;
    }

```
###### /java/seedu/address/model/attendance/Attendance.java
``` java

    /**
     * Person must be not be null
     * @param attendee
     * @param event
     * @param hasAttended
     */
    public Attendance(Person attendee, EpicEvent event, boolean hasAttended) {
        Objects.requireNonNull(attendee);
        Objects.requireNonNull(event);
        this.attendee = attendee;
        this.event = event;
        this.hasAttendedEvent = hasAttended;
    }

    public Person getPerson() {
        Objects.requireNonNull(attendee);
        return attendee;
    }

    public EpicEvent getEvent() {
        Objects.requireNonNull(event);
        return event;
    }

    public boolean hasAttended() {
        return hasAttendedEvent;
    }

    /**
     * Edits this attendance by transferring the name and tags of the dummyAttendance over
     */
    public void setAttendance(Attendance dummyAttendance) {
        this.attendee = dummyAttendance.getPerson();
        this.event = dummyAttendance.getEvent();
        this.hasAttendedEvent = dummyAttendance.hasAttended();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Attendance)) {
            return false;
        }

        Attendance otherAttendance = (Attendance) other;
        return otherAttendance.getPerson().equals(this.getPerson())
                && otherAttendance.getEvent().equals(this.getEvent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(attendee, event, hasAttendedEvent);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Person: ")
                .append(attendee.getName())
                .append(" Event: ")
                .append(event.getName())
                .append(" Attendance: ")
                .append(Boolean.toString(hasAttendedEvent));
        return builder.toString();
    }
}
```
###### /java/seedu/address/model/attendance/exceptions/DuplicateAttendanceException.java
``` java
/**
 * Signals that the operation will result in duplicate Attendance objects.
 */
public class DuplicateAttendanceException extends DuplicateDataException {
    public DuplicateAttendanceException() {
        super("Operation would result in duplicate attendance");
    }
}
```
###### /java/seedu/address/model/EventPlanner.java
``` java
    /**
     * Toggles the attendance of a particular person in a particular event
     */
    public void toggleAttendance(Person person, EpicEvent event)
        throws EventNotFoundException, PersonNotFoundInEventException {
        events.toggleAttendance(person, event);
    }

    //// tag-level operations

    /**
     *  Updates the master person tag list to include tags in {@code objectTags} that are not in the list.
     *  @return a mapping of the Tags in the list Tag object in the master list.
     */
    private Map<Tag, Tag> updateMasterPersonTagList(UniqueTagList objectTags) {
        personTags.mergeFrom(objectTags);

        // Create map with values = tag object references in the master list
        // used for checking tag references
        final Map<Tag, Tag> masterTagObjects = new HashMap<>();
        personTags.forEach(tag -> masterTagObjects.put(tag, tag));

        return masterTagObjects;
    }


    /**
     *  Updates the master event tag list to include tags in {@code objectTags} that are not in the list.
     *  @return a mapping of the Tags in the list Tag object in the master list.
     */
    private Map<Tag, Tag> updateMasterEventTagList(UniqueTagList objectTags) {
        eventTags.mergeFrom(objectTags);

        // Create map with values = tag object references in the master list
        // used for checking tag references
        final Map<Tag, Tag> masterTagObjects = new HashMap<>();
        eventTags.forEach(tag -> masterTagObjects.put(tag, tag));

        return masterTagObjects;
    }

    public void addPersonTag(Tag t) throws UniqueTagList.DuplicateTagException {
        personTags.add(t);
    }

    public void addEventTag(Tag t) throws UniqueTagList.DuplicateTagException {
        eventTags.add(t);
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java

    @Override
    public void toggleAttendance(Person person, EpicEvent event)
        throws EventNotFoundException, PersonNotFoundInEventException {
        requireAllNonNull(person, event);

        eventPlanner.toggleAttendance(person, event);
        indicateEventPlannerChanged();
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    //=========== Filtered Event List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the filtered list of {@code EpicEvent} backed by the internal list of
     * {@code eventPlanner}
     */
    @Override
    public ObservableList<EpicEvent> getFilteredEventList() {
        return FXCollections.unmodifiableObservableList(filteredEvents);
    }

    @Override
    public void updateFilteredEventList(Predicate<EpicEvent> predicate) {
        requireNonNull(predicate);
        filteredEvents.setPredicate(predicate);
    }
```
###### /java/seedu/address/model/ModelManager.java
``` java

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return eventPlanner.equals(other.eventPlanner)
                && filteredPersons.equals(other.filteredPersons)
                && filteredEvents.equals(other.filteredEvents);
    }

}
```
###### /java/seedu/address/model/event/exceptions/DuplicateEventException.java
``` java
/**
 * Signals that the operation will result in duplicate EpicEvent objects.
 */
public class DuplicateEventException extends DuplicateDataException {
    public DuplicateEventException() {
        super("Operation would result in duplicate events");
    }
}
```
###### /java/seedu/address/model/event/UniqueEpicEventList.java
``` java
/**
 * A list of events that enforces uniqueness between its elements and does not allow nulls.
 *
 * Supports a minimal set of list operations.
 *
 * @see EpicEvent#equals(Object)
 * @see CollectionUtil#elementsAreUnique(Collection)
 */
public class UniqueEpicEventList {

    private final ObservableList<EpicEvent> internalList = FXCollections.observableArrayList();

    /**
     * Returns true if the list contains an equivalent event as the given argument.
     */
    public boolean contains(EpicEvent toCheck) {
        requireNonNull(toCheck);
        return internalList.contains(toCheck);
    }

    /**
     * Adds an event to the list.
     *
     * @throws DuplicateEventException if the event to add is a duplicate of an existing event in the list.
     */
    public void add(EpicEvent toAdd) throws DuplicateEventException {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateEventException();
        }
        internalList.add(toAdd);
    }

```
###### /java/seedu/address/model/event/UniqueEpicEventList.java
``` java
    /**
     * Toggles the attendance of the person in the event.
     *
     * @throws PersonNotFoundInEventException if person could not be found in event
     * @throws EventNotFoundException if no such event could be found in the list
     */
    public void toggleAttendance(Person person, EpicEvent eventToToggleAttendance)
            throws PersonNotFoundInEventException, EventNotFoundException {
        requireAllNonNull(person, eventToToggleAttendance);

        int index = internalList.indexOf(eventToToggleAttendance);
        if (index == -1) {
            throw new EventNotFoundException();
        }

        eventToToggleAttendance.toggleAttendance(person);
    }

```
###### /java/seedu/address/model/event/UniqueEpicEventList.java
``` java
    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniqueEpicEventList // instanceof handles nulls
                && this.internalList.equals(((UniqueEpicEventList) other).internalList));
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<EpicEvent> asObservableList() {
        return FXCollections.unmodifiableObservableList(internalList);
    }
    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

}
```