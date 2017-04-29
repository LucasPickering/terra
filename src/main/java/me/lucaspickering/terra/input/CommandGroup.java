package me.lucaspickering.terra.input;

import java.util.HashMap;
import java.util.Map;

public enum CommandGroup {

    GAME("game"),
    WORLD("world");

    private static final Map<String, CommandGroup> stringMappings = new HashMap<>();

    static {
        // Initialize the string:CommandGroup map
        for (CommandGroup group : values()) {
            stringMappings.put(group.name, group);
        }
    }

    private final String name;
    private final Map<String, Command> commands = new HashMap<>();

    CommandGroup(String name) {
        this.name = name;
    }

    public void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public String getName() {
        return name;
    }

    public Command getCommandByName(String commandName) {
        return commands.get(commandName);
    }

    public static CommandGroup getByString(String groupName) {
        return stringMappings.get(groupName);
    }
}
