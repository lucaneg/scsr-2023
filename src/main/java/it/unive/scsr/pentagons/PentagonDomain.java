package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.LogicalNegation;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class PentagonDomain implements ValueDomain<PentagonDomain> {

    private ValueEnvironment<Interval> interval;

    private Map<Identifier, Set<Identifier>> sub;

    public PentagonDomain() {
        this(new ValueEnvironment<>(new Interval(), new HashMap<>(), new Interval()), new HashMap<>());
    }

    public PentagonDomain(ValueEnvironment<Interval> interval, Map<Identifier, Set<Identifier>> sub) {
        this.interval = interval;
        this.sub = sub;
    }

    private PentagonDomain recomputePentagon() {
        Map<Identifier, Set<Identifier>> newSub = new HashMap<>();
        interval.getKeys().forEach(id1 -> {
            Set<Identifier> aux = new HashSet<>();
            interval.getKeys().forEach(id2 -> {
                if (!id1.equals(id2) && interval.getState(id1).interval.getHigh().compareTo(interval.getState(id2).interval.getLow()) < 0) {
                    aux.add(id2);
                }
            });
            newSub.put(id1, aux);
        });

        return new PentagonDomain(this.interval, newSub);
    }
    @Override
    public PentagonDomain lub(PentagonDomain other) throws SemanticException {
        ValueEnvironment<Interval> lubInterval = this.interval.lub(other.interval);
        Map<Identifier, Set<Identifier>> lubSub = new HashMap<>();

        Set<Identifier> variables = new HashSet<>();
        variables.addAll(this.sub.keySet());
        variables.addAll(other.sub.keySet());

        variables.forEach(var -> {
            Set<Identifier> aux = new HashSet<>();
            if (!this.sub.containsKey(var)) {
                this.sub.get(var).forEach( id -> {
                    if (this.interval.getState(var).interval.getHigh().compareTo(this.interval.getState(id).interval.getLow()) < 0) {
                        aux.add(id);
                    }
                });
                lubSub.put(var, aux);
            } else if (!other.sub.containsKey(var)) {
                other.sub.get(var).forEach( id -> {
                    if (other.interval.getState(var).interval.getHigh().compareTo(other.interval.getState(id).interval.getLow()) < 0) {
                        aux.add(id);
                    }
                });
                lubSub.put(var, aux);
            } else {
                aux.addAll(this.sub.get(var));
                aux.retainAll(other.sub.get(var));
                lubSub.put(var, aux);
            }
        });

        return new PentagonDomain(lubInterval, lubSub);
    }

    @Override
    public boolean lessOrEqual(PentagonDomain other) throws SemanticException {
        return interval.lessOrEqual(other.interval) && this.subLessOrEqual(other);
    }

    private boolean subLessOrEqual(PentagonDomain other) {
        return other.sub.keySet().stream().allMatch( var2 -> other.sub.get(var2).stream().allMatch( id2 -> this.sub.get(var2).contains(id2) ||
                this.interval.getState(var2).interval.getHigh().compareTo(this.interval.getState(id2).interval.getLow()) < 0));
    }

    @Override
    public PentagonDomain top() {
        return new PentagonDomain(interval.top(), new HashMap<>());
    }

    @Override
    public PentagonDomain bottom() {
        return new PentagonDomain(interval.bottom(), new HashMap<>());
    }

    @Override
    public PentagonDomain assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        ValueEnvironment<Interval> newInterval = interval.assign(id, expression, pp);
        Map<Identifier, Set<Identifier>> newSub = sub;

        if (pp instanceof Constant) {
            newSub.put(id, new HashSet<>());
        } else if (expression instanceof Identifier) {
            sub.get((Identifier) expression).remove(id);
            sub.put(id, sub.get( (Identifier) expression));
        } else if (expression instanceof BinaryExpression) {
            throw new NotImplementedException();
        }

        return new PentagonDomain(newInterval, newSub).recomputePentagon();
    }

    @Override
    public PentagonDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this.copy();
    }

    @Override
    public PentagonDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        ValueEnvironment<Interval> newInterval = interval.assume(expression, pp); //interval as always
        Map<Identifier, Set<Identifier>> newSub = sub; //create new sub starting from the old one

        //since SUB are not affected by numbers we can only consider the case of identifiers and binary expressions (or negation of binary expressions)
        BinaryExpression exp = null;
        Identifier left = null;
        Identifier right = null;

        //negation of binary expression (with two identifiers) (ex. !(x < y))
        if(expression instanceof UnaryExpression){
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            if (unaryExpression.getOperator() instanceof LogicalNegation) {
                expression = expression.removeNegations();
            }
        }

        //binary expression (with two identifiers) (ex. x < y)
        if(expression instanceof BinaryExpression && ((BinaryExpression)expression).getLeft() instanceof Identifier && ((BinaryExpression)expression).getRight() instanceof Identifier) {
            exp = (BinaryExpression) expression;
            left = (Identifier) exp.getLeft();
            right = (Identifier) exp.getRight();
        }
        else //if no sub should be affected, return the same domain recomposed by the changes in the interval
            return new PentagonDomain(newInterval, newSub).recomputePentagon();

        if (exp.getOperator() instanceof ComparisonLt){ // x < y
            newSub.get(right).remove(left);
            newSub.get(left).add(right);
            newSub.get(left).addAll(sub.get(right));
        }
        if (exp.getOperator() instanceof ComparisonGt){ //x > y
            newSub.get(left).remove(right);
            newSub.get(right).add(left);
            newSub.get(right).addAll(sub.get(left));
        }
        if (exp.getOperator() instanceof ComparisonLe){ // x <= y
            newSub.get(left).remove(right);
            newSub.get(right).remove(left);
            newSub.get(left).addAll(sub.get(right));
        }
        if (exp.getOperator() instanceof ComparisonGe){ // x >= y
            newSub.get(left).remove(right);
            newSub.get(right).remove(left);
            newSub.get(right).addAll(sub.get(left));
        }
        if (exp.getOperator() instanceof ComparisonEq){ //x==y
            newSub.get(left).remove(right);
            newSub.get(right).remove(left);
            newSub.get(left).addAll(sub.get(right));
            newSub.get(right).addAll(sub.get(left));
        }

        return new PentagonDomain(newInterval, newSub).recomputePentagon();
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
        return null;
    }

    @Override
    public PentagonDomain pushScope(ScopeToken token) throws SemanticException {
        return this.copy();
    }

    @Override
    public PentagonDomain popScope(ScopeToken token) throws SemanticException {
        return this.copy();
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }

    /**
     * Performs a deep copy of the current instance
     * @return the copy of this
     */
    private PentagonDomain copy() {
        Map<Identifier, Interval> cp = interval.mkNewFunction(interval.getMap(), false);
        ValueEnvironment<Interval> intervalCopy = interval.mk(interval.lattice, cp);

        Map<Identifier, Set<Identifier>> subCopy = new HashMap<>();
        sub.forEach((id, idSet) -> {
            subCopy.put(id, new HashSet<>(idSet));
        });

        return new PentagonDomain(intervalCopy, subCopy);
    }
}
