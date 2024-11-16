package mscalc.engine.commands;

public interface IExpressionCommand {
    CommandType getCommandType();
    void accept(ISerializeCommandVisitor commandVisitor);
}
