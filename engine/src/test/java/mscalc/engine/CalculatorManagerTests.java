package mscalc.engine;

import mscalc.engine.commands.Command;
import mscalc.engine.resource.JavaBundleResourceProvider;
import mscalc.engine.resource.ResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorManagerTests {

    static CalculatorManager m_calculatorManager;
    static ResourceProvider m_resourceProvider;
    static CalculatorManagerDisplayTester m_calculatorDisplayTester;

    List<Command> CommandListFromStringInput(String input) {
        List<Command> result = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            Command asCommand = Command.CommandNULL;
            if (ch == '.') {
                asCommand = Command.CommandPNT;
            } else if ('0' <= ch && ch <= '9') {
                int diff = (int) (ch) - (int) ('0');
                asCommand = Command.fromInt(diff + Command.Command0.toInt());
            }

            if (asCommand != Command.CommandNULL) {
                result.add(asCommand);
            }
        }

        return result;
    }

    void TestMaxDigitsReachedScenario(String constInput) {
        CalculatorManagerDisplayTester pCalculatorDisplay = m_calculatorDisplayTester;

        // Make sure we're in a clean state.
        assertEquals(0, pCalculatorDisplay.GetMaxDigitsCalledCount());

        List<Command> commands = CommandListFromStringInput(constInput);
        assertFalse(commands.isEmpty());

        // The last element in the list should always cause MaxDigitsReached
        // Remember the command but remove from the actual input that is sent
        Command finalInput = commands.get(commands.size() - 1);
        commands.removeLast();
        String input = constInput.substring(0, constInput.length() - 1);

        m_calculatorManager.SetStandardMode();
        ExecuteCommands(commands.toArray(new Command[0]));

        String expectedDisplay = input;
        String display = pCalculatorDisplay.GetPrimaryDisplay();
        assertEquals(expectedDisplay, display);

        m_calculatorManager.SendCommand(finalInput);

        // Verify MaxDigitsReached
        display = pCalculatorDisplay.GetPrimaryDisplay();
        assertEquals(expectedDisplay, display);

        // MaxDigitsReached should have been called once
        assertTrue(0 < pCalculatorDisplay.GetMaxDigitsCalledCount());
    }

    // Creates instance of CalculationManager before running tests
    @BeforeEach
    void CommonSetup() {
        m_calculatorDisplayTester = new CalculatorManagerDisplayTester();
        m_resourceProvider = new JavaBundleResourceProvider();
        m_calculatorManager = new CalculatorManager(m_calculatorDisplayTester, m_resourceProvider);
        TestDriver.Initialize(m_calculatorDisplayTester, m_calculatorManager);
    }

    void ExecuteCommands(Command[] commands) {
        for (Command cmd : commands) {
            if (cmd == Command.CommandNULL) break;
            m_calculatorManager.SendCommand(cmd);
        }
    }

    void ExecuteCommands(List<Command> commands) {
        for (Command command : commands) {
            if (command == Command.CommandNULL) {
                break;
            }

            m_calculatorManager.SendCommand(command);
        }
    }

    @Test
    void CalculatorManagerTestStandard() {
        Command commands1[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandPNT,
                Command.Command4, Command.Command5, Command.Command6, Command.CommandNULL};
        TestDriver.Test("123.456", "", commands1);

        Command commands2[] = {Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "0 + ", commands2);

        Command commands3[] = {Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("0", "\u221A(0)", commands3);

        Command commands4[] = {Command.Command2, Command.CommandADD, Command.Command3, Command.CommandEQU,
                Command.Command4, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("7", "4 + 3=", commands4);

        Command commands5[] = {Command.Command4, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("4", "4=", commands5);

        Command commands6[] = {Command.Command2, Command.Command5, Command.Command6, Command.CommandSQRT,
                Command.CommandSQRT, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("2", "\u221A(\u221A(\u221A(256)))", commands6);

        Command commands7[] = {Command.Command3, Command.CommandSUB, Command.Command6, Command.CommandEQU,
                Command.CommandMUL, Command.Command3, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("-9", "-3 \u00D7 3=", commands7);

        Command commands8[] = {Command.Command9, Command.CommandMUL, Command.Command6, Command.CommandSUB,
                Command.CommandCENTR, Command.Command8, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("46", "54 - 8=", commands8);

        Command commands9[] = {Command.Command6, Command.CommandMUL, Command.Command6, Command.CommandPERCENT, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("0.36", "6 \u00D7 0.06=", commands9);

        Command commands10[] = {Command.Command5, Command.Command0, Command.CommandADD, Command.Command2,
                Command.Command0, Command.CommandPERCENT, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("60", "50 + 10=", commands10);

        Command commands11[] = {Command.Command4, Command.CommandADD, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("8", "4 + 4=", commands11);

        Command commands12[] = {Command.Command5, Command.CommandADD, Command.CommandMUL, Command.Command3, Command.CommandNULL};
        TestDriver.Test("3", "5 \u00D7 ", commands12);

        Command commands13[] = {Command.Command1, Command.CommandEXP, Command.CommandSIGN, Command.Command9, Command.Command9, Command.Command9,
                Command.Command9, Command.CommandDIV, Command.Command1, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Overflow", "1.e-9999 \u00F7 ", commands13);

        Command commands14[] = {Command.Command5, Command.Command0, Command.CommandADD, Command.Command2,
                Command.Command0, Command.CommandPERCENT, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("60", "50 + 10=", commands14);

        Command commands15[] = {Command.Command0, Command.CommandDIV, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Result is undefined", "0 \u00F7 ", commands15);

        Command commands16[] = {Command.Command1, Command.CommandDIV, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Cannot divide by zero", "1 \u00F7 ", commands16);

        Command commands17[] = {Command.Command1, Command.Command2, Command.CommandADD, Command.Command5,
                Command.CommandCENTR, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("14", "14 + ", commands17);

        Command commands18[] = {Command.Command1, Command.Command0, Command.Command0, Command.CommandSIGN, Command.CommandREC, Command.CommandNULL};
        TestDriver.Test("-0.01", "1/(-100)", commands18);

        Command commands19[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandBACK, Command.CommandBACK, Command.CommandNULL};
        TestDriver.Test("1", "", commands19);

        Command commands20[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandBACK,
                Command.CommandBACK, Command.CommandBACK, Command.CommandNULL};
        TestDriver.Test("0", "", commands20);

        Command commands21[] = {Command.Command4, Command.CommandSQRT, Command.CommandSUB, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "0 + ", commands21);

        Command commands22[] = {Command.Command1, Command.Command0, Command.Command2, Command.Command4, Command.CommandSQRT,
                Command.CommandSUB, Command.Command3, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "0 + ", commands22);
    }

    @Test
    void CalculatorManagerTestScientific() {
        Command commands1[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandPNT,
                Command.Command4, Command.Command5, Command.Command6, Command.CommandNULL};
        TestDriver.Test("123.456", "", commands1, true, true);

        Command commands2[] = {Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "0 + ", commands2, true, true);

        Command commands3[] = {Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("0", "\u221A(0)", commands3, true, true);

        Command commands4[] = {Command.Command1, Command.CommandADD, Command.Command0, Command.CommandMUL,
                Command.Command2, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("1", "1 + 0 \u00D7 2=", commands4, true, true);

        Command commands5[] = {Command.Command4, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("4", "4=", commands5, true, true);

        Command commands6[] = {Command.Command2, Command.Command5, Command.Command6, Command.CommandSQRT,
                Command.CommandSQRT, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("2", "\u221A(\u221A(\u221A(256)))", commands6, true, true);

        Command commands7[] = {Command.Command3, Command.CommandSUB, Command.Command6, Command.CommandEQU,
                Command.CommandMUL, Command.Command3, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("-9", "-3 \u00D7 3 + ", commands7, true, true);

        Command commands8[] = {Command.Command9, Command.CommandMUL, Command.Command6, Command.CommandSUB, Command.CommandCENTR,
                Command.Command8, Command.CommandMUL, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("38", "9 \u00D7 6 - 8 \u00D7 2 + ", commands8, true, true);

        Command commands9[] = {Command.Command6, Command.CommandMUL, Command.Command6, Command.CommandSIGN, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("Invalid input", "6 \u00D7 \u221A(-6)", commands9, true, true);

        Command commands10[] = {Command.Command5, Command.Command0, Command.CommandADD, Command.Command2,
                Command.Command0, Command.CommandREC, Command.CommandSUB, Command.CommandNULL};
        TestDriver.Test("50.05", "50 + 1/(20) - ", commands10, true, true);

        Command commands11[] = {Command.Command4, Command.CommandADD, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("8", "4 + 4=", commands11, true, true);

        Command commands12[] = {Command.Command5, Command.CommandADD, Command.CommandMUL, Command.Command3, Command.CommandNULL};
        TestDriver.Test("3", "5 \u00D7 ", commands12, true, true);

        Command commands13[] = {Command.Command1, Command.CommandEXP, Command.CommandSIGN, Command.Command9, Command.Command9, Command.Command9,
                Command.Command9, Command.CommandDIV, Command.Command1, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Overflow", "1.e-9999 \u00F7 ", commands13, true, true);

        Command commands14[] = {Command.Command5, Command.Command0, Command.CommandADD, Command.Command2,
                Command.Command0, Command.CommandPERCENT, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("60", "50 + 10=", commands14, true, true);

        Command commands15[] = {Command.Command0, Command.CommandDIV, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Result is undefined", "0 \u00F7 ", commands15, true, true);

        Command commands16[] = {Command.Command1, Command.CommandDIV, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Cannot divide by zero", "1 \u00F7 ", commands16, true, true);

        Command commands17[] = {Command.Command1, Command.Command2, Command.CommandADD, Command.Command5,
                Command.CommandCENTR, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("14", "12 + 2 + ", commands17, true, true);

        Command commands18[] = {Command.Command1, Command.Command0, Command.Command0, Command.CommandSIGN, Command.CommandREC, Command.CommandNULL};
        TestDriver.Test("-0.01", "1/(-100)", commands18, true, true);

        Command commands19[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandBACK, Command.CommandBACK, Command.CommandNULL};
        TestDriver.Test("1", "", commands19, true, true);

        Command commands20[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandBACK,
                Command.CommandBACK, Command.CommandBACK, Command.CommandNULL};
        TestDriver.Test("0", "", commands20, true, true);

        Command commands21[] = {Command.Command4, Command.CommandSQRT, Command.CommandSUB, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "\u221A(4) - 2 + ", commands21, true, true);

        Command commands22[] = {Command.Command0, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("0", "\u221A(0)", commands22, true, true);

        Command commands23[] = {Command.Command1, Command.Command0, Command.Command2, Command.Command4, Command.CommandSQRT,
                Command.CommandSUB, Command.Command3, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "\u221A(1024) - 32 + ", commands23, true, true);

        Command commands24[] = {Command.Command2, Command.Command5, Command.Command7, Command.CommandSQRT,
                Command.CommandSQRT, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("2.0009748976330773374220277351385", "\u221A(\u221A(\u221A(257)))", commands24, true, true);
    }

    // Scientific functions from the scientific calculator
    @Test
    void CalculatorManagerTestScientific2() {
        Command commands1[] = {Command.Command1, Command.Command2, Command.CommandSQR, Command.CommandNULL};
        TestDriver.Test("144", "sqr(12)", commands1, true, true);

        Command commands2[] = {Command.Command5, Command.CommandFAC, Command.CommandNULL};
        TestDriver.Test("120", "fact(5)", commands2, true, true);

        Command commands3[] = {Command.Command5, Command.CommandPWR, Command.Command2, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("25", "5 ^ 2 + ", commands3, true, true);

        Command commands4[] = {Command.Command8, Command.CommandROOT, Command.Command3, Command.CommandMUL, Command.CommandNULL};
        TestDriver.Test("2", "8 yroot 3 \u00D7 ", commands4, true, true);

        Command commands5[] = {Command.Command8, Command.CommandCUB, Command.CommandNULL};
        TestDriver.Test("512", "cube(8)", commands5, true, true);

        Command commands6[] = {Command.Command8, Command.CommandCUB, Command.CommandCUBEROOT, Command.CommandNULL};
        TestDriver.Test("8", "cuberoot(cube(8))", commands6, true, true);

        Command commands7[] = {Command.Command1, Command.Command0, Command.CommandLOG, Command.CommandNULL};
        TestDriver.Test("1", "log(10)", commands7, true, true);

        Command commands8[] = {Command.Command5, Command.CommandPOW10, Command.CommandNULL};
        TestDriver.Test("100,000", "10^(5)", commands8, true, true);

        Command commands9[] = {Command.Command1, Command.Command0, Command.CommandLN, Command.CommandNULL};
        TestDriver.Test("2.3025850929940456840179914546844", "ln(10)", commands9, true, true);

        Command commands10[] = {Command.Command1, Command.CommandSIN, Command.CommandNULL};
        TestDriver.Test("0.01745240643728351281941897851632", "sin\u2080(1)", commands10, true, true);

        Command commands11[] = {Command.Command1, Command.CommandCOS, Command.CommandNULL};
        TestDriver.Test("0.99984769515639123915701155881391", "cos\u2080(1)", commands11, true, true);

        Command commands12[] = {Command.Command1, Command.CommandTAN, Command.CommandNULL};
        TestDriver.Test("0.01745506492821758576512889521973", "tan\u2080(1)", commands12, true, true);

        Command commands13[] = {Command.Command1, Command.CommandASIN, Command.CommandNULL};
        TestDriver.Test("90", "sin\u2080\u207B\u00B9(1)", commands13, true, true);

        Command commands14[] = {Command.Command1, Command.CommandACOS, Command.CommandNULL};
        TestDriver.Test("0", "cos\u2080\u207B\u00B9(1)", commands14, true, true);

        Command commands15[] = {Command.Command1, Command.CommandATAN, Command.CommandNULL};
        TestDriver.Test("45", "tan\u2080\u207B\u00B9(1)", commands15, true, true);

        Command commands16[] = {Command.Command2, Command.CommandPOWE, Command.CommandNULL};
        TestDriver.Test("7.389056098930650227230427460575", "e^(2)", commands16, true, true);

        Command commands17[] = {Command.Command5, Command.CommandPWR, Command.Command0, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("1", "5 ^ 0 + ", commands17, true, true);

        Command commands18[] = {Command.Command0, Command.CommandPWR, Command.Command0, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("1", "0 ^ 0 + ", commands18, true, true);

        Command commands19[] = {Command.Command2, Command.Command7, Command.CommandSIGN, Command.CommandROOT,
                Command.Command3, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("-3", "-27 yroot 3 + ", commands19, true, true);

        Command commands20[] = {Command.Command8, Command.CommandPWR, Command.CommandOPENP, Command.Command2,
                Command.CommandDIV, Command.Command3, Command.CommandCLOSEP, Command.CommandSUB,
                Command.Command4, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "8 ^ (2 \u00F7 3) - 4 + ", commands20, true, true);

        Command commands21[] = {Command.Command4, Command.CommandPWR, Command.CommandOPENP, Command.Command3,
                Command.CommandDIV, Command.Command2, Command.CommandCLOSEP, Command.CommandSUB,
                Command.Command8, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("0", "4 ^ (3 \u00F7 2) - 8 + ", commands21, true, true);

        Command commands22[] = {Command.Command1, Command.Command0, Command.CommandPWR, Command.Command1, Command.CommandPNT, Command.Command2,
                Command.Command3, Command.Command4, Command.Command5, Command.Command6, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("17.161687912241792074207286679393", "10 ^ 1.23456 + ", commands22, true, true);

        Command commands23[] = {Command.Command1, Command.CommandSEC, Command.CommandNULL};
        TestDriver.Test("1.0001523280439076654284264342126", "sec\u2080(1)", commands23, true, true);

        Command commands24[] = {Command.Command1, Command.CommandCSC, Command.CommandNULL};
        TestDriver.Test("57.298688498550183476612683735174", "csc\u2080(1)", commands24, true, true);

        Command commands25[] = {Command.Command1, Command.CommandCOT, Command.CommandNULL};
        TestDriver.Test("57.289961630759424687278147537113", "cot\u2080(1)", commands25, true, true);

        Command commands26[] = {Command.Command1, Command.CommandASEC, Command.CommandNULL};
        TestDriver.Test("0", "sec\u2080\u207B\u00B9(1)", commands26, true, true);

        Command commands27[] = {Command.Command1, Command.CommandACSC, Command.CommandNULL};
        TestDriver.Test("90", "csc\u2080\u207B\u00B9(1)", commands27, true, true);

        Command commands28[] = {Command.Command1, Command.CommandACOT, Command.CommandNULL};
        TestDriver.Test("45", "cot\u2080\u207B\u00B9(1)", commands28, true, true);

        Command commands29[] = {Command.Command1, Command.CommandSECH, Command.CommandNULL};
        TestDriver.Test("0.64805427366388539957497735322615", "sech(1)", commands29, true, true);

        Command commands30[] = {Command.Command1, Command.CommandCSCH, Command.CommandNULL};
        TestDriver.Test("0.85091812823932154513384276328718", "csch(1)", commands30, true, true);

        Command commands31[] = {Command.Command1, Command.CommandCOTH, Command.CommandNULL};
        TestDriver.Test("1.3130352854993313036361612469308", "coth(1)", commands31, true, true);

        Command commands32[] = {Command.Command1, Command.CommandASECH, Command.CommandNULL};
        TestDriver.Test("0", "sech\u207B\u00B9(1)", commands32, true, true);

        Command commands33[] = {Command.Command1, Command.CommandACSCH, Command.CommandNULL};
        TestDriver.Test("0.88137358701954302523260932497979", "csch\u207B\u00B9(1)", commands33, true, true);

        Command commands34[] = {Command.Command2, Command.CommandACOTH, Command.CommandNULL};
        TestDriver.Test("0.54930614433405484569762261846126", "coth\u207B\u00B9(2)", commands34, true, true);

        Command commands35[] = {Command.Command8, Command.CommandPOW2, Command.CommandNULL};
        TestDriver.Test("256", "2^(8)", commands35, true, true);

        Command commands36[] = {Command.CommandRand, Command.CommandCeil, Command.CommandNULL};
        TestDriver.Test("1", "N/A", commands36, true, true);

        Command commands37[] = {Command.CommandRand, Command.CommandFloor, Command.CommandNULL};
        TestDriver.Test("0", "N/A", commands37, true, true);

        Command commands38[] = {Command.CommandRand, Command.CommandSIGN, Command.CommandCeil, Command.CommandNULL};
        TestDriver.Test("0", "N/A", commands38, true, true);

        Command commands39[] = {Command.CommandRand, Command.CommandSIGN, Command.CommandFloor, Command.CommandNULL};
        TestDriver.Test("-1", "N/A", commands39, true, true);

        Command commands40[] = {Command.Command3, Command.CommandPNT, Command.Command8, Command.CommandFloor, Command.CommandNULL};
        TestDriver.Test("3", "floor(3.8)", commands40, true, true);

        Command commands41[] = {Command.Command3, Command.CommandPNT, Command.Command8, Command.CommandCeil, Command.CommandNULL};
        TestDriver.Test("4", "ceil(3.8)", commands41, true, true);

        Command commands42[] = {Command.Command5, Command.CommandLogBaseY, Command.Command3, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("1.4649735207179271671970404076786", "5 log base 3 + ", commands42, true, true);
    }

    @Test
    void CalculatorManagerTestScientificParenthesis() {
        Command commands1[] = {Command.Command1, Command.CommandADD, Command.CommandOPENP, Command.CommandADD,
                Command.Command3, Command.CommandCLOSEP, Command.CommandNULL};
        TestDriver.Test("3", "1 + (0 + 3)", commands1, true, true);

        Command commands2[] = {
                Command.CommandOPENP, Command.CommandOPENP, Command.Command1, Command.Command2, Command.CommandCLOSEP, Command.CommandNULL
        };
        TestDriver.Test("12", "((12)", commands2, true, true);

        Command commands3[] = {Command.Command1, Command.Command2, Command.CommandCLOSEP,
                Command.CommandCLOSEP, Command.CommandOPENP, Command.CommandNULL};
        TestDriver.Test("12", "12 \u00D7 (", commands3, true, true);

        Command commands4[] = {
                Command.Command2, Command.CommandOPENP, Command.Command2, Command.CommandCLOSEP, Command.CommandADD, Command.CommandNULL
        };
        TestDriver.Test("4", "2 \u00D7 (2) + ", commands4, true, true);

        Command commands5[] = {Command.Command2, Command.CommandOPENP, Command.Command2, Command.CommandCLOSEP,
                Command.CommandADD, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("8", "2 \u00D7 (2) + 4=", commands5, true, true);
    }

    @Test
    void CalculatorManagerTestScientificError() {
        Command commands1[] = {Command.Command1, Command.CommandDIV, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Cannot divide by zero", "1 \u00F7 ", commands1, true, true);
        assertTrue(m_calculatorDisplayTester.GetIsError());

        Command commands2[] = {Command.Command2, Command.CommandSIGN, Command.CommandLOG, Command.CommandNULL};
        TestDriver.Test("Invalid input", "log(-2)", commands2, true, true);
        assertTrue(m_calculatorDisplayTester.GetIsError());

        Command commands3[] = {Command.Command0, Command.CommandDIV, Command.Command0, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("Result is undefined", "0 \u00F7 ", commands3, true, true);
        assertTrue(m_calculatorDisplayTester.GetIsError());

        // Do the same tests for the basic calculator
        TestDriver.Test("Cannot divide by zero", "1 \u00F7 ", commands1);
        assertTrue(m_calculatorDisplayTester.GetIsError());
        TestDriver.Test("Invalid input", "log(-2)", commands2);
        assertTrue(m_calculatorDisplayTester.GetIsError());
        TestDriver.Test("Result is undefined", "0 \u00F7 ", commands3);
        assertTrue(m_calculatorDisplayTester.GetIsError());
    }

    // Radians and Grads Test
    @Test
    void CalculatorManagerTestScientificModeChange() {
        Command commands1[] = {Command.CommandRAD, Command.CommandPI, Command.CommandSIN, Command.CommandNULL};
        TestDriver.Test("0", "N/A", commands1, true, true);

        Command commands2[] = {Command.CommandRAD, Command.CommandPI, Command.CommandCOS, Command.CommandNULL};
        TestDriver.Test("-1", "N/A", commands2, true, true);

        Command commands3[] = {Command.CommandRAD, Command.CommandPI, Command.CommandTAN, Command.CommandNULL};
        TestDriver.Test("0", "N/A", commands3, true, true);

        Command commands4[] = {Command.CommandGRAD, Command.Command4, Command.Command0, Command.Command0, Command.CommandSIN, Command.CommandNULL};
        TestDriver.Test("0", "N/A", commands4, true, true);

        Command commands5[] = {Command.CommandGRAD, Command.Command4, Command.Command0, Command.Command0, Command.CommandCOS, Command.CommandNULL};
        TestDriver.Test("1", "N/A", commands5, true, true);

        Command commands6[] = {Command.CommandGRAD, Command.Command4, Command.Command0, Command.Command0, Command.CommandTAN, Command.CommandNULL};
        TestDriver.Test("0", "N/A", commands6, true, true);
    }

    @Test
    void CalculatorManagerTestModeChange() {
        Command commands1[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandNULL};
        TestDriver.Test("123", "", commands1, true, false);

        Command commands2[] = {Command.ModeScientific, Command.CommandNULL};
        TestDriver.Test("0", "", commands2, true, false);

        Command commands3[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandNULL};
        TestDriver.Test("123", "", commands3, true, false);

        Command commands4[] = {Command.ModeProgrammer, Command.CommandNULL};
        TestDriver.Test("0", "", commands4, true, false);

        Command commands5[] = {Command.Command1, Command.Command2, Command.Command3, Command.CommandNULL};
        TestDriver.Test("123", "", commands5, true, false);

        Command commands6[] = {Command.ModeScientific, Command.CommandNULL};
        TestDriver.Test("0", "", commands6, true, false);

        Command commands7[] = {Command.Command6, Command.Command7, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("67", "67 + ", commands7, true, false);

        Command commands8[] = {Command.ModeBasic, Command.CommandNULL};
        TestDriver.Test("0", "", commands8, true, false);
    }

    @Test
    void CalculatorManagerTestProgrammer() {
        Command commands1[] = {Command.ModeProgrammer, Command.Command5, Command.Command3, Command.CommandNand,
                Command.Command8, Command.Command3, Command.CommandAnd, Command.CommandNULL};
        TestDriver.Test("-18", "53 NAND 83 AND ", commands1, true, false);

        Command commands2[] = {Command.ModeProgrammer, Command.Command5, Command.Command3, Command.CommandNor,
                Command.Command8, Command.Command3, Command.CommandAnd, Command.CommandNULL};
        TestDriver.Test("-120", "53 NOR 83 AND ", commands2, true, false);

        Command commands3[] = {
                Command.ModeProgrammer, Command.Command5, Command.CommandLSHF, Command.Command1, Command.CommandAnd, Command.CommandNULL
        };
        TestDriver.Test("10", "5 Lsh 1 AND ", commands3, true, false);

        Command commands5[] = {
                Command.ModeProgrammer, Command.Command5, Command.CommandRSHFL, Command.Command1, Command.CommandAnd, Command.CommandNULL
        };
        TestDriver.Test("2", "5 Rsh 1 AND ", commands5, true, false);

        Command commands6[] = {Command.ModeProgrammer, Command.CommandBINPOS63, Command.CommandRSHF, Command.Command5,
                Command.Command6, Command.CommandAnd, Command.CommandNULL};
        TestDriver.Test("-128", "-9223372036854775808 Rsh 56 AND ", commands6, true, false);

        Command commands7[] = {Command.ModeProgrammer, Command.Command1, Command.CommandROL, Command.CommandNULL};
        TestDriver.Test("2", "RoL(1)", commands7, true, false);

        Command commands8[] = {Command.ModeProgrammer, Command.Command1, Command.CommandROR, Command.CommandNULL};
        TestDriver.Test("-9,223,372,036,854,775,808", "RoR(1)", commands8, true, false);

        Command commands9[] = {Command.ModeProgrammer, Command.Command1, Command.CommandRORC, Command.CommandNULL};
        TestDriver.Test("0", "RoR(1)", commands9, true, false);

        Command commands10[] = {Command.ModeProgrammer, Command.Command1, Command.CommandRORC, Command.CommandRORC, Command.CommandNULL};
        TestDriver.Test("-9,223,372,036,854,775,808", "RoR(RoR(1))", commands10, true, false);
    }

    void Cleanup() {
        m_calculatorManager.Reset();
        m_calculatorDisplayTester.reset();
    }

    @Test
    void CalculatorManagerTestMemory() {
        Command scientificCalculatorTest52[] = {Command.Command1, Command.CommandSTORE, Command.CommandNULL};
        String expectedPrimaryDisplayTestScientific52 = ("1");
        String expectedExpressionDisplayTestScientific52 = ("");

        Command scientificCalculatorTest53[] = {Command.Command1, Command.CommandNULL};
        String expectedPrimaryDisplayTestScientific53 = ("1");
        String expectedExpressionDisplayTestScientific53 = ("");

        CalculatorManagerDisplayTester pCalculatorDisplay = m_calculatorDisplayTester;
        String resultPrimary = "";
        String resultExpression = "";

        Cleanup();
        ExecuteCommands(scientificCalculatorTest52);
        resultPrimary = pCalculatorDisplay.GetPrimaryDisplay();
        resultExpression = pCalculatorDisplay.GetExpression();
        assertEquals(expectedPrimaryDisplayTestScientific52, resultPrimary);

        Cleanup();
        ExecuteCommands(scientificCalculatorTest53);
        m_calculatorManager.MemorizeNumber();
        m_calculatorManager.SendCommand(Command.CommandCLEAR);
        m_calculatorManager.MemorizedNumberLoad(0);
        resultPrimary = pCalculatorDisplay.GetPrimaryDisplay();
        resultExpression = pCalculatorDisplay.GetExpression();
        assertEquals(expectedPrimaryDisplayTestScientific52, resultPrimary);

        Cleanup();
        m_calculatorManager.SendCommand(Command.Command1);
        m_calculatorManager.MemorizeNumber();
        m_calculatorManager.SendCommand(Command.CommandCLEAR);
        m_calculatorManager.SendCommand(Command.Command2);
        m_calculatorManager.MemorizeNumber();
        m_calculatorManager.SendCommand(Command.CommandCLEAR);
        m_calculatorManager.MemorizedNumberLoad(1);
        resultPrimary = pCalculatorDisplay.GetPrimaryDisplay();
        assertEquals("1", resultPrimary);

        m_calculatorManager.MemorizedNumberLoad(0);
        resultPrimary = pCalculatorDisplay.GetPrimaryDisplay();
        assertEquals("2", resultPrimary);

        Cleanup();
        m_calculatorManager.SendCommand(Command.Command1);
        m_calculatorManager.SendCommand(Command.CommandSIGN);
        m_calculatorManager.MemorizeNumber();
        m_calculatorManager.SendCommand(Command.CommandADD);
        m_calculatorManager.SendCommand(Command.Command2);
        m_calculatorManager.SendCommand(Command.CommandEQU);
        m_calculatorManager.MemorizeNumber();
        m_calculatorManager.SendCommand(Command.CommandMUL);
        m_calculatorManager.SendCommand(Command.Command2);
        m_calculatorManager.MemorizeNumber();

        List<String> memorizedNumbers = pCalculatorDisplay.GetMemorizedNumbers();

        List<String> expectedMemorizedNumbers = new ArrayList<>();
        expectedMemorizedNumbers.add("2");
        expectedMemorizedNumbers.add("1");
        expectedMemorizedNumbers.add("-1");

        boolean isEqual = false;
        if (memorizedNumbers.size() < expectedMemorizedNumbers.size()) {
            isEqual = memorizedNumbers.subList(0, expectedMemorizedNumbers.size()).equals(expectedMemorizedNumbers);
        } else {
            isEqual = expectedMemorizedNumbers.subList(0, memorizedNumbers.size()).equals(memorizedNumbers);
        }
        assertTrue(isEqual);

        m_calculatorManager.SendCommand(Command.CommandCLEAR);
        m_calculatorManager.SendCommand(Command.Command2);
        m_calculatorManager.MemorizedNumberAdd(0);
        m_calculatorManager.MemorizedNumberAdd(1);
        m_calculatorManager.MemorizedNumberAdd(2);

        memorizedNumbers = pCalculatorDisplay.GetMemorizedNumbers();

        expectedMemorizedNumbers.clear();
        expectedMemorizedNumbers.add("4");
        expectedMemorizedNumbers.add("3");
        expectedMemorizedNumbers.add("1");

        if (memorizedNumbers.size() < expectedMemorizedNumbers.size()) {
            isEqual = memorizedNumbers.subList(0, expectedMemorizedNumbers.size()).equals(expectedMemorizedNumbers);
        } else {
            isEqual = expectedMemorizedNumbers.subList(0, memorizedNumbers.size()).equals(memorizedNumbers);
        }
        assertTrue(isEqual);

        m_calculatorManager.SendCommand(Command.CommandCLEAR);
        m_calculatorManager.SendCommand(Command.Command1);
        m_calculatorManager.SendCommand(Command.CommandPNT);
        m_calculatorManager.SendCommand(Command.Command5);

        m_calculatorManager.MemorizedNumberSubtract(0);
        m_calculatorManager.MemorizedNumberSubtract(1);
        m_calculatorManager.MemorizedNumberSubtract(2);

        memorizedNumbers = pCalculatorDisplay.GetMemorizedNumbers();

        expectedMemorizedNumbers.clear();
        expectedMemorizedNumbers.add("2.5");
        expectedMemorizedNumbers.add("1.5");
        expectedMemorizedNumbers.add("-0.5");

        if (memorizedNumbers.size() < expectedMemorizedNumbers.size()) {
            isEqual = memorizedNumbers.subList(0, expectedMemorizedNumbers.size()).equals(expectedMemorizedNumbers);
        } else {
            isEqual = expectedMemorizedNumbers.subList(0, memorizedNumbers.size()).equals(memorizedNumbers);
        }
        assertTrue(isEqual);

        // Memorizing 101 numbers, which exceeds the limit.
        Cleanup();
        for (int i = 0; i < 101; i++) {
            m_calculatorManager.SendCommand(Command.Command1);
            m_calculatorManager.MemorizeNumber();
        }

        memorizedNumbers = pCalculatorDisplay.GetMemorizedNumbers();
        assertEquals(100, memorizedNumbers.size());

        // Memorizing new number, which should show up at the top of the memory
        m_calculatorManager.SendCommand(Command.Command2);
        m_calculatorManager.MemorizeNumber();
        memorizedNumbers = pCalculatorDisplay.GetMemorizedNumbers();
        assertEquals("2", memorizedNumbers.get(0));

        // Test for trying to memorize invalid value
        m_calculatorManager.SendCommand(Command.Command2);
        m_calculatorManager.SendCommand(Command.CommandSIGN);
        m_calculatorManager.SendCommand(Command.CommandSQRT);
        m_calculatorManager.MemorizeNumber();
    }

    // Send 12345678910111213 and verify MaxDigitsReached
    @Test
    void CalculatorManagerTestMaxDigitsReached() {
        TestMaxDigitsReachedScenario("1,234,567,891,011,1213");
    }

    @Test
    void CalculatorManagerTestMaxDigitsReached_LeadingDecimal() {
        TestMaxDigitsReachedScenario("0.12345678910111213");
    }

    @Test
    void CalculatorManagerTestMaxDigitsReached_TrailingDecimal() {
        TestMaxDigitsReachedScenario("123,456,789,101,112.13");
    }

    @Test
    void CalculatorManagerTestBinaryOperatorReceived() {
        CalculatorManagerDisplayTester pCalculatorDisplay = m_calculatorDisplayTester;

        assertEquals(0, pCalculatorDisplay.GetBinaryOperatorReceivedCallCount());

        m_calculatorManager.SetStandardMode();
        ExecuteCommands(new Command[]{Command.Command1, Command.CommandADD});

        String display = pCalculatorDisplay.GetPrimaryDisplay();
        assertEquals("1", display);

        // Verify BinaryOperatorReceived
        assertEquals(1, pCalculatorDisplay.GetBinaryOperatorReceivedCallCount());
    }

    @Test
    void CalculatorManagerTestBinaryOperatorReceived_Multiple() {
        CalculatorManagerDisplayTester pCalculatorDisplay = m_calculatorDisplayTester;

        assertEquals(0, pCalculatorDisplay.GetBinaryOperatorReceivedCallCount());

        m_calculatorManager.SetStandardMode();
        ExecuteCommands(new Command[]{Command.Command1, Command.CommandADD, Command.CommandSUB, Command.CommandMUL});

        String display = pCalculatorDisplay.GetPrimaryDisplay();
        assertEquals("1", display);

        // Verify BinaryOperatorReceived
        assertEquals(3, pCalculatorDisplay.GetBinaryOperatorReceivedCallCount());
    }

    @Test
    void CalculatorManagerTestBinaryOperatorReceived_LongInput() {
        CalculatorManagerDisplayTester pCalculatorDisplay = m_calculatorDisplayTester;

        assertEquals(0, pCalculatorDisplay.GetBinaryOperatorReceivedCallCount());

        m_calculatorManager.SetStandardMode();
        ExecuteCommands(new Command[]{Command.Command1,
                Command.CommandADD,
                Command.Command2,
                Command.CommandMUL,
                Command.Command1,
                Command.Command0,
                Command.CommandSUB,
                Command.Command5,
                Command.CommandDIV,
                Command.Command5,
                Command.CommandEQU});

        String display = pCalculatorDisplay.GetPrimaryDisplay();
        assertEquals("5", display);

        // Verify BinaryOperatorReceived
        assertEquals(4, pCalculatorDisplay.GetBinaryOperatorReceivedCallCount());
    }

    @Test
    void CalculatorManagerTestStandardOrderOfOperations() {
        Command commands1[] = {Command.Command1, Command.CommandREC, Command.CommandNULL};
        TestDriver.Test("1", "1/(1)", commands1);

        Command commands2[] = {Command.Command4, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("2", "\u221A(4)", commands2);

        Command commands3[] = {Command.Command1, Command.CommandADD, Command.Command4, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("2", "1 + \u221A(4)", commands3);

        Command commands4[] = {Command.Command1, Command.CommandADD, Command.Command4, Command.CommandSQRT, Command.CommandSUB, Command.CommandNULL};
        TestDriver.Test("3", "3 - ", commands4);

        Command commands5[] = {Command.Command2, Command.CommandMUL, Command.Command4, Command.CommandREC, Command.CommandNULL};
        TestDriver.Test("0.25", "2 \u00D7 1/(4)", commands5);

        Command commands6[] = {Command.Command5, Command.CommandDIV, Command.Command6, Command.CommandPERCENT, Command.CommandNULL};
        TestDriver.Test("0.06", "5 \u00F7 0.06", commands6);

        Command commands7[] = {Command.Command4, Command.CommandSQRT, Command.CommandSUB, Command.CommandNULL};
        TestDriver.Test("2", "\u221A(4) - ", commands7);

        Command commands8[] = {Command.Command7, Command.CommandSQR, Command.CommandDIV, Command.CommandNULL};
        TestDriver.Test("49", "sqr(7) \u00F7 ", commands8);

        Command commands9[] = {Command.Command8, Command.CommandSQR, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("8", "\u221A(sqr(8))", commands9);

        Command commands10[] = {Command.Command1, Command.Command0, Command.CommandADD, Command.Command2, Command.CommandSUB, Command.CommandNULL};
        TestDriver.Test("12", "12 - ", commands10);

        Command commands11[] = {Command.Command3, Command.CommandMUL, Command.Command4, Command.CommandDIV, Command.CommandNULL};
        TestDriver.Test("12", "12 \u00F7 ", commands11);

        Command commands12[] = {Command.Command6, Command.CommandDIV, Command.Command3, Command.CommandSUB, Command.CommandADD, Command.CommandNULL};
        TestDriver.Test("2", "2 + ", commands12);

        Command commands13[] = {Command.Command7, Command.CommandSUB, Command.Command4, Command.CommandDIV, Command.CommandMUL, Command.CommandNULL};
        TestDriver.Test("3", "3 \u00D7 ", commands13);

        Command commands14[] = {Command.Command8, Command.CommandMUL, Command.Command2, Command.CommandADD, Command.CommandSQRT, Command.CommandNULL};
        TestDriver.Test("4", "16 + \u221A(16)", commands14);

        Command commands15[] = {Command.Command9, Command.CommandADD, Command.Command0, Command.CommandMUL, Command.CommandSIGN, Command.CommandNULL};
        TestDriver.Test("-9", "9 \u00D7 negate(9)", commands15);

        Command commands16[] = {Command.Command9, Command.CommandSIGN, Command.Command0, Command.CommandADD, Command.CommandMUL, Command.CommandNULL};
        TestDriver.Test("-90", "-90 \u00D7 ", commands16);

        Command commands17[] = {Command.Command1, Command.CommandADD, Command.Command2, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("3", "1 + 2=", commands17);

        Command commands18[] = {Command.Command2, Command.Command0, Command.CommandMUL, Command.Command0,
                Command.Command2, Command.CommandEQU, Command.CommandNULL};
        TestDriver.Test("40", "20 \u00D7 2=", commands18);

        Command commands19[] = {Command.Command1, Command.CommandADD, Command.Command2, Command.CommandADD, Command.CommandBACK, Command.CommandNULL};
        TestDriver.Test("3", "3 + ", commands19);

        Command commands20[] = {Command.Command1, Command.CommandADD, Command.Command2, Command.CommandADD, Command.CommandCLEAR, Command.CommandNULL};
        TestDriver.Test("0", "", commands20);

        Command commands21[] = {Command.Command1, Command.CommandADD, Command.Command2, Command.CommandADD, Command.CommandCENTR, Command.CommandNULL};
        TestDriver.Test("0", "3 + ", commands21);

        Command commands22[] = {Command.Command1, Command.CommandADD, Command.Command2, Command.CommandCLEAR, Command.CommandNULL};
        TestDriver.Test("0", "", commands22);

        Command commands23[] = {Command.Command1, Command.CommandADD, Command.Command2, Command.CommandCENTR, Command.CommandNULL};
        TestDriver.Test("0", "1 + ", commands23);

        Command commands24[] = {Command.Command1, Command.CommandMUL, Command.Command2, Command.CommandMUL, Command.Command3, Command.CommandMUL,
                Command.Command4, Command.CommandMUL, Command.Command5, Command.CommandMUL, Command.CommandNULL};
        TestDriver.Test("120", "120 \u00D7 ", commands24);
    }
}
