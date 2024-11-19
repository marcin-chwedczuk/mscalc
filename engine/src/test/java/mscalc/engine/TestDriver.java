package mscalc.engine;

import mscalc.engine.commands.Command;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDriver {
    static CalculatorManagerDisplayTester m_displayTester;
    static CalculatorManager m_calculatorManager;

    static void Initialize(CalculatorManagerDisplayTester displayTester, CalculatorManager calculatorManager) {
        m_displayTester = displayTester;
        m_calculatorManager = calculatorManager;
    }

    static void Test(String expectedPrimary, String expectedExpression, Command[] testCommands) {
        Test(expectedPrimary, expectedExpression, Arrays.asList(testCommands));
    }

    static void Test(String expectedPrimary, String expectedExpression, List<Command> testCommands) {
        Test(expectedPrimary, expectedExpression, testCommands, true, false);
    }

    static void Test(String expectedPrimary, String expectedExpression, Command[] testCommands, boolean cleanup, boolean isScientific) {
        Test(expectedPrimary, expectedExpression, Arrays.asList(testCommands), cleanup, isScientific);
    }

    static void Test(String expectedPrimary, String expectedExpression, List<Command> testCommands, boolean cleanup, boolean isScientific) {
        if (cleanup) {
            m_calculatorManager.Reset();
        }

        if (isScientific) {
            m_calculatorManager.sendCommand(Command.ModeScientific);
        }

        for (Command currentCommand : testCommands) {
            if (currentCommand == Command.CommandNULL) break;
            m_calculatorManager.sendCommand(currentCommand);
        }

        assertEquals(expectedPrimary, m_displayTester.GetPrimaryDisplay());
        if (!"N/A".equals(expectedExpression)) {
            assertEquals(expectedExpression, m_displayTester.GetExpression());
        }
    }

}
