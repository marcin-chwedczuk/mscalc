package mscalc.engine;

import mscalc.engine.commands.Command;
import mscalc.engine.commands.IExpressionCommand;
import mscalc.engine.resource.ResourceProvider;

import java.lang.classfile.Opcode;
import java.util.ArrayList;
import java.util.List;

import static mscalc.engine.Commands.*;
import static mscalc.engine.ratpack.CalcErr.*;

public class CalculatorManager implements CalcDisplay {
    private static final int m_maximumMemorySize = 100;
    private static final int MAX_HISTORY_ITEMS = 20;

    private CalcDisplay m_displayCallback;
    private CCalcEngine m_currentCalculatorEngine;
    private CCalcEngine m_scientificCalculatorEngine;
    private CCalcEngine m_standardCalculatorEngine;
    private CCalcEngine m_programmerCalculatorEngine;
    private ResourceProvider m_resourceProvider;
    private boolean m_inHistoryItemLoadMode;

    private List<Rational> m_memorizedNumbers = new ArrayList<>();
    private Rational m_persistedPrimaryValue = Rational.of(0);
    private boolean m_isExponentialFormat;
    private Command m_currentDegreeMode;

    private CalculatorHistory m_pStdHistory;
    private CalculatorHistory m_pSciHistory;
    private CalculatorHistory m_pHistory;

    public CalculatorManager(CalcDisplay displayCallback, ResourceProvider resourceProvider) {
                m_displayCallback = displayCallback;
                 m_currentCalculatorEngine = null;
                 m_resourceProvider = resourceProvider;
                 m_inHistoryItemLoadMode = false;
                 m_persistedPrimaryValue = new Rational();
                 m_isExponentialFormat = false;
                 m_currentDegreeMode = Command.CommandNULL;
                 m_pStdHistory = new CalculatorHistory(MAX_HISTORY_ITEMS);
                 m_pSciHistory = new CalculatorHistory(MAX_HISTORY_ITEMS);
                 m_pHistory = null;

        CCalcEngine.InitialOneTimeOnlySetup(m_resourceProvider);
    }

    /// <summary>
    /// Call the callback function using passed in IDisplayHelper.
    /// Used to set the primary display value on ViewModel
    /// </summary>
    /// <param name="text">wstring representing text to be displayed</param>
    void SetPrimaryDisplay(String displayString, boolean isError)
    {
        if (!m_inHistoryItemLoadMode)
        {
            m_displayCallback.setPrimaryDisplay(displayString, isError);
        }
    }

    void SetIsInError(boolean isError)
    {
        m_displayCallback.setIsInError(isError);
    }

    void DisplayPasteError()
    {
        m_currentCalculatorEngine.DisplayError(CALC_E_DOMAIN /*code for "Invalid input" error*/);
    }

    void MaxDigitsReached()
    {
        m_displayCallback.maxDigitsReached();
    }

    void BinaryOperatorReceived()
    {
        m_displayCallback.binaryOperatorReceived();
    }

    void MemoryItemChanged(int indexOfMemory)
    {
        m_displayCallback.memoryItemChanged(indexOfMemory);
    }

    void InputChanged()
    {
        m_displayCallback.inputChanged();
    }

    /// <summary>
    /// Call the callback function using passed in IDisplayHelper.
    /// Used to set the expression display value on ViewModel
    /// </summary>
    /// <param name="expressionString">wstring representing expression to be displayed</param>
    void SetExpressionDisplay(
            List<Pair<String, Integer>>  tokens,
            List<IExpressionCommand> commands)
    {
        if (!m_inHistoryItemLoadMode)
        {
            m_displayCallback.setExpressionDisplay(tokens, commands);
        }
    }

    /// <summary>
    /// Callback from the CalculatorControl
    /// Passed in string representations of memorized numbers get passed to the client
    /// </summary>
    /// <param name="memorizedNumber">vector containing wstring values of memorized numbers</param>
    void SetMemorizedNumbers(List<String> memorizedNumbers)
    {
        m_displayCallback.setMemorizedNumbers(memorizedNumbers);
    }

