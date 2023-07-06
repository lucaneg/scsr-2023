package main.java.it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import main.java.it.unive.scsr.StrictUpperBounds;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;


public class CustomPentagonsDomain implements ValueDomain<PentagonsDomain> {

    private ValueEnvironment<Interval> intervalEnvironment;
    private ValueEnvironment<StrictUpperBounds> strictUpperBoundsEnvironment;

    @Override
    public PentagonsDomain lub(PentagonsDomain pentagonsDomain) throws SemanticException {
        CustomPentagonsDomain other = (CustomPentagonsDomain) pentagonsDomain;
        ValueEnvironment<Interval> lubIntervalEnvironment = this.intervalEnvironment.lub(other.intervalEnvironment);
        ValueEnvironment<StrictUpperBounds> lubStrictUpperBoundsEnvironment = this.strictUpperBoundsEnvironment.lub(other.strictUpperBoundsEnvironment);
        return new CustomPentagonsDomain(lubIntervalEnvironment, lubStrictUpperBoundsEnvironment);
    }

    @Override
    public boolean lessOrEqual(PentagonsDomain pentagonsDomain) throws SemanticException {
        CustomPentagonsDomain other = (CustomPentagonsDomain) pentagonsDomain;
        boolean intervalLessOrEqual = this.intervalEnvironment.lessOrEqual(other.intervalEnvironment);
        boolean strictUpperBoundsLessOrEqual = this.strictUpperBoundsEnvironment.lessOrEqual(other.strictUpperBoundsEnvironment);
        return intervalLessOrEqual && strictUpperBoundsLessOrEqual;
    }

    @Override
    public PentagonsDomain top() {
        ValueEnvironment<Interval> topIntervalEnvironment = new ValueEnvironment<>();
        ValueEnvironment<StrictUpperBounds> topStrictUpperBoundsEnvironment = new ValueEnvironment<>();
        return new CustomPentagonsDomain(topIntervalEnvironment, topStrictUpperBoundsEnvironment);
    }

    @Override
    public PentagonsDomain bottom() {
        ValueEnvironment<Interval> bottomIntervalEnvironment = new ValueEnvironment<>();
        ValueEnvironment<StrictUpperBounds> bottomStrictUpperBoundsEnvironment = new ValueEnvironment<>();
        return new CustomPentagonsDomain(bottomIntervalEnvironment, bottomStrictUpperBoundsEnvironment);
    }

    @Override
    public PentagonsDomain assign(Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Interval intervalValue = intervalEnvironment.eval(valueExpression, programPoint);
        intervalEnvironment.assign(identifier, intervalValue);
        return this;
    }

    @Override
    public PentagonsDomain smallStepSemantics(ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Interval intervalValue = intervalEnvironment.eval(valueExpression, programPoint);
        return new CustomPentagonsDomain(intervalEnvironment, strictUpperBoundsEnvironment);
    }

    @Override
    public PentagonsDomain assume(ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Interval intervalValue = intervalEnvironment.eval(valueExpression, programPoint);
        intervalEnvironment.assume(valueExpression, intervalValue);
        return this;
    }

    @Override
    public PentagonsDomain forgetIdentifier(Identifier identifier) throws SemanticException {
        intervalEnvironment.forget(identifier);
        strictUpperBoundsEnvironment.forget(identifier);
        return this;
    }

    @Override
    public PentagonsDomain forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        intervalEnvironment.forgetIf(test);
        strictUpperBoundsEnvironment.forgetIf(test);
        return this;
    }

    @Override
    public Satisfiability satisfies(ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Interval intervalValue = intervalEnvironment.eval(valueExpression, programPoint);
        if (intervalValue.isBottom())
            return Satisfiability.UNSATISFIED;
        if (intervalValue.isTop())
            return Satisfiability.UNKNOWN;
        return Satisfiability.SATISFIED;
    }

    @Override
    public PentagonsDomain pushScope(ScopeToken scopeToken) throws SemanticException {
        intervalEnvironment.pushScope(scopeToken);
        strictUpperBoundsEnvironment.pushScope(scopeToken);
        return this;
    }

    @Override
    public PentagonsDomain popScope(ScopeToken scopeToken) throws SemanticException {
        intervalEnvironment.popScope(scopeToken);
        strictUpperBoundsEnvironment.popScope(scopeToken);
        return this;
    }

    @Override
    public DomainRepresentation representation() {
        return new CustomPentagonsDomainRepresentation(intervalEnvironment, strictUpperBoundsEnvironment);
    }

    private CustomPentagonsDomain(ValueEnvironment<Interval> intervalEnvironment, ValueEnvironment<StrictUpperBounds> strictUpperBoundsEnvironment) {
        this.intervalEnvironment = intervalEnvironment;
        this.strictUpperBoundsEnvironment = strictUpperBoundsEnvironment;
    }

    private static class CustomPentagonsDomainRepresentation implements DomainRepresentation {
        private final ValueEnvironment<Interval> intervalEnvironment;
        private final ValueEnvironment<StrictUpperBounds> strictUpperBoundsEnvironment;

        public CustomPentagonsDomainRepresentation(ValueEnvironment<Interval> intervalEnvironment, ValueEnvironment<StrictUpperBounds> strictUpperBoundsEnvironment) {
            this.intervalEnvironment = intervalEnvironment;
            this.strictUpperBoundsEnvironment = strictUpperBoundsEnvironment;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Interval Environment:\n");
            builder.append(intervalEnvironment.toString());
            builder.append("\nStrict Upper Bounds Environment:\n");
            builder.append(strictUpperBoundsEnvironment.toString());
            return builder.toString();
        }
    }
}
