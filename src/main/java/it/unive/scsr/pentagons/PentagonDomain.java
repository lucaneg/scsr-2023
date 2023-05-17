package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.SetRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.numeric.Addition;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.NumericOperation;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.numeric.IntInterval;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PentagonDomain implements ValueDomain<PentagonDomain> {
    public enum PentagonType {
        BOTTOM,
        TOP,
        GENERAL
    }
    public static final PentagonDomain BOTTOM = new PentagonDomain(PentagonType.BOTTOM);
    public static final PentagonDomain TOP = new PentagonDomain(PentagonType.TOP);

    private final PentagonType type;
    private final Map<Identifier, PentagonElement> pentagons;

    public PentagonDomain() {
        this(PentagonType.GENERAL, new HashMap<>());
    }

    public PentagonDomain(PentagonType type) {
        this (type, new HashMap<>());
    }

    private PentagonDomain(PentagonType type, Map<Identifier, PentagonElement> pentagons){
        this.type = type;
        this.pentagons = pentagons;
    }


    @Override
    public PentagonDomain lub(PentagonDomain other) throws SemanticException {
        Set<Identifier> ids = new HashSet<>();
        ids.addAll(this.pentagons.keySet());
        ids.addAll(other.pentagons.keySet());

        PentagonDomain lub = new PentagonDomain();

        ids.forEach(id -> {
            PentagonElement element;
            if(!this.pentagons.containsKey(id)){
                element = other.pentagons.get(id);
            }
            else if(!other.pentagons.containsKey(id)){
                element = this.pentagons.get(id);
            } else {
                Set<Identifier> elementSub;

                element = new PentagonElement(
                        getIntervalLub(other, id),
                        getSubLub(other, id)
                );
            }
            lub.pentagons.put(id, element);
        });

        return lub;
    }

    private Interval getIntervalLub(PentagonDomain other, Identifier id){
        Interval intvLub;

        Interval thisInterval = this.pentagons.get(id).getInterval();
        Interval otherInterval = other.pentagons.get(id).getInterval();

        if (thisInterval.isTop() || otherInterval.isTop()){
            intvLub = Interval.TOP;
        } else {
            intvLub = new Interval(
                    thisInterval.interval.getLow().min(otherInterval.interval.getLow()),
                    thisInterval.interval.getHigh().max(otherInterval.interval.getHigh())
            );
        }
        return intvLub;
    }

    private Set<Identifier> getSubLub(PentagonDomain other, Identifier id){
        Set<Identifier> thisSub = this.pentagons.get(id).getSub();
        Set<Identifier> otherSub = other.pentagons.get(id).getSub();

        return thisSub.stream().filter(otherSub::contains).collect(Collectors.toSet());
    }

    @Override
    public boolean lessOrEqual(PentagonDomain that) {
        return isIntervalLessOrEqual(getBoxDomain(this), getBoxDomain(that)) &&
                isSubLessOrEqual(getBoxDomain(this), getSubDomain(this), getSubDomain(that));
    }
    
    private Map<Identifier, Interval> getBoxDomain(PentagonDomain p){
        return p.pentagons.keySet().stream()
                .map(id -> new Pair<>(id, p.pentagons.get(id).getInterval()))
                .collect(Collectors.toMap(pair -> pair.a, pair -> pair.b));
    }

    private Map<Identifier, Set<Identifier>> getSubDomain(PentagonDomain p){
        return p.pentagons.keySet().stream()
                .map(id -> new Pair<>(id, p.pentagons.get(id).getSub()))
                .collect(Collectors.toMap(pair -> pair.a, pair -> pair.b));
    }

    private boolean isIntervalLessOrEqual(Map<Identifier, Interval> b1, Map<Identifier, Interval> b2) {
        return b1.entrySet().stream().allMatch(x -> {
            try {
                return b2.containsKey(x.getKey()) && x.getValue().lessOrEqual(b2.get(x.getKey()));
            } catch (SemanticException e) {
                return false;
            }
        });
    }

    private boolean isSubLessOrEqual(Map<Identifier, Interval> b1, Map<Identifier, Set<Identifier>> s1, Map<Identifier, Set<Identifier>> s2) {
        return s2.keySet().stream().allMatch(x -> s2.get(x).stream()
                .allMatch(y -> s1.get(x).contains(y) ||
                        b1.get(x).interval.getHigh().compareTo(b1.get(y).interval.getLow()) < 0)
        );
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
        PentagonDomain freshPentagon = this.copy();
        PentagonElement element = freshPentagon.removeIdentifier(id);

        if (expression instanceof Constant) {
            if (!expression.getStaticType().isNumericType() ||
                    !expression.getStaticType().asNumericType().isIntegral()) {
                return freshPentagon;
            }
            Constant constant = (Constant) expression;
            freshPentagon.addIdentifier(id, new PentagonElement(
                    new Interval((Integer) constant.getValue(), (Integer) constant.getValue()),
                    new HashSet<>()));
        } else if (expression instanceof UnaryExpression && ((UnaryExpression) expression).getOperator() instanceof NumericNegation) {
            freshPentagon.addIdentifier(id, new PentagonElement(
                    new Interval(element.getInterval().interval.getHigh().multiply(new MathNumber(-1)),
                            element.getInterval().interval.getLow().multiply(new MathNumber(-1))),
                    new HashSet<>()));
        } else if (expression instanceof BinaryExpression) {

            if (((BinaryExpression) expression).getOperator() instanceof AdditionOperator) {

                if (((BinaryExpression) expression).getLeft() instanceof Variable) {

                    System.out.println(((Variable) ((BinaryExpression) expression).getLeft()).getName());
                    System.out.println("--printing variable --");
                }

                if (((BinaryExpression) expression).getLeft() instanceof Constant) {
                    System.out.println("--printing constant --");
                    System.out.println(((Constant) ((BinaryExpression) expression).getLeft()).getValue());
                }



                // freshPentagon.addIdentifier(id, new PentagonElement());

            } else if (((BinaryExpression) expression).getOperator() instanceof SubtractionOperator) {

            } else if (((BinaryExpression) expression).getOperator() instanceof MultiplicationOperator) {

            } else if (((BinaryExpression) expression).getOperator() instanceof DivisionOperator) {

            }
        }
        return freshPentagon;
    }

    /**
     * Removes the identifier from the pentagon
     * @param id identifier to remove
     */
    private PentagonElement removeIdentifier(Identifier id) {
        this.pentagons.values().
                forEach(pentagonElement -> pentagonElement.getSub().removeIf(identifier -> identifier.equals(id)));
        return this.pentagons.remove(id);
    }

    /**
     * Adds a new Identifier to the pentagon
     * @param id the identifier
     * @param element the corresponding PentagonElement
     */
    private void addIdentifier(Identifier id, PentagonElement element) {
        this.pentagons.put(id, element);
    }

    @Override
    public PentagonDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this.copy();
    }

    @Override
    public PentagonDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
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
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Satisfiability.UNKNOWN;
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
        return new StringRepresentation(StringUtils.join(
                pentagons.entrySet().stream().map(e -> e.getKey().getName() + " -> " + e.getValue().representation().toString()).collect(Collectors.toList()),
                "\n <br>"));
    }

    @Override
    public int hashCode() {
        return this.pentagons.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PentagonDomain)) return false;
        return this.type == ((PentagonDomain) obj).type && this.pentagons.equals(((PentagonDomain) obj).pentagons);
    }

    /**
     * Performs a deep copy of the current instance
     * @return the copy of this
     */
    private PentagonDomain copy() {
        PentagonDomain newDomain = new PentagonDomain();
        pentagons.forEach((identifier, pentagonElement) ->
                newDomain.pentagons.put(identifier, new PentagonElement(
                        new Interval(pentagonElement.getInterval().interval.getLow(), pentagonElement.getInterval().interval.getHigh()),
                        new HashSet<>(pentagonElement.getSub()))));
        return newDomain;
    }
}