    /// <summary>
    /// Callback from the engine
    /// </summary>
    /// <param name="parenthesisCount">string containing the parenthesis count</param>
    void SetParenthesisNumber(int parenthesisCount)
    {
        m_displayCallback.setParenthesisNumber(parenthesisCount);
    }

    /// <summary>
    /// Callback from the engine
    /// </summary>
    void OnNoRightParenAdded()
    {
        m_displayCallback.onNoRightParenAdded();
    }

    /// <summary>
    /// Reset CalculatorManager.
    /// Set the mode to the standard calculator
    /// Set the degree mode as regular degree (as oppose to Rad or Grad)
    /// Clear all the entries and memories
    /// Clear Memory if clearMemory parameter is true.(Default value is true)
    /// </summary>
    void Reset(boolean clearMemory /* = true*/)
    {
        SetStandardMode();

        if (m_scientificCalculatorEngine != null)
        {
            m_scientificCalculatorEngine.ProcessCommand(IDC_DEG);
            m_scientificCalculatorEngine.ProcessCommand(IDC_CLEAR);

            if (m_isExponentialFormat)
            {
                m_isExponentialFormat = false;
                m_scientificCalculatorEngine.ProcessCommand(IDC_FE);
            }
        }
        if (m_programmerCalculatorEngine != null)
        {
            m_programmerCalculatorEngine.ProcessCommand(IDC_CLEAR);
        }

        if (clearMemory)
        {
            this.MemorizedNumberClearAll();
        }
    }

    /// <summary>
    /// Change the current calculator engine to standard calculator engine.
    /// </summary>
    void SetStandardMode()
    {
        if (m_standardCalculatorEngine == null)
        {
            m_standardCalculatorEngine =
                    new CCalcEngine(false /* Respect Order of Operations */, false /* Set to Integer Mode */, m_resourceProvider, this, m_pStdHistory);
        }

        m_currentCalculatorEngine = m_standardCalculatorEngine;
        m_currentCalculatorEngine.ProcessCommand(IDC_DEC);
        m_currentCalculatorEngine.ProcessCommand(IDC_CLEAR);
        m_currentCalculatorEngine.ChangePrecision(CalculatorPrecision.StandardModePrecision.toInt());
        UpdateMaxIntDigits();
        m_pHistory = m_pStdHistory;
    }

    /// <summary>
    /// Change the current calculator engine to scientific calculator engine.
    /// </summary>
    void SetScientificMode()
    {
        if (m_scientificCalculatorEngine == null)
        {
            m_scientificCalculatorEngine =
                    new CCalcEngine(true /* Respect Order of Operations */, false /* Set to Integer Mode */, m_resourceProvider, this, m_pSciHistory);
        }

        m_currentCalculatorEngine = m_scientificCalculatorEngine;
        m_currentCalculatorEngine.ProcessCommand(IDC_DEC);
        m_currentCalculatorEngine.ProcessCommand(IDC_CLEAR);
        m_currentCalculatorEngine.ChangePrecision(CalculatorPrecision.ScientificModePrecision.toInt());
        m_pHistory = m_pSciHistory;
    }

    /// <summary>
    /// Change the current calculator engine to scientific calculator engine.
    /// </summary>
    void SetProgrammerMode()
    {
        if (m_programmerCalculatorEngine == null)
        {
            m_programmerCalculatorEngine =
                    new CCalcEngine(true /* Respect Order of Operations */, true /* Set to Integer Mode */, m_resourceProvider, this, null);
        }

        m_currentCalculatorEngine = m_programmerCalculatorEngine;
        m_currentCalculatorEngine.ProcessCommand(IDC_DEC);
        m_currentCalculatorEngine.ProcessCommand(IDC_CLEAR);
        m_currentCalculatorEngine.ChangePrecision(CalculatorPrecision.ProgrammerModePrecision.toInt());
    }

