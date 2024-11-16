package mscalc.engine.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CUnaryCommand implements IUnaryCommand {
    private final List<Integer> command = new ArrayList<>();

    public CUnaryCommand(int command) {
        this.command.add(command);
    }

    public CUnaryCommand(int command1, int command2) {
        this.command.add(command1);
        this.command.add(command2);
    }

    @Override
    public List<Integer> getCommands() {
        return Collections.unmodifiableList(command);
    }

    @Override
    public void setCommands(int command1, int command2) {
        this.command.clear();
        this.command.add(command1);
        this.command.add(command2);
    }

    @Override
    public void setCommand(int command) {
        this.command.clear();
        this.command.add(command);
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.UnaryCommand;
    }

    @Override
    public void accept(ISerializeCommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
