package it.unive.scsr;

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


public class PentagonsDomain implements ValueDomain<PentagonsDomain> {

    // Implements PentagonsDomain constructor
    ValueEnvironment<Interval> interval;
    ValueEnvironment<StrictUpperBounds> strictUpperBounds;

    /* Lattice Operations*/

    @Override
    public PentagonsDomain lub(PentagonsDomain other) throws SemanticException {
        return null;
    }

    @Override
    public boolean lessOrEqual(PentagonsDomain other) throws SemanticException {
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

    // Assegna ad un identificativo il valore di un espressione. Ad esempio l'assegnamento di una variabile

    @Override
    public PentagonsDomain assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain forgetIdentifier(Identifier id) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return null;
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain pushScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public PentagonsDomain popScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }
}