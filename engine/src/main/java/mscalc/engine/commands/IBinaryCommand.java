package mscalc.engine.commands;

public interface IBinaryCommand extends IOperatorCommand {
    void setCommand(int command);
    int getCommand();
}
