package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.function.Predicate;

public class PentagonDomain<T extends ValueDomain<T>> implements ValueDomain<PentagonDomain<T>> {

    private StrictUpperBound sub;


    @Override
    public PentagonDomain<T> lub(PentagonDomain<T> other) throws SemanticException {
        return null;
    }

    @Override
    public boolean lessOrEqual(PentagonDomain<T> other) throws SemanticException {
        return false;
    }

    @Override
    public PentagonDomain<T> top() {
        return null;
    }

    @Override
    public PentagonDomain<T> bottom() {
        return null;
    }

    @Override
    public PentagonDomain<T> assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain<T> smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain<T> assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain<T> forgetIdentifier(Identifier id) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain<T> forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return null;
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain<T> pushScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public PentagonDomain<T> popScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }
}
