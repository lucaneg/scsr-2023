package main.java.it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.function.Predicate;


public class CustomPentagonsDomain implements ValueDomain<PentagonsDomain> {

    ValueEnvironment<Interval> intervalEnvironment;
    ValueEnvironment<StrictUpperBounds> strictUpperBoundsEnvironment;



    @Override
    public PentagonsDomain lub(PentagonsDomain pentagonsDomain) throws SemanticException {
        return null;
    }

    @Override
    public boolean lessOrEqual(PentagonsDomain pentagonsDomain) throws SemanticException {
        return false;
    }

    @Override
    public PentagonsDomain top() {
        return null;
    }

    @Override
    public PentagonsDomain bottom() {
        return null;
    }


    @Override
    public PentagonsDomain assign(Identifier identifier, ValueExpression valuExpression, ProgramPoint programPoint) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain smallStepSemantics(ValueExpression valuExpression, ProgramPoint programPoint) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain assume(ValueExpression valuExpression, ProgramPoint programPoint) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain forgetIdentifier(Identifier identifier) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return null;
    }

    @Override
    public Satisfiability satisfies(ValueExpression valuExpression, ProgramPoint programPoint ) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain pushScope(ScopeToken scopeToken) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain popScope(ScopeToken scopeToken) throws SemanticException {
        return null;
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }
}
