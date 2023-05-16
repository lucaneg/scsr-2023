package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.SetRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
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

    public PentagonDomain(PentagonType type) {
        this (type, new HashMap<>());
    }

    public PentagonDomain() {
        this(PentagonType.GENERAL, new HashMap<>());
    }

    private PentagonDomain(PentagonType type, Map<Identifier, PentagonElement> pentagons){
        this.type = type;
        this.pentagons = pentagons;
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


    @Override
    public PentagonDomain lub(PentagonDomain other) throws SemanticException {
        return TOP;
    }

    @Override
    public boolean lessOrEqual(PentagonDomain that) {
        return isIntervalLessOrEqual(getBoxDomain(this), getBoxDomain(that)) &&
                isSubLessOrEqual(getBoxDomain(this), getSubDomain(this), getSubDomain(that));
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
        
        PentagonDomain newDomain = this.copy();
        
        newDomain.pentagons.values().
                forEach(pentagonElement -> pentagonElement.getSub().removeIf(identifier -> identifier.equals(id)));

        newDomain.pentagons.remove(id);


        if (expression instanceof Constant) {
            if (!expression.getStaticType().isNumericType() ||
                    !expression.getStaticType().asNumericType().isIntegral()) {
                
                return newDomain;
            }
            Constant constant = (Constant) expression;
            newDomain.pentagons.put(id, new PentagonElement(
                    new Interval((Integer) constant.getValue(), (Integer) constant.getValue()),
                    new HashSet<>()));
        }
        /*
        else if (expression instanceof UnaryExpression) {

        } else if (expression instanceof BinaryExpression) {

        } else if ()

         */
        
        return newDomain;
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
        return obj instanceof PentagonDomain && this.hashCode() == obj.hashCode();
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