    /// <summary>
    /// Send command to the Calc Engine
    /// Cast Command Enum to OpCode.
    /// Handle special commands such as mode change and combination of two commands.
    /// </summary>
    /// <param name="command">Enum Command</command>
    void endCommand(Command command)
    {
        // When the expression line is cleared, we save the current state, which includes,
        // primary display, memory, and degree mode
        if (command == Command.CommandCLEAR || command == Command.CommandEQU || command == Command.ModeBasic || command == Command.ModeScientific
                || command == Command.ModeProgrammer)
        {
            switch (command)
            {
                case Command.ModeBasic:
                    this.SetStandardMode();
                    break;
                case Command.ModeScientific:
                    this.SetScientificMode();
                    break;
                case Command.ModeProgrammer:
                    this.SetProgrammerMode();
                    break;
                default:
                    m_currentCalculatorEngine.ProcessCommand(command.toInt());
            }

            InputChanged();
            return;
        }

        if (command == Command.CommandDEG || command == Command.CommandRAD || command == Command.CommandGRAD)
        {
            m_currentDegreeMode = command;
        }

        switch (command)
        {
            case Command.CommandASIN:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandSIN.toInt()));
                break;
            case Command.CommandACOS:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandCOS.toInt()));
                break;
            case Command.CommandATAN:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandTAN.toInt()));
                break;
            case Command.CommandPOWE:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandLN.toInt()));
                break;
            case Command.CommandASINH:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandSINH.toInt()));
                break;
            case Command.CommandACOSH:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandCOSH.toInt()));
                break;
            case Command.CommandATANH:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandTANH.toInt()));
                break;
            case Command.CommandASEC:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandSEC.toInt()));
                break;
            case Command.CommandACSC:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandCSC.toInt()));
                break;
            case Command.CommandACOT:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandCOT).toInt());
                break;
            case Command.CommandASECH:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandSECH.toInt()));
                break;
            case Command.CommandACSCH:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandCSCH.toInt()));
                break;
            case Command.CommandACOTH:
                m_currentCalculatorEngine.ProcessCommand((Command.CommandINV.toInt()));
                m_currentCalculatorEngine.ProcessCommand((Command.CommandCOTH).toInt());
                break;
            case Command.CommandFE:
                m_isExponentialFormat = !m_isExponentialFormat;
            // [[fallthrough]];
            default:
                m_currentCalculatorEngine.ProcessCommand((command).toInt());
                break;
        }

        InputChanged();
    }

    /// <summary>
    /// Load the persisted value that is saved in memory of CalcEngine
    /// </summary>
    void LoadPersistedPrimaryValue()
    {
        m_currentCalculatorEngine.PersistedMemObject(m_persistedPrimaryValue);
        m_currentCalculatorEngine.ProcessCommand(IDC_RECALL);
        InputChanged();
    }

    /// <summary>
    /// Memorize the current displayed value
    /// Notify the client with new the new memorize value vector
    /// </summary>
    void MemorizeNumber()
    {
        if (m_currentCalculatorEngine.FInErrorState())
        {
            return;
        }

        m_currentCalculatorEngine.ProcessCommand(IDC_STORE);

        var memoryObjectPtr = m_currentCalculatorEngine.PersistedMemObject();
        if (memoryObjectPtr != null)
        {
            m_memorizedNumbers.addFirst(memoryObjectPtr);
        }

        while (m_memorizedNumbers.size() > m_maximumMemorySize)
        {
            m_memorizedNumbers.removeLast();
        }
        this.SetMemorizedNumbersString();
    }

    /// <summary>
    /// Recall the memorized number.
    /// The memorized number gets loaded to the primary display
    /// </summary>
    /// <param name="indexOfMemory">Index of the target memory</param>
    void MemorizedNumberLoad(int indexOfMemory)
    {
        if (m_currentCalculatorEngine.FInErrorState())
        {
            return;
        }

        this.MemorizedNumberSelect(indexOfMemory);
        m_currentCalculatorEngine.ProcessCommand(IDC_RECALL);
        InputChanged();
    }

    /// <summary>
    /// Do the addition to the selected memory
    /// It adds primary display value to the selected memory
    /// Notify the client with new the new memorize value vector
    /// </summary>
    /// <param name="indexOfMemory">Index of the target memory</param>
    void MemorizedNumberAdd(int indexOfMemory)
    {
        if (m_currentCalculatorEngine.FInErrorState())
        {
            return;
        }

        if (m_memorizedNumbers.isEmpty())
        {
            this.MemorizeNumber();
        }
        else
        {
            this.MemorizedNumberSelect(indexOfMemory);
            m_currentCalculatorEngine.ProcessCommand(IDC_MPLUS);

            this.MemorizedNumberChanged(indexOfMemory);

            this.SetMemorizedNumbersString();
        }

        m_displayCallback.MemoryItemChanged(indexOfMemory);
    }

    void MemorizedNumberClear(int indexOfMemory)
    {
        if (indexOfMemory < m_memorizedNumbers.size())
        {
            m_memorizedNumbers.remove(indexOfMemory);
        }
    }

    /// <summary>
    /// Do the subtraction to the selected memory
    /// It adds primary display value to the selected memory
    /// Notify the client with new the new memorize value vector
    /// </summary>
    /// <param name="indexOfMemory">Index of the target memory</param>
    void MemorizedNumberSubtract(int indexOfMemory)
    {
        if (m_currentCalculatorEngine.FInErrorState())
        {
            return;
        }

        // To add negative of the number on display to the memory -x = x - 2x
        if (m_memorizedNumbers.isEmpty())
        {
            this.MemorizeNumber();
            this.MemorizedNumberSubtract(0);
            this.MemorizedNumberSubtract(0);
        }
        else
        {
            this.MemorizedNumberSelect(indexOfMemory);
            m_currentCalculatorEngine.ProcessCommand(IDC_MMINUS);

            this.MemorizedNumberChanged(indexOfMemory);

            this.SetMemorizedNumbersString();
        }

        m_displayCallback.MemoryItemChanged(indexOfMemory);
    }

    /// <summary>
    /// Clear all the memorized values
    /// Notify the client with new the new memorize value vector
    /// </summary>
    void MemorizedNumberClearAll()
    {
        m_memorizedNumbers.clear();

        m_currentCalculatorEngine.ProcessCommand(IDC_MCLEAR);
        this.SetMemorizedNumbersString();
    }

    /// <summary>
    /// Helper function that selects a memory from the vector and set it to CCalcEngine
    /// Saved RAT number needs to be copied and passed in, as CCalcEngine destroyed the passed in RAT
    /// </summary>
    /// <param name="indexOfMemory">Index of the target memory</param>
    void MemorizedNumberSelect(int indexOfMemory)
    {
        if (m_currentCalculatorEngine.FInErrorState())
        {
            return;
        }

        var memoryObject = m_memorizedNumbers.get(indexOfMemory);
        m_currentCalculatorEngine.PersistedMemObject(memoryObject);
    }

    /// <summary>
    /// Helper function that needs to be executed when memory is modified
    /// When memory is modified, destroy the old RAT and put the new RAT in vector
    /// </summary>
    /// <param name="indexOfMemory">Index of the target memory</param>
    void MemorizedNumberChanged(int indexOfMemory)
    {
        if (m_currentCalculatorEngine.FInErrorState())
        {
            return;
        }

        var memoryObject = m_currentCalculatorEngine.PersistedMemObject();
        if (memoryObject != null)
        {
            m_memorizedNumbers.set(indexOfMemory, memoryObject);
        }
    }


}
