package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PentagonDomain implements ValueDomain<PentagonDomain> {
    static final PentagonDomain BOTTOM = new PentagonDomain(PentagonType.BOTTOM);
    static final PentagonDomain TOP = new PentagonDomain(PentagonType.TOP);

    private final PentagonType type;
    private final Map<Identifier, PentagonElement> pentagons = new HashMap<>();

    public PentagonDomain(PentagonType type) {
        this.type = type;
    }

    public PentagonDomain() {
        this.type = PentagonType.GENERAL;
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
        return isIntervalLessOrEqual(getBoxDomain(that)) &&
                isSubLessOrEqual(getBoxDomain(this), getSubDomain(this), getSubDomain(that));
    }

    private boolean isIntervalLessOrEqual(Map<Identifier, Interval> b2) {
        return getBoxDomain(this).entrySet().stream().allMatch(x -> {
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
        return this;
    }

    @Override
    public PentagonDomain smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public PentagonDomain assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return this;
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
        throw new NotImplementedException();
    }

    @Override
    public PentagonDomain pushScope(ScopeToken token) throws SemanticException {
        return this;
    }

    @Override
    public PentagonDomain popScope(ScopeToken token) throws SemanticException {
        return this;
    }

    @Override
    public DomainRepresentation representation() {
        throw new NotImplementedException();
    }

    public enum PentagonType {
        BOTTOM,
        TOP,
        GENERAL
    }
}
