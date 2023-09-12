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
import it.unive.lisa.symbolic.value.operator.ComparisonOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.LogicalNegation;
import it.unive.lisa.util.numeric.MathNumber;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PentagonDomain implements ValueDomain<PentagonDomain> {

    /**
     * The interval environment of this abstract domain.
     */
    private ValueEnvironment<Interval> interval;

    /**
     * The set of sub expressions of this abstract domain.
     */
    private Map<Identifier, Set<Identifier>> sub;

    /**
     * Builds an empty instance of this abstract domain.
     */
    public PentagonDomain() {
        this(new ValueEnvironment<>(new Interval(), new HashMap<>(), new Interval()), new HashMap<>());
    }

    /**
     * Builds an instance of this abstract domain with the given interval environment and sub expressions.
     *
     * @param interval the interval environment of this abstract domain.
     * @param sub      the set of sub expressions of this abstract domain.
     */
    public PentagonDomain(ValueEnvironment<Interval> interval, Map<Identifier, Set<Identifier>> sub) {
        this.interval = interval;
        this.sub = sub;
    }

    /**
     * After the execution of some operation, this method is called to recompute the sub expressions of this abstract domain using the new interval environment.
     */
    private PentagonDomain recomputePentagon() {
        PentagonDomain newPentagon = this.copy();
        //add sub expressions derived from intervals
        interval.getKeys().forEach(id1 -> {
            Set<Identifier> aux = new HashSet<>();
            interval.getKeys().forEach(id2 -> {
                if ((interval.getState(id1).interval != null && interval.getState(id2).interval != null) && !id1.equals(id2) && interval.getState(id1).interval.getHigh().compareTo(interval.getState(id2).interval.getLow()) < 0) {
                    aux.add(id2);
                }
            });

            newPentagon.sub.computeIfAbsent(id1, k -> new HashSet<>()).addAll(aux);

        });

        //make sub expressions coherent with all the other sub expressions
        AtomicReference<Boolean> AllEmpty = new AtomicReference<>(false);
        while(!AllEmpty.get()) {
            AllEmpty.set(true);
            newPentagon.sub.forEach((id1, set) -> {
                Set<Identifier> aux = new HashSet<>();
                set.forEach(id2 -> {
                    if (newPentagon.sub.containsKey(id2)) {
                        aux.addAll(newPentagon.sub.get(id2));
                    }
                });
                set.addAll(aux);
                if (!set.containsAll(aux)) {
                    AllEmpty.set(false);
                }
            });
        }



        return newPentagon;
    }

    /**
     * Computes the least upper bound between two pentagons. This is done by computing the least upper bound between the interval environments.
     * The resulting sub expressions are computed by taking the union of the lub between the sub expressions of the two pentagons and the sub expressions obtained by the interval environment.
     *
     * @return the new pentagon.
     */
    @Override
    public PentagonDomain lub(PentagonDomain other) throws SemanticException {
        //lub interval
        ValueEnvironment<Interval> lubInterval = this.interval.lub(other.interval);
        Map<Identifier, Set<Identifier>> lubSub = new HashMap<>();

        // all the variables in the two pentagons
        Set<Identifier> variables = new HashSet<>();
        variables.addAll(this.sub.keySet());
        variables.addAll(other.sub.keySet());

        variables.forEach(var -> {
            Set<Identifier> aux = new HashSet<>();
            if (!this.sub.containsKey(var)) {

                other.sub.get(var).forEach(id -> {
                    if (this.interval.getState(var).interval != null && this.interval.getState(id).interval != null &&
                            this.interval.getState(var).interval.getHigh().compareTo(this.interval.getState(id).interval.getLow()) < 0) {
                        aux.add(id);
                    }
                });
                lubSub.put(var, aux);
                lubSub.get(var).addAll(other.sub.get(var));
            } else if (!other.sub.containsKey(var)) {
                this.sub.get(var).forEach(id -> {
                    if (other.interval.getState(var).interval != null && other.interval.getState(id).interval != null &&
                            other.interval.getState(var).interval.getHigh().compareTo(other.interval.getState(id).interval.getLow()) < 0) {
                        aux.add(id);
                    }
                });
                lubSub.put(var, aux);
                lubSub.get(var).addAll(this.sub.get(var));
            } else {
                // if the intersection between the two sets is not empty, then we have to add the sub expressions of the interval environment.
                aux.addAll(this.sub.get(var));
                aux.retainAll(other.sub.get(var));
                lubSub.put(var, aux);
            }
        });
        return new PentagonDomain(lubInterval, lubSub).recomputePentagon();
    }

    /**
     * Determines if this abstract domain is less or equal than the other abstract domain.
     * @param other the other domain element
     *
     * @return true if this abstract domain is less or equal than the other abstract domain, false otherwise.
     * @throws SemanticException if something goes wrong during the comparison.
     */
    @Override
    public boolean lessOrEqual(PentagonDomain other) throws SemanticException {
        return interval.lessOrEqual(other.interval) && this.subLessOrEqual(other);
    }

    /**
     * Helper method to determine if the sub of this abstract domain is less or equal than the sub of the other abstract domain.
     * @param other the other domain element
     * @return true if the sub of this abstract domain is less or equal than the sub of the other abstract domain, false otherwise.
     */
    private boolean subLessOrEqual(PentagonDomain other) {
        //create the sub for the pentagons if they are not present
        this.sub.keySet().forEach( id -> {
            other.sub.computeIfAbsent(id, k -> new HashSet<>());
        });

        other.sub.keySet().forEach( id -> {
            this.sub.computeIfAbsent(id, k -> new HashSet<>());
        });

        // check if the sub of this abstract domain is less or equal than the sub of the other abstract domain by verify the presence of each sub in the other abstract domain
        // or by checking if sup of the interval of the sub is less than the inf of the interval of the sub in the other abstract domain.
        return other.sub.keySet().stream().allMatch( var2 -> other.sub.get(var2).stream().allMatch( id2 -> this.sub.get(var2).contains(id2) ||
                this.interval.getState(var2).interval.getHigh().compareTo(this.interval.getState(id2).interval.getLow()) < 0));
    }

    /**
     * @return a new instance of pentagon domain with the TOP value for the interval environment and the set of sub expressions.
     */
    @Override
    public PentagonDomain top() {
        return new PentagonDomain(interval.top(), new HashMap<>());
    }

    /**
     * @return a new instance of pentagon domain with the BOTTOM value for the interval environment and the set of sub expressions.
     */
    @Override
    public PentagonDomain bottom() {
        return new PentagonDomain(interval.bottom(), new HashMap<>());
    }

    /**
     * compute the pentagon domain by using the information of the assignment. (for simplicity we assume expressions up to binary expressions)
     * @param id         the identifier to assign the value to
     * @param expression the expression to assign
     * @param pp         the program point that where this operation is being evaluated
     *
     * @return the new pentagon domain.
     * @throws SemanticException if something goes wrong during the computation.
     */
    @Override
    public PentagonDomain assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        PentagonDomain newPentagon = this.copy();
        // assign the value to the interval environment
        newPentagon.interval = interval.assign(id, expression, pp);
        Map<Identifier, Set<Identifier>> newSub = newPentagon.sub;

        // remove the sub expression of the identifier from the sub environment (we lose information because we don't know the value of the identifier anymore)
        newSub.forEach((k, v) -> {
            v.remove(id);
        });

        // if the expression is a constant or an identifier, then we clear the sub expression of the identifier.
        if (expression instanceof Constant) {
            newSub.put(id, new HashSet<>());
        }
        // if the expression is an identifier (x=y) then the sub of x is the sub of y. we remove x from y's sub. we add x to all the sub that contains y.
        else if (expression instanceof Identifier) {
            newSub.computeIfAbsent((Identifier) expression, k -> new HashSet<>()).remove(id);
            newSub.put(id, new HashSet<>(newSub.get((Identifier) expression)));
            newSub.forEach((k, v) -> {
                if (v.contains((Identifier) expression)) {
                    v.add(id);
                }
            });
        }
        // if the expression is a binary expression, then we have to compute the sub expression of the identifier.
        else if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            //get the left and right expression of the binary expression
            SymbolicExpression left = binaryExpression.getLeft();
            SymbolicExpression right = binaryExpression.getRight();
            BinaryOperator binaryOperator = binaryExpression.getOperator();
            // x = 5 + y --> x = y + 5
            if(left instanceof Constant && right instanceof Identifier){
                left = right;
                right = binaryExpression.getLeft();
            }

            if (left instanceof Identifier && right instanceof Constant) {
                Identifier leftId = (Identifier) left;
                Constant rightConst = (Constant) right;
                // x = y + 5
                if (binaryOperator instanceof AdditionOperator) {
                    if ((Integer) rightConst.getValue() > 0) {
                        newSub.put(id, new HashSet<>());
                        newSub.get(leftId).add(id);
                    } else {
                        newSub.computeIfAbsent(leftId, k -> new HashSet<>()).remove(id);
                        if ((Integer) rightConst.getValue() < 0) {
                            newSub.get(id).add(leftId);
                        }
                        newSub.put(id, new HashSet<>(newSub.get(leftId)));
                    }
                }
                // x = y - 5
                else if (binaryOperator instanceof SubtractionOperator) {
                    if ((Integer) rightConst.getValue() < 0) {
                        newSub.put(id, new HashSet<>());
                        newSub.get(leftId).add(id);
                    }
                    else {
                        newSub.computeIfAbsent(leftId, k -> new HashSet<>()).remove(id);
                        newSub.put(id, new HashSet<>(newSub.get(leftId)));
                        if ((Integer) rightConst.getValue() > 0)
                            newSub.get(id).add(leftId);
                    }
                }
                //x = y / 5
                else if (binaryOperator instanceof DivisionOperator) {
                    if ((Integer) rightConst.getValue() >= 1) {
                        newSub.computeIfAbsent(leftId, k -> new HashSet<>()).remove(id);
                        newSub.put(id, new HashSet<>(newSub.get(leftId)));
                        if ((Integer) rightConst.getValue() > 1)
                            newSub.get(id).add(leftId);
                    }
                    else if ((Integer) rightConst.getValue() > 0) {
                        newSub.put(id, new HashSet<>());
                        newSub.get(leftId).add(id);
                    }
                    else {
                        newSub.put(id, new HashSet<>());
                    }
                }
                // TOP in all the other cases
                else {
                    newSub.put(id, new HashSet<>());
                }
                newSub.get(id).remove(id);
            }
            else if(left instanceof BinaryExpression && right instanceof Constant && binaryOperator instanceof DivisionOperator)
            {
                // id = (left + right)/2
                if (((BinaryExpression) left).getLeft() instanceof Identifier && ((BinaryExpression) left).getRight() instanceof Identifier && (Integer) ((Constant) right).getValue() == 2) {
                    Identifier LeftId = (Identifier) ((BinaryExpression) left).getLeft();
                    Identifier RightId = (Identifier) ((BinaryExpression) left).getRight();

                    //take the values presents in both the sub of leftid and rightid
                    Set<Identifier> sub = new HashSet<>(newSub.get(LeftId));
                    sub.retainAll(newSub.get(RightId));
                    //since the operation is the mean, id will be less than all the value that are in both sub
                    newSub.put(id, new HashSet<>());
                    newSub.get(id).addAll(sub);
                }

            }
            // x = y [operator] z
            else if (left instanceof Identifier && right instanceof Identifier) {
                Identifier leftId = (Identifier) left;
                Identifier rightId = (Identifier) right;
                if(binaryOperator instanceof SubtractionOperator)
                {
                    //check if the bottom of interval of left1 is greater than 0
                    if(newPentagon.interval.getState(rightId).interval.getLow().compareTo(new MathNumber(0))>0)
                    {
                        newSub.put(id, new HashSet<>());
                        newSub.get(id).add(leftId);
                        newSub.get(id).addAll(newSub.get(leftId));

                    }
                }
                else {
                    newSub.put(id, new HashSet<>());
                    newSub.computeIfAbsent(leftId, k -> new HashSet<>()).remove(id);
                    newSub.computeIfAbsent(rightId, k -> new HashSet<>()).remove(id);
                }
            }
        }
        return newPentagon.recomputePentagon();
    }

    /**
     * compute the pentagon domain by using the information of to assume. (for simplicity we assume expressions up to binary expressions)
     * @param expression the expression to assume to hold.
     * @param pp         the program point that where this operation is being evaluated
     *
     * @return the new pentagon domain.
     * @throws SemanticException if something goes wrong during the computation.
     */
    @Override
    public PentagonDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        PentagonDomain newPentagon = this.copy();
        newPentagon.interval = interval.assume(expression, pp);
        Map<Identifier, Set<Identifier>> newSub = newPentagon.sub;

        //since SUB are not affected by numbers we can only consider the case of identifiers and binary expressions (or negation of binary expressions)
        BinaryExpression exp = null;
        Identifier left = null;
        Identifier right = null;

        //negation of binary expression (with two identifiers) (ex. !(x < y) --> x >= y)
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            if (unaryExpression.getOperator() instanceof LogicalNegation) {
                expression = expression.removeNegations();
            }
        }

        //binary expression (with two identifiers) (ex. x < y)
        if (expression instanceof BinaryExpression && ((BinaryExpression) expression).getLeft() instanceof Identifier && ((BinaryExpression) expression).getRight() instanceof Identifier) {
            exp = (BinaryExpression) expression;
            left = (Identifier) exp.getLeft();
            right = (Identifier) exp.getRight();
        } else //if no sub should be affected, return the same domain recomposed by the changes in the interval
            return newPentagon.recomputePentagon();

        if (exp.getOperator() instanceof ComparisonLt) { // x < y
            newSub.computeIfAbsent(right, k -> new HashSet<>()).remove(left);
            newSub.computeIfAbsent(left, k -> new HashSet<>()).add(right);
            newSub.get(left).addAll(newSub.get(right));
        }
        if (exp.getOperator() instanceof ComparisonGt) { //x > y
            newSub.computeIfAbsent(left, k -> new HashSet<>()).remove(right);
            newSub.computeIfAbsent(right, k -> new HashSet<>()).add(left);
            newSub.get(right).addAll(newSub.get(left));
        }
        if (exp.getOperator() instanceof ComparisonLe) { // x <= y
            newSub.computeIfAbsent(left, k -> new HashSet<>()).remove(right);
            newSub.computeIfAbsent(right, k -> new HashSet<>()).remove(left);
            newSub.get(left).addAll(newSub.get(right));
        }
        if (exp.getOperator() instanceof ComparisonGe) { // x >= y
            newSub.computeIfAbsent(left, k -> new HashSet<>()).remove(right);
            newSub.computeIfAbsent(right, k -> new HashSet<>()).remove(left);
            newSub.get(right).addAll(newSub.get(left));
        }
        if (exp.getOperator() instanceof ComparisonEq) { //x==y
            newSub.computeIfAbsent(left, k -> new HashSet<>()).remove(right);
            newSub.computeIfAbsent(right, k -> new HashSet<>()).remove(left);
            newSub.get(left).addAll(newSub.get(right));
            newSub.get(right).addAll(newSub.get(left));
        }

        return newPentagon.recomputePentagon();
    }

    /**
     * Verifies if the current domain satisfies the given expression.
     * @param expression the expression whose satisfiability is to be evaluated
     * @param pp         the program point that where this operation is being evaluated
     *
     * @return the satisfiability of the expression
     * @throws SemanticException if something goes wrong during the computation.
     */
    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        //we proceed by using the or clause since we just need that one of the two elements satisfies the expression (the other element is always coherent with the first one since we perform recomputePentagon())
        return interval.satisfies(expression, pp).or(satisfiesSub(expression, pp));
    }

    /**
     * Helper method for satisfies. It checks if the expression is satisfied by the sub domain (for simplicity we consider up to binary expressions).
     * @param expression the expression to be checked
     * @param pp        the program point that where this operation is being evaluated
     * @return the satisfiability of the expression
     */
    private Satisfiability satisfiesSub(ValueExpression expression, ProgramPoint pp){
        Satisfiability sat = Satisfiability.UNKNOWN;

        // negation of binary expression (with two identifiers) (ex. !(x < y) becomes x >= y)
        if(expression instanceof UnaryExpression){
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            if (unaryExpression.getOperator() instanceof LogicalNegation) {
                expression = expression.removeNegations();
            }
        }

        BinaryExpression exp = null;
        Identifier left = null;
        Identifier right = null;

        // in order to understand if the expression is satisfied we need to check only the comparisons (since in the sub domain we have only comparisons and no information about numbers)
        if(expression instanceof BinaryExpression && ((BinaryExpression)expression).getLeft() instanceof Identifier && ((BinaryExpression)expression).getRight() instanceof Identifier) {
            exp = (BinaryExpression) expression;
            left = (Identifier) exp.getLeft();
            right = (Identifier) exp.getRight();

            if (exp.getOperator() instanceof ComparisonLt || exp.getOperator() instanceof ComparisonLe){ // x < y || x <= y
                if (sub.get(left) != null && sub.get(left).contains(right)) {
                    sat = Satisfiability.SATISFIED;
                }
            }
            if (exp.getOperator() instanceof ComparisonGt || exp.getOperator() instanceof ComparisonGe){ //x > y || x >= y
                if (sub.get(right) != null && sub.get(right).contains(left)) {
                    sat = Satisfiability.SATISFIED;
                }
            }
            if (exp.getOperator() instanceof ComparisonEq){ //x==y
                if ((sub.get(left) != null && sub.get(left).contains(right)) || (sub.get(right) != null && sub.get(right).contains(left))) {
                    sat = Satisfiability.NOT_SATISFIED;
                }
            }

        }

        return sat;
    }


    /**
     * Get the representation of the domain as a string
     * @return the representation of the domain as a string
     */
    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(interval.representation() + ", <br>" + sub.entrySet().stream().map(e -> e.getKey().getName() + " < [ " + e.getValue().toString() + " ]").collect(Collectors.toSet()));
    }


    /**
     * Copy the current domain
     * @return the copy of the current domain
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

    @Override
    public PentagonDomain pushScope(ScopeToken token) throws SemanticException {
        return this.copy();
    }

    @Override
    public PentagonDomain popScope(ScopeToken token) throws SemanticException {
        return this.copy();
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
    public PentagonDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this.copy();
    }
}