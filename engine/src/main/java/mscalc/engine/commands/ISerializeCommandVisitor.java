package mscalc.engine.commands;

public interface ISerializeCommandVisitor {
    void Visit(IOpndCommand opndCmd);
    void Visit(IUnaryCommand unaryCmd);
    void Visit(IBinaryCommand binaryCmd);
    void Visit(IParenthesisCommand paraCmd);
}
