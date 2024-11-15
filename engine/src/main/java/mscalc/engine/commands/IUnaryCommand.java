package mscalc.engine.commands;

import java.util.List;

public interface IUnaryCommand extends IOperatorCommand {
    List<Integer> getCommands();
    void setCommands(int command1, int command2);
}
