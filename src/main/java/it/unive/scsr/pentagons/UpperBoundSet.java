package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.lattices.InverseSetLattice;
import it.unive.lisa.symbolic.value.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * This class implements the pentagons domain according to the abstract "Pentagons: A weakly relational abstract domain for the efficient validation of array accesses"
 * <a href="https://www.sciencedirect.com/science/article/pii/S0167642309000719">visit the paper's page</a>
 *
 * @author Finesso Davide 881825, Musone Mattia 877962, Porcu Davide 874311
 * @version 1.0.0
 */
public class UpperBoundSet extends InverseSetLattice<UpperBoundSet, Identifier> {

    public static final UpperBoundSet BOTTOM = new UpperBoundSet(new HashSet<>(), false); // empty set + false = BOTTOM
    public static final UpperBoundSet TOP = new UpperBoundSet(new HashSet<>(), true); // empty set + true = TOP

    @Override
    public boolean isTop() {
        return this.isTop;
    }

    @Override
    public boolean isBottom() {
        return super.isBottom();
    }

    public UpperBoundSet() {
        super(new HashSet<>(), true); // empty set + true = TOP
    }

    public UpperBoundSet(Set<Identifier> elements) {
        this(elements, false);
    }

    public UpperBoundSet(Set<Identifier> elements, boolean isTop) {
        super(elements, isTop);
    }

    @Override
    public UpperBoundSet mk(Set<Identifier> set) {
        return new UpperBoundSet(set, false);
    }

    @Override
    public UpperBoundSet top() {
        return TOP;
    }

    @Override
    public UpperBoundSet bottom() {
        return BOTTOM;
    }

    public boolean contains(UpperBoundSet other) {
        return this.elements.containsAll(other.elements);
    }
}
