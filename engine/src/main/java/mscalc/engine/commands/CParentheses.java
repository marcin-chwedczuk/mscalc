package mscalc.engine.commands;

public class CParentheses implements IParenthesisCommand {
    private final int command;

    public CParentheses(int command) {
        this.command = command;
    }

    @Override
    public int getCommand() {
        return command;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.Parentheses;
    }

    @Override
    public void accept(ISerializeCommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
