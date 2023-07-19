package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.function.Predicate;

public class PentagonDomain implements ValueDomain<PentagonDomain> {
    @Override
    public PentagonDomain lub(PentagonDomain other) throws SemanticException {
        return null;
    }

    @Override
    public boolean lessOrEqual(PentagonDomain other) throws SemanticException {
        return false;
    }

    @Override
    public PentagonDomain top() {
        return null;
    }

    @Override
    public PentagonDomain bottom() {
        return null;
    }

    @Override
    public PentagonDomain assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain forgetIdentifier(Identifier id) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return null;
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain pushScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain popScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }
}
