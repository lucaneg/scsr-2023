package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.LogicalNegation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        PentagonDomain newPentagon = this.copy();
        interval.getKeys().forEach(id1 -> {
            Set<Identifier> aux = new HashSet<>();
            interval.getKeys().forEach(id2 -> {
                if ((interval.getState(id1).interval != null && interval.getState(id2).interval != null) && !id1.equals(id2) && interval.getState(id1).interval.getHigh().compareTo(interval.getState(id2).interval.getLow()) < 0) {
                    aux.add(id2);
                }
            });

            newPentagon.sub.computeIfAbsent(id1, k -> new HashSet<>()).addAll(aux);

        });
        return newPentagon;
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
                other.sub.get(var).forEach( id -> {
                    if (this.interval.getState(var).interval != null && this.interval.getState(id).interval != null &&
                            this.interval.getState(var).interval.getHigh().compareTo(this.interval.getState(id).interval.getLow()) < 0) {
                        aux.add(id);
                    }
                });
                lubSub.put(var, aux);
                lubSub.get(var).addAll(other.sub.get(var));
            } else if (!other.sub.containsKey(var)) {
                this.sub.get(var).forEach( id -> {
                    if (other.interval.getState(var).interval != null && other.interval.getState(id).interval != null &&
                            other.interval.getState(var).interval.getHigh().compareTo(other.interval.getState(id).interval.getLow()) < 0) {
                        aux.add(id);
                    }
                });
                lubSub.put(var, aux);
                lubSub.get(var).addAll(this.sub.get(var));
            } else {
                aux.addAll(this.sub.get(var));
                aux.retainAll(other.sub.get(var));
                lubSub.put(var, aux);
            }
        });
        return new PentagonDomain(lubInterval, lubSub).recomputePentagon();
    }

    @Override
    public boolean lessOrEqual(PentagonDomain other) throws SemanticException {
        return interval.lessOrEqual(other.interval) && this.subLessOrEqual(other);
    }

    private boolean subLessOrEqual(PentagonDomain other) {
        this.sub.keySet().forEach( id -> {
            other.sub.computeIfAbsent(id, k -> new HashSet<>());
        });

        other.sub.keySet().forEach( id -> {
            this.sub.computeIfAbsent(id, k -> new HashSet<>());
        });

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

        if (expression instanceof Constant) {
            newSub.put(id, new HashSet<>());
            newSub.forEach((k,v) -> {
                v.remove(id);
            });
        } else if (expression instanceof Identifier) {
            newSub.forEach((k,v) -> {
                v.remove(id);
            });
            if (newSub.get((Identifier) expression) != null)
                newSub.get((Identifier) expression).remove(id);
            else
                newSub.put((Identifier) expression, new HashSet<>());
            newSub.put(id, new HashSet<>(sub.get( (Identifier) expression)));
        } else if (expression instanceof BinaryExpression) {
            newSub.forEach((k,v) -> {
                v.remove(id);
            });
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            SymbolicExpression left = binaryExpression.getLeft();
            SymbolicExpression right = binaryExpression.getRight();
            BinaryOperator binaryOperator = binaryExpression.getOperator();
            if (left instanceof Identifier && right instanceof Constant) {
                Identifier leftId = (Identifier) left;
                Constant rightConst = (Constant) right;
                if (binaryOperator instanceof AdditionOperator) {
                    if ((Integer) rightConst.getValue() > 0) {
                        newSub.put(id, new HashSet<>());
                        newSub.get(leftId).add(id);
                    } else {
                        if (newSub.get(leftId) != null)
                            newSub.get(leftId).remove(id);
                        else
                            newSub.put(leftId, new HashSet<>());
                        if ((Integer) rightConst.getValue() < 0) {
                            newSub.get(id).add(leftId);
                        }
                        newSub.put(id, new HashSet<>(sub.get(leftId)));
                    }
                } else if (binaryOperator instanceof SubtractionOperator) {
                    if ((Integer) rightConst.getValue() < 0) {
                        newSub.put(id, new HashSet<>());
                        newSub.get(leftId).add(id);
                    } else {
                        if (newSub.get(leftId) != null)
                            newSub.get(leftId).remove(id);
                        else
                            newSub.put(leftId, new HashSet<>());
                        newSub.put(id, new HashSet<>(sub.get(leftId)));
                        if ((Integer) rightConst.getValue() > 0)
                            newSub.get(id).add(leftId);
                    }
                } else {
                    newSub.put(id, new HashSet<>());
                }
            } else if (left instanceof Identifier && right instanceof Identifier){
                Identifier leftId = (Identifier) left;
                Identifier rightId = (Identifier) right;
                newSub.put(id, new HashSet<>());
                if (newSub.get(leftId) != null)
                 newSub.get(leftId).remove(id);
                else
                    newSub.put(leftId, new HashSet<>());
                if (newSub.get(rightId) != null)
                 newSub.get(rightId).remove(id);
                else
                    newSub.put(rightId, new HashSet<>());
            }
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
            if (newSub.get(right) != null)
                newSub.get(right).remove(left);
            else
                newSub.put(right, new HashSet<>());
            newSub.get(left).add(right);
            newSub.get(left).addAll(sub.get(right));
        }
        if (exp.getOperator() instanceof ComparisonGt){ //x > y
            if (newSub.get(left) != null)
                newSub.get(left).remove(right);
            else
                newSub.put(left, new HashSet<>());
            newSub.get(right).add(left);
            newSub.get(right).addAll(sub.get(left));
        }
        if (exp.getOperator() instanceof ComparisonLe){ // x <= y
            if (newSub.get(left) != null)
                newSub.get(left).remove(right);
            else
                newSub.put(left, new HashSet<>());
            if (newSub.get(right) != null)
                newSub.get(right).remove(left);
            else
                newSub.put(right, new HashSet<>());
            newSub.get(left).addAll(sub.get(right));
        }
        if (exp.getOperator() instanceof ComparisonGe){ // x >= y
            if (newSub.get(left) != null)
                newSub.get(left).remove(right);
            else
                newSub.put(left, new HashSet<>());
            if (newSub.get(right) != null)
                newSub.get(right).remove(left);
            else
                newSub.put(right, new HashSet<>());
            newSub.get(right).addAll(sub.get(left));
        }
        if (exp.getOperator() instanceof ComparisonEq){ //x==y
            if (newSub.get(left) != null)
                newSub.get(left).remove(right);
            else
                newSub.put(left, new HashSet<>());
            if (newSub.get(right) != null)
                newSub.get(right).remove(left);
            else
                newSub.put(right, new HashSet<>());
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

    private Satisfiability satisfiesSub(ValueExpression expression, ProgramPoint pp){
        Satisfiability sat = Satisfiability.UNKNOWN;
        if(expression instanceof UnaryExpression){
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            if (unaryExpression.getOperator() instanceof LogicalNegation) {
                expression = expression.removeNegations();
            }
        }

        BinaryExpression exp = null;
        Identifier left = null;
        Identifier right = null;

        if(expression instanceof BinaryExpression && ((BinaryExpression)expression).getLeft() instanceof Identifier && ((BinaryExpression)expression).getRight() instanceof Identifier) {
            exp = (BinaryExpression) expression;
            left = (Identifier) exp.getLeft();
            right = (Identifier) exp.getRight();

            if (exp.getOperator() instanceof ComparisonLt || exp.getOperator() instanceof ComparisonLe){ // x < y || x <= y
                if (sub.get(left).contains(right)) {
                    sat = Satisfiability.SATISFIED;
                }
            }
            if (exp.getOperator() instanceof ComparisonGt || exp.getOperator() instanceof ComparisonGe){ //x > y || x >= y
                if (sub.get(right).contains(left)) {
                    sat = Satisfiability.SATISFIED;
                }
            }
            if (exp.getOperator() instanceof ComparisonEq){ //x==y
                if (sub.get(left).contains(right) || sub.get(right).contains(left)) {
                    sat = Satisfiability.NOT_SATISFIED;
                }
            }

        }

        return sat;
    }
    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return interval.satisfies(expression, pp).or(satisfiesSub(expression, pp));
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
        return new StringRepresentation(interval.representation() + ", <br>" + sub.entrySet().stream().map(e -> e.getKey().getName() + " < [ " + e.getValue().toString() + " ]").collect(Collectors.toSet()));
    }


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
