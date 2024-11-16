package mscalc.engine;

import mscalc.engine.commands.IExpressionCommand;

import java.util.List;
import java.util.Map;

public interface HistoryDisplay extends AutoCloseable {
    int addToHistory(
            List<Pair<String, Integer>> tokens,
            List<IExpressionCommand> commands,
            String result);
}
