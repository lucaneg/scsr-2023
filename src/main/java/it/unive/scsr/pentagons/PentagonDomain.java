package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.unary.LogicalNegation;
import it.unive.lisa.util.numeric.MathNumber;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Predicate;


/**
 * This class implements the pentagons domain according to the abstract "Pentagons: A weakly relational abstract domain for the efficient validation of array accesses"
 * <a href="https://www.sciencedirect.com/science/article/pii/S0167642309000719">visit the paper's page</a>
 *
 * @author Finesso Davide 881825, Musone Mattia 877962, Porcu Davide 874311
 * @version 1.0.0
 */
public class PentagonDomain implements ValueDomain<PentagonDomain> {

    /**
     * Representation of the StrictUpperBound
     */
    private StrictUpperBound strictUpperBound;
    /**
     * Representation of the interval
     */
    private ValueEnvironment<Interval> interval;

    public PentagonDomain() {
        this(new ValueEnvironment<>(new Interval(), new HashMap<>(), new Interval()), new StrictUpperBound());
    }


    public PentagonDomain(ValueEnvironment<Interval> interval, StrictUpperBound strictUpperBound) {
        this.interval = interval;
        this.strictUpperBound = strictUpperBound;
    }

    @Override
    public PentagonDomain top() {
        return new PentagonDomain(interval.top(), strictUpperBound.top());
    }

    @Override
    public PentagonDomain bottom() {
        return new PentagonDomain(interval.bottom(), strictUpperBound.bottom());
    }

    @Override
    public boolean isTop() {
        return strictUpperBound.isTop() && interval.isTop();
    }

    @Override
    public boolean isBottom() {
        return (strictUpperBound.function != null && strictUpperBound.function.toString().contains(Lattice.BOTTOM_STRING)) || interval.isBottom();
    }

    /**
     * Implementation of second condition in Order operation of Pentagon.
     */
    public boolean checkOrder(PentagonDomain other) {
        for (Identifier x : other.strictUpperBound.getKeys()) {
            for (Identifier y : other.strictUpperBound.getState(x)) {
                if (!(this.strictUpperBound.getState(x).contains(y) ||
                        this.interval.getState(x).interval.getHigh().compareTo(this.interval.getState(y).interval.getLow()) < 0)) {
                    return false;
                }

            }
        }
        return true;
    }

    @Override
    public boolean lessOrEqual(PentagonDomain other) throws SemanticException {
        return this.interval.lessOrEqual(other.interval) && checkOrder(other);
    }

    @Override
    public PentagonDomain lub(PentagonDomain other) throws SemanticException {
        //return new Pentagons(this.interval.lub(other.interval), this.strictUpperBound.lub(other.strictUpperBound));
        if (other == null || other.isBottom() || this.isTop() || this == other || this.equals(other))
            return this;

        if (this.isBottom() || other.isTop())
            return other;
        ValueEnvironment<Interval> resultInterval = this.interval.lub(other.interval);
        StrictUpperBound resultSUB;

        StrictUpperBound sub1;
        StrictUpperBound sub2 = new StrictUpperBound();
        StrictUpperBound sub3 = new StrictUpperBound();

        sub1 = this.strictUpperBound.lub(other.strictUpperBound);

        for (Identifier x : this.strictUpperBound.getKeys()) {
            HashSet<Identifier> result = new HashSet<>();
            UpperBoundSet upperBoundSet = this.strictUpperBound.getState(x);
            for (Identifier y : upperBoundSet) {
                if (!(this.strictUpperBound.getState(x).contains(y) ||
                        other.interval.getState(x).interval.getHigh().compareTo(this.interval.getState(y).interval.getLow()) < 0)) {
                    result.add(y);
                }
            }
            sub2 = sub2.putState(x, new UpperBoundSet(result));
        }

        for (Identifier x : this.strictUpperBound.getKeys()) {
            HashSet<Identifier> result = new HashSet<>();
            UpperBoundSet upperBoundSet = other.strictUpperBound.getState(x);
            for (Identifier y : upperBoundSet) {
                if (!(this.strictUpperBound.getState(x).contains(y) ||
                        this.interval.getState(x).interval.getHigh().compareTo(other.interval.getState(y).interval.getLow()) < 0)) {
                    result.add(y);
                }
            }
            sub3 = sub3.putState(x, new UpperBoundSet(result));
        }

        // union
        resultSUB = sub1.lub(sub2).lub(sub3);

        return new PentagonDomain(resultInterval, resultSUB);
    }

