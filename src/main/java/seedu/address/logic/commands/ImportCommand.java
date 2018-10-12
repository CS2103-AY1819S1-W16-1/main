package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Room;
import seedu.address.model.person.School;
import seedu.address.model.tag.Tag;

//@@author kengwoon
/**
 * Imports an XML file to update the address book.
 */
public class ImportCommand extends Command {

    public static final String COMMAND_WORD = "import";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Imports an XML file to update the address book. "
            + "Parameters: "
            + "import "
            + "f/FILENAME.xml";

    public static final String MESSAGE_SUCCESS = "%1$s file read and database updated.";
    public static final String MESSAGE_FILE_NOT_FOUND = "File not found.";
    public static final String MESSAGE_FILE_EMPTY = "Unable to read. File is empty.";
    public static final String MESSAGE_CONFIG_ERR = "Configuration error.";
    public static final String MESSAGE_PARSE_ERR = "Error parsing XML file.";

    private final File file;
    private final List<Person> personList;


    /**
     * Creates an ImportCommand to import the specified file.
     */
    public ImportCommand(File file) {
        requireNonNull(file);
        this.file = file;
        this.personList = new ArrayList<>();
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            personList.clear();
            Set<Tag> tags = new HashSet<>();
            NodeList nList = doc.getElementsByTagName("persons");
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                tags.clear();
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    Name name = new Name(element.getElementsByTagName("name").item(0).getTextContent());
                    Phone phone = new Phone(element.getElementsByTagName("phone").item(0).getTextContent());
                    Email email = new Email(element.getElementsByTagName("email").item(0).getTextContent());
                    Room room = new Room(element.getElementsByTagName("room").item(0).getTextContent());
                    School school = new School(element.getElementsByTagName("school").item(0).getTextContent());
                    NodeList tagged = element.getElementsByTagName("tagged");
                    if (tagged.getLength() != 0) {
                        for (int j = 0; j < tagged.getLength(); j++) {
                            tags.add(new Tag(tagged.item(j).getTextContent()));
                        }
                    }
                    Person temp = new Person(name, phone, email, room, school, tags);
                    personList.add(temp);
                }
            }

            model.addMultiplePersons(personList);
            model.commitAddressBook();
            return new CommandResult(String.format(MESSAGE_SUCCESS, file.getName()));
        } catch (FileNotFoundException e) {
            throw new CommandException(MESSAGE_FILE_NOT_FOUND);
        } catch (IOException e) {
            throw new CommandException(MESSAGE_FILE_EMPTY);
        } catch (ParserConfigurationException e) {
            throw new CommandException(MESSAGE_CONFIG_ERR);
        } catch (SAXException e) {
            throw new CommandException(MESSAGE_PARSE_ERR);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ImportCommand // instanceof handles nulls
                && file.equals(((ImportCommand) other).file));
    }
}
