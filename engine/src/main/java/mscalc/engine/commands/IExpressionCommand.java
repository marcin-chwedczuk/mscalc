package mscalc.engine.commands;

import mscalc.engine.CommandType;

public interface IExpressionCommand {
    CommandType getCommandType();
    void accept(ISerializeCommandVisitor commandVisitor);
}
