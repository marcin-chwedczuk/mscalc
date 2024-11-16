package mscalc.engine.commands;

public interface ISerializeCommandVisitor {
    void visit(IOpndCommand opndCmd);
    void visit(IUnaryCommand unaryCmd);
    void visit(IBinaryCommand binaryCmd);
    void visit(IParenthesisCommand paraCmd);
}
