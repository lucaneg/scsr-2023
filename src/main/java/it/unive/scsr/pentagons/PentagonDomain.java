package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class PentagonDomain implements ValueDomain<PentagonDomain> {
    static final PentagonDomain BOTTOM = new PentagonDomain(PentagonType.BOTTOM);
    static final PentagonDomain TOP = new PentagonDomain(PentagonType.TOP);

    private final PentagonType type;
    private final Map<Identifier, PentagonElement> pentagons = new HashMap<>();

    public PentagonDomain(PentagonType type) {
        this.type = type;
    }

    public PentagonDomain() {
        this.type = PentagonType.GENERAL;
    }

    @Override
    public PentagonDomain lub(PentagonDomain other) throws SemanticException {
        return TOP;
    }

    @Override
    public boolean lessOrEqual(PentagonDomain other) throws SemanticException {
        throw new NotImplementedException();
    }

    @Override
    public PentagonDomain top() {
        return TOP;
    }

    @Override
    public PentagonDomain bottom() {
        return BOTTOM;
    }

    @Override
    public PentagonDomain assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public PentagonDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public PentagonDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public PentagonDomain forgetIdentifier(Identifier id) throws SemanticException {
        return this;
    }

    @Override
    public PentagonDomain forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return this;
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        throw new NotImplementedException();
    }

    @Override
    public PentagonDomain pushScope(ScopeToken token) throws SemanticException {
        return this;
    }

    @Override
    public PentagonDomain popScope(ScopeToken token) throws SemanticException {
        return this;
    }

    @Override
    public DomainRepresentation representation() {
        throw new NotImplementedException();
    }

    public enum PentagonType {
        BOTTOM,
        TOP,
        GENERAL
    }
}
