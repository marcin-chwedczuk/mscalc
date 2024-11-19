package mscalc.engine;

import mscalc.engine.commands.IExpressionCommand;

import java.util.ArrayList;
import java.util.List;

public class CalculatorHistory implements HistoryDisplay {
    static class HISTORYITEMVECTOR
    {
        List<Pair<String, Integer>> spTokens = new ArrayList<>();
        List<IExpressionCommand> spCommands = new ArrayList<>();
        String expression = "";
        String result = "";
    }

    static class HISTORYITEM
    {
        HISTORYITEMVECTOR historyItemVector = new HISTORYITEMVECTOR();
    }

    private final List<HISTORYITEM> m_historyItems = new ArrayList<>();
    private final int m_maxHistorySize;

    public CalculatorHistory(int maxSize) {
        this.m_maxHistorySize = maxSize;
    }

    public int MaxHistorySize()
    {
        return m_maxHistorySize;
    }

    static String GetGeneratedExpression(List<Pair<String, Integer>> tokens)
    {
        StringBuilder expression = new StringBuilder();
        boolean isFirst = true;

        for (var token : tokens)
        {
            if (isFirst)
            {
                isFirst = false;
            }
            else
            {
                expression.append(' ');
            }
            expression.append(token.getKey());
        }

        return expression.toString();
    }

    @Override
    public int addToHistory(List<Pair<String, Integer>> tokens, List<IExpressionCommand> commands, String result) {
        HISTORYITEM spHistoryItem = new HISTORYITEM();

        spHistoryItem.historyItemVector.spTokens = tokens;
        spHistoryItem.historyItemVector.spCommands = commands;
        spHistoryItem.historyItemVector.expression = GetGeneratedExpression(tokens);
        spHistoryItem.historyItemVector.result = result;
        return AddItem(spHistoryItem);
    }

    int AddItem(HISTORYITEM spHistoryItem)
    {
        if (m_historyItems.size() >= m_maxHistorySize)
        {
            m_historyItems.removeFirst();
        }

        m_historyItems.add(spHistoryItem);
        return (m_historyItems.size() - 1);
    }

    boolean RemoveItem(int uIdx)
    {
        if (uIdx < m_historyItems.size())
        {
            m_historyItems.remove(uIdx);
            return true;
        }

        return false;
    }

    List<HISTORYITEM> GetHistory()
    {
        return m_historyItems;
    }

    HISTORYITEM GetHistoryItem(int uIdx)
    {
        assert(uIdx < m_historyItems.size());
        return m_historyItems.get(uIdx);
    }

    void ClearHistory()
    {
        m_historyItems.clear();
    }

    @Override
    public void close() throws Exception {

    }
}
