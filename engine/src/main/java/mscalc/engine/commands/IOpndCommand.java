package mscalc.engine.commands;

import java.util.List;

public interface IOpndCommand extends IExpressionCommand {
    List<Integer> getCommands();
    void appendCommand(int command);
    void toggleSign();
    void removeFromEnd();
    boolean isNegative();
    boolean isSciFmt();
    boolean isDecimalPresent();
    String getToken(char decimalSymbol);
    void setCommands(List<Integer> commands);
}
