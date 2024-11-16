package mscalc.engine;

import mscalc.engine.commands.IExpressionCommand;

import java.util.List;
import java.util.Map;

public interface CalcDisplay {
     void setPrimaryDisplay(String text, boolean isError);
     void setIsInError(boolean isInError);
     void setExpressionDisplay(
             List<Pair<String, Integer>> tokens,
             List<IExpressionCommand> commands);

     void setParenthesisNumber(int count);
     void onNoRightParenAdded();
     void maxDigitsReached(); // not an error but still need to inform UI layer.
     void binaryOperatorReceived();
     void onHistoryItemAdded(int addedItemIndex);
     void setMemorizedNumbers(List<String> memorizedNumbers);
     void memoryItemChanged(int indexOfMemory);
     void inputChanged();
}
