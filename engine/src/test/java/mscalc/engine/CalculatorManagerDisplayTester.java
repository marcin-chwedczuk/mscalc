package mscalc.engine;

import mscalc.engine.commands.IExpressionCommand;

import java.util.ArrayList;
import java.util.List;

public class CalculatorManagerDisplayTester implements CalcDisplay {
    private String m_primaryDisplay;
    private String m_expression;
    private int m_parenDisplay;
    private boolean m_isError;
    private List<String> m_memorizedNumberStrings = new ArrayList<>();
    private int m_maxDigitsCalledCount;
    private int m_binaryOperatorReceivedCallCount;

    public CalculatorManagerDisplayTester() {
        reset();
    }

    public void reset()
    {
        m_isError = false;
        m_maxDigitsCalledCount = 0;
        m_binaryOperatorReceivedCallCount = 0;
    }

    @Override
    public void setPrimaryDisplay(String text, boolean isError) {
        m_primaryDisplay = text;
        m_isError = isError;
    }

    @Override
    public void setIsInError(boolean isInError) {
        m_isError = isInError;
    }

    @Override
    public void setExpressionDisplay(List<Pair<String, Integer>> tokens, List<IExpressionCommand> commands) {
        StringBuilder tmp = new StringBuilder();

        for (var currentPair : tokens)
        {
            tmp.append( currentPair.getKey() );
        }

        m_expression = tmp.toString();
    }

    @Override
    public void setParenthesisNumber(int count) {
        m_parenDisplay = count;
    }

    @Override
    public void onNoRightParenAdded() {
        // This method is used to create a narrator announcement when a close parenthesis cannot be added because there are no open parentheses
    }

    @Override
    public void maxDigitsReached() {
        m_maxDigitsCalledCount++;
    }

    @Override
    public void binaryOperatorReceived() {
        m_binaryOperatorReceivedCallCount++;
    }

    @Override
    public void onHistoryItemAdded(int addedItemIndex) {
        // empty
    }

    @Override
    public void setMemorizedNumbers(List<String> memorizedNumbers) {
        m_memorizedNumberStrings = memorizedNumbers;
    }

    @Override
    public void memoryItemChanged(int indexOfMemory) {
        // empty
    }

    @Override
    public void inputChanged() {
        // empty
    }

    public String GetPrimaryDisplay()
    {
        return m_primaryDisplay;
    }

    public String GetExpression()
    {
        return m_expression;
    }

    public List<String> GetMemorizedNumbers()
    {
        return m_memorizedNumberStrings;
    }

    public boolean GetIsError()
    {
        return m_isError;
    }

    public int GetMaxDigitsCalledCount()
    {
        return m_maxDigitsCalledCount;
    }

    public int GetBinaryOperatorReceivedCallCount()
    {
        return m_binaryOperatorReceivedCallCount;
    }
}
