package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.lattices.FunctionalLattice;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.LogicalNegation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class implements the pentagons domain according to the abstract "Pentagons: A weakly relational abstract domain for the efficient validation of array accesses"
 * <a href="https://www.sciencedirect.com/science/article/pii/S0167642309000719">visit the paper's page</a>
 *
 * @author Finesso Davide 881825, Musone Mattia 877962, Porcu Davide 874311
 * @version 1.0.0
 */
public class StrictUpperBound extends FunctionalLattice<StrictUpperBound, Identifier, UpperBoundSet> implements ValueDomain<StrictUpperBound> {

    public StrictUpperBound() {
        super(new UpperBoundSet());
        this.function = mkNewFunction(function, false);
    }

    public StrictUpperBound(UpperBoundSet lattice) {
        super(lattice);
        this.function = mkNewFunction(function, false);
    }

    public StrictUpperBound(UpperBoundSet lattice, Map<Identifier, UpperBoundSet> function) {
        super(lattice);
        this.function = mkNewFunction(function, false);
    }


    @Override
    public StrictUpperBound top() {
        return new StrictUpperBound(this.lattice.top(), null);
    }

    @Override
    public StrictUpperBound bottom() {
        return new StrictUpperBound(this.lattice.bottom(), null);
    }

    @Override
    public StrictUpperBound lubAux(StrictUpperBound other) throws SemanticException {
        Set<Identifier> id_set = new HashSet<>(this.function.keySet());
        id_set.addAll(other.function.keySet());
        var lub = new StrictUpperBound();
        for (var id : id_set) {
            UpperBoundSet element;
            if (!this.function.containsKey(id)) {
                element = other.function.get(id);
            } else if (!other.function.containsKey(id)) {
                element = this.function.get(id);
            } else {
                var this_id = this.function.get(id);
                var other_id = other.function.get(id);
                if ((!this_id.isTop() && other_id.isTop()) || (!this_id.isBottom() && other_id.isBottom()))
                    element = this_id;
                else if ((this_id.isTop() && !other_id.isTop()) || (this_id.isBottom() && !other_id.isBottom()))
                    element = other_id;
                else
                    element = new UpperBoundSet();
            }
            lub = lub.putState(id, element);
        }
        return lub;
    }