    @Override
    public PentagonDomain widening(PentagonDomain other) throws SemanticException {
        return new PentagonDomain(this.interval.widening(other.interval), this.strictUpperBound.widening(other.strictUpperBound));
    }


    private PentagonDomain refinePentagon() throws SemanticException {
        Map<Identifier, Interval> newMap = new HashMap<>();
        for (Identifier sx : strictUpperBound.getKeys()) {
            if (strictUpperBound.getState(sx).isBottom() || strictUpperBound.getState(sx).isTop()) {
                newMap.put(sx, interval.getState(sx));
            } else {
                // for each sub of identifier sx in the map
                for (Identifier dx : strictUpperBound.getState(sx)) {
                    // if interval of sx and dx are not TOP or BOTTOM
                    if (!interval.getState(sx).isBottom() &&
                            !interval.getState(sx).isTop() &&
                            !interval.getState(dx).isBottom() &&
                            !interval.getState(dx).isTop()) {
                        // get the bounds of interval of both identifiers "sx" and "dx"
                        MathNumber sxLow = interval.getState(sx).interval.getLow();
                        MathNumber sxHigh = interval.getState(sx).interval.getHigh();
                        MathNumber dxLow = interval.getState(dx).interval.getLow();
                        MathNumber dxHigh = interval.getState(dx).interval.getHigh();


                        // sx < dx, sx --> [s_low, s_high], dx --> [d_low, d_high]
                        if (sxLow.compareTo(dxLow) >= 0) { // sxLow >= dxLow
                            newMap.put(sx, new Interval());
                        } else if (sxHigh.compareTo(dxLow) >= 0) { //sxLow < dxLow AND sxHigh >= dxLow
                            newMap.put(sx, new Interval(sxLow, dxLow.subtract(new MathNumber(1))));
                        } else {
                            newMap.put(sx, interval.getState(sx));
                        }
                    } else {
                        newMap.put(sx, interval.getState(sx));
                    }
                }
            }
        }
        PentagonDomain newPentagon = new PentagonDomain();
        newPentagon.interval = interval.mk(new Interval(), newMap);
        newPentagon.strictUpperBound = strictUpperBound;
        return newPentagon;
    }

    @Override
    public PentagonDomain assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        ValueEnvironment<Interval> intervals = interval.assign(id, expression, pp);
        StrictUpperBound strictUpperBounds = strictUpperBound.assign(id, expression, pp);
        PentagonDomain p = new PentagonDomain(intervals, strictUpperBounds).refinePentagon();
        return p;
    }

    @Override
    public PentagonDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            if (unaryExpression.getOperator() == LogicalNegation.INSTANCE) {
                expression = expression.removeNegations();
            }
        }

        ValueEnvironment<Interval> intervals = interval.assume(expression, pp);
        StrictUpperBound strictUpperBounds = strictUpperBound.assume(expression, pp);

        // create copy of intervals
        Map<Identifier, Interval> cp = intervals.mkNewFunction(intervals.getMap(), false);
        ValueEnvironment<Interval> copy = intervals.mk(intervals.lattice, cp);
        // create copy of strictupperbound
        StrictUpperBound copy2 = new StrictUpperBound(strictUpperBounds.lattice, strictUpperBounds.getMap());
        PentagonDomain p = new PentagonDomain(copy, copy2).refinePentagon();
        return p;
    }


    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(interval.representation() + ", {" + strictUpperBound.representation() + "} ");
    }

    @Override
    public PentagonDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return new PentagonDomain(this.interval, this.strictUpperBound);
    }


    @Override
    public PentagonDomain forgetIdentifier(Identifier id) throws SemanticException {
        return new PentagonDomain(interval.forgetIdentifier(id), strictUpperBound.forgetIdentifier(id));
    }

    @Override
    public PentagonDomain forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return new PentagonDomain(interval.forgetIdentifiersIf(test), strictUpperBound.forgetIdentifiersIf(test));
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return interval.satisfies(expression, pp).and(strictUpperBound.satisfies(expression, pp));
    }

    @Override
    public PentagonDomain pushScope(ScopeToken token) throws SemanticException {
        return new PentagonDomain(interval.pushScope(token), strictUpperBound.pushScope(token));
    }

    @Override
    public PentagonDomain popScope(ScopeToken token) throws SemanticException {
        return new PentagonDomain(interval.popScope(token), strictUpperBound.popScope(token));
    }

}
