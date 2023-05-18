package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.function.Predicate;

@Deprecated
public class StrictUpperBound<T extends ValueDomain<T>> implements ValueDomain<StrictUpperBound<T>> {
    @Override
    public StrictUpperBound<T> lub(StrictUpperBound<T> other) throws SemanticException {
        return null;
    }

    @Override
    public boolean lessOrEqual(StrictUpperBound<T> other) throws SemanticException {
        return false;
    }

    @Override
    public StrictUpperBound<T> top() {
        return null;
    }

    @Override
    public StrictUpperBound<T> bottom() {
        return null;
    }

    @Override
    public StrictUpperBound<T> assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public StrictUpperBound<T> smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public StrictUpperBound<T> assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public StrictUpperBound<T> forgetIdentifier(Identifier id) throws SemanticException {
        return null;
    }

    @Override
    public StrictUpperBound<T> forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return null;
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public StrictUpperBound<T> pushScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public StrictUpperBound<T> popScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }
}