    /**
     * Notice: all operations that are not defined return TOP
     */
    @Override
    public StrictUpperBound assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        //We are interested only to assignment
        if (pp instanceof Assignment) {
            var clone = new StrictUpperBound(lattice, this.function);
            Map<Identifier, UpperBoundSet> newMap = mkNewFunction(function, false); // TODO queta non la usiamo mai in teoria...
            if (expression instanceof Constant) {
                clone = clone.putState(id, UpperBoundSet.TOP);
            } else if (expression instanceof Identifier) { // just copy same values id = idExp
                Identifier idExp = (Identifier) expression;
                clone = clone.putState(id, this.getState(idExp));
            } else if (expression instanceof BinaryExpression) {
                BinaryExpression binaryExpression = (BinaryExpression) expression;
                var sx = binaryExpression.getLeft();
                var dx = binaryExpression.getRight();
                HashSet<Identifier> result = new HashSet<>();
                if (binaryExpression.getOperator() instanceof AdditionOperator && dx instanceof Constant) {
                    Constant idx = (Constant) dx;
                    Identifier sxId = (Identifier) sx;
                    //Sum with a negative constant
                    if ((Integer) idx.getValue() < 0) {
                        result.addAll(clone.function.getOrDefault(sxId, new UpperBoundSet()).elements());
                        result.add(sxId);
                        clone = clone.putState(id, new UpperBoundSet(result));
                    }
                    //Sum with a positive constant
                    if ((Integer) idx.getValue() > 0)
                        clone = clone.putState(id, UpperBoundSet.TOP); // overwrite current top

                    if ((Integer) idx.getValue() == 0)
                        clone = clone.putState(id, this.getState((Identifier) sx));

                } else if (binaryExpression.getOperator() instanceof MultiplicationOperator) {
                    /*if(sx.getDynamicType().isNumericType() && dx.getDynamicType().isNumericType()){
                        var xx = sx.getDynamicType().asNumericType();
                        if(xx instanceof Int32Type) {
                            Int32Type ss = (Int32Type) xx;
                            var xy = dx.getDynamicType().asNumericType().isIntegral();
                        }
                        if(sx instanceof Constant && dx instanceof Constant)
                        {
                            Constant sxc = (Constant)sx;
                            Constant dxc = (Constant)dx;
                            int val1 = (Integer)sxc.getValue();
                            int val2 = (Integer)dxc.getValue();
                            if((val1 < 0 && val2 > 0) || (val1 > 0 && val2 < 0)){}

                        }
                    }*/
                    // safe
                    clone = clone.putState(id, UpperBoundSet.TOP);

                } else if (binaryExpression.getOperator() instanceof SubtractionOperator) {
                    if (sx instanceof Identifier && dx instanceof Constant) { // identifier - constant
                        Identifier identifier = (Identifier) sx;
                        Constant c = (Constant) dx;
                        Integer value = (Integer) c.getValue();
                        if (value > 0) { // id = identifier - PositiveConstant
                            // add elements already existing of identifier
                            result.addAll(clone.function.getOrDefault(identifier, new UpperBoundSet()).elements());
                            result.add(identifier);
                            clone = clone.putState(id, new UpperBoundSet(result));
                        } else if (value == 0) { // id =  identifier - 0 (like an assignement)
                            // just copy same values id = sxId
                            clone = clone.putState(id, this.getState(identifier));
                        } else { // id = identifier - (NegativeConstant) -> identifier + Constant
                            // safe: TOP
                            clone = clone.putState(id, UpperBoundSet.TOP);
                        }
                    } else if (sx instanceof Identifier && dx instanceof Identifier) { // identifier - identifier
                        clone = clone.putState(id, UpperBoundSet.TOP); // safe: top
                    }
                } else if (binaryExpression.getOperator() instanceof DivisionOperator) {
                    // safe
                    clone = clone.putState(id, UpperBoundSet.TOP); // overwrite current top
                } else {
                    // general case (any other case not handled): return TOP
                    clone = clone.putState(id, UpperBoundSet.TOP); // anything else assumed to be TOP
                }
            }
            return clone;
        }
        return new StrictUpperBound(this.lattice, this.function);
    }


    @Override
    public StrictUpperBound assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            // remove negation from conditional expression
            if (unaryExpression.getOperator() == LogicalNegation.INSTANCE) {
                expression = expression.removeNegations();
            }
        }
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            // Creates a new instance of the underlying function.
            Map<Identifier, UpperBoundSet> newMap = mkNewFunction(this.function, false);
            var clone = new StrictUpperBound(lattice, this.function);
            if (binaryExpression.getLeft() instanceof Identifier && binaryExpression.getRight() instanceof Identifier) {
                Identifier id1 = (Identifier) binaryExpression.getLeft();
                Identifier id2 = (Identifier) binaryExpression.getRight();
                BinaryOperator operator = binaryExpression.getOperator();
                if (operator == ComparisonEq.INSTANCE) { // == -> do the union of the two strict upper bound
                    clone = clone.putState(id1, clone.getState(id1).glb(clone.getState(id2)));
                    clone = clone.putState(id2, clone.getState(id2).glb(clone.getState(id2)));
                } else if (operator == ComparisonNe.INSTANCE) { // != -> the result is just the same map created before
                    // no need to do anything to the map: Strict Upper Bound not change
                } else if (operator instanceof ComparisonOperator) {
                    // all other comparisons: ComparisonGt (>), ComparisonGe (>=), ComparisonLt (<), ComparisonLe (<=)
                    // normalize in less/less-equal comparison
                    if (operator instanceof ComparisonGt || operator instanceof ComparisonGe) {
                        // swap id1 and id2 if id1 > id2 in order to obtain id2 <= id1
                        Identifier temp = id1;
                        id1 = id2;
                        id2 = temp;
                        // change the operator direction:
                        //  ">=" (ge) becomes "<=" (le)
                        //  ">"  (gt) becomes "<"  (lt)
                        operator = operator instanceof ComparisonGe ? ComparisonLe.INSTANCE : ComparisonLt.INSTANCE;
                    }
                    if (id1.equals(id2)) { // check if id1==id2 and id1<id2: if yes, it is BOTTOM
                        clone = clone.putState(id1, this.lattice.bottom()); // id1=id2 so just a single replace in the map
                    } else { // id1 < id2 or id1 <= id2
                        HashSet<Identifier> tempResultOfId1 = new HashSet<>();
                        if (!clone.getState(id1).isTop()) { // extract values of id1 if there are any (i.e. if it is not top)
                            tempResultOfId1.addAll(clone.getState(id1).elements);
                        }

                        // add StrictUpperBound of id2
                        // union of StrictUpperBound of id1 and StrictUpperBound of id2
                        if (!clone.getState(id2).isTop()) { // extract values of id1 if there are any (i.e. if it is not top)
                            tempResultOfId1.addAll(clone.getState(id2).elements);
                        }
                        // if id1 < id 2 then I have to add also y to the StrictUpperBound of x
                        if (operator instanceof ComparisonLt) {
                            tempResultOfId1.add(id2);
                        }
                        if (!tempResultOfId1.isEmpty()) { // replace the new StrictUpperBound of id1
                            clone = clone.putState(id1, new UpperBoundSet(tempResultOfId1));
                        }
                        // check for contradictions: if there are contradiction like x<y and y<x then set BOTTOM for both x and y
                        if (clone.getMap().containsKey(id1) &&
                                clone.getState(id1).elements().contains(id2) &&
                                clone.getState(id2).elements().contains(id1)) {
                            clone = clone.putState(id1, this.lattice.bottom());
                            clone = clone.putState(id2, this.lattice.bottom());
                        }

                    }
                }
            }
            return clone;
        }
        return new StrictUpperBound(this.lattice, this.function);
    }


    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(function != null ? function.toString() : "[]");
    }

    @Override
    public StrictUpperBound mk(UpperBoundSet lattice, Map<Identifier, UpperBoundSet> function) {
        return new StrictUpperBound(lattice, function);
    }

    @Override
    public StrictUpperBound forgetIdentifier(Identifier id) throws SemanticException {
        Map<Identifier, UpperBoundSet> newMap = mkNewFunction(function, false);
        newMap.remove(id);
        return new StrictUpperBound(lattice, newMap);
    }

    @Override
    public StrictUpperBound forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        Map<Identifier, UpperBoundSet> newMap = mkNewFunction(function, false);
        for (Identifier id : this.function.keySet()) {
            // add only who doesn't match the predicate = remove all identifiers that match the predicate
            if (!test.test(id)) {
                newMap.put(id, this.function.get(id));
            }
        }

        return new StrictUpperBound(lattice, newMap);
    }


    @Override
    public StrictUpperBound smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return new StrictUpperBound(this.lattice, this.function);
    }


    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Satisfiability sat = Satisfiability.UNKNOWN;
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            if (unaryExpression.getOperator() == LogicalNegation.INSTANCE) {
                expression = expression.removeNegations();
            }
        }
        if (expression instanceof BinaryExpression) {
            if (isBottom())
                sat = Satisfiability.BOTTOM;
            else {
                BinaryExpression binaryExpression = (BinaryExpression) expression;
                if (binaryExpression.getLeft() instanceof Identifier && binaryExpression.getRight() instanceof Identifier) {
                    Identifier id1 = (Identifier) binaryExpression.getLeft();
                    Identifier id2 = (Identifier) binaryExpression.getRight();
                    BinaryOperator operator = binaryExpression.getOperator();

                    if (operator == ComparisonEq.INSTANCE) {
                        if (getState(id2).contains(id1) || getState(id1).contains(id2))
                            sat = Satisfiability.NOT_SATISFIED;
                    } else if (operator == ComparisonGe.INSTANCE || operator == ComparisonGt.INSTANCE) {
                        if (getState(id2).contains(id1))
                            sat = Satisfiability.SATISFIED;
                    } else if (operator == ComparisonLe.INSTANCE || operator == ComparisonLt.INSTANCE) {
                        if (getState(id1).contains(id2))
                            sat = Satisfiability.SATISFIED;
                    } else if (operator == ComparisonNe.INSTANCE) {
                        if (getState(id2).contains(id1) || getState(id1).contains(id2))
                            sat = Satisfiability.SATISFIED;
                    } else if (operator == LogicalAnd.INSTANCE)
                        sat = satisfies(id1, pp).and(satisfies(id2, pp));
                    else if (operator == LogicalOr.INSTANCE)
                        sat = satisfies(id1, pp).or(satisfies(id2, pp));
                }
            }
        }
        return sat;
    }

    @Override
    public StrictUpperBound pushScope(ScopeToken token) throws SemanticException {
        return new StrictUpperBound(lattice, function);
    }

    @Override
    public StrictUpperBound popScope(ScopeToken token) throws SemanticException {
        return new StrictUpperBound(lattice, function);
    }
}

