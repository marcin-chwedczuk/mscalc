package mscalc.engine.commands;

public class CBinaryCommand implements IBinaryCommand {
    private int command;

    public CBinaryCommand(int command) {
        this.command = command;
    }

    @Override
    public void setCommand(int command) {
        this.command = command;
    }

    @Override
    public int getCommand() {
        return command;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.BinaryCommand;
    }

    @Override
    public void accept(ISerializeCommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
