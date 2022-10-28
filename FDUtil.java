import java.util.Set;
import java.util.HashSet;

/**
 * This utility class is not meant to be instantiated, and just provides some
 * useful methods on FD sets.
 * 
 * @author Tristan Gaeta
 * @version 10-27-22
 */
public final class FDUtil {

  /**
   * Resolves all trivial FDs in the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return a set of trivial FDs with respect to the given FDSet
   */
  public static FDSet trivial(final FDSet fdset) {
    FDSet out = new FDSet();
    for (final FD fd : fdset) {
      Set<Set<String>> ps = powerSet(fd.getLeft());
      for (Set<String> right : ps) {
        if (right.size() > 0)
          out.add(new FD(fd.getLeft(), right));
      }
    }
    return out;
  }

  /**
   * Augments every FD in the given set of FDs with the given attributes
   * 
   * @param fdset FD Set (Immutable)
   * @param attrs a set of attributes with which to augment FDs (Immutable)
   * @return a set of augmented FDs
   */
  public static FDSet augment(final FDSet fdset, final Set<String> attrs) {
    FDSet out = new FDSet();
    for (final FD fd : fdset) {
      FD newFD = new FD(fd);
      newFD.addToLeft(attrs);
      newFD.addToRight(attrs);
      out.add(newFD);
    }
    return out;
  }

  /**
   * Exhaustively resolves transitive FDs with respect to the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return all transitive FDs with respect to the input FD set
   */
  public static FDSet transitive(final FDSet fdset) {
    return transitiveRec(new FDSet(), new FDSet(fdset));
  }

  /**
   * Recursive helper method for transitive
   * 
   * @param accum the accumulation of transitive FDs
   * @param fdset the set of known/derived FDs
   * @return all transitive FDs with respect to the input FD set
   */
  private static FDSet transitiveRec(FDSet accum, FDSet fdset) {
    for (FD fdA : fdset) {
      for (FD fdB : fdset) {
        if (fdB.getLeft().containsAll(fdA.getRight())) {
          FD fd = new FD(fdA.getLeft(), fdB.getRight());
          if (fdset.getSet().add(fd)) {
            accum.add(fd);
            return transitiveRec(accum, fdset);
          }
        }
      }
    }
    return accum;
  }

  private static Set<String> allAttributes(final FDSet fdset) {
    Set<String> out = new HashSet<>();
    for (final FD fd : fdset) {
      out.addAll(fd.getLeft());
      out.addAll(fd.getRight());
    }
    return out;
  }

  /**
   * Generates the closure of the given FD Set
   * 
   * @param fdset (Immutable) FD Set
   * @return the closure of the input FD Set
   */
  public static FDSet fdSetClosure(final FDSet fdset) {
    FDSet copy = new FDSet(fdset);
    boolean repeat = true;
    while (repeat) {
      repeat = false;
      // augment
      Set<Set<String>> ps = powerSet(allAttributes(fdset));
      for (Set<String> subset : ps) {
        repeat |= copy.getSet().addAll(augment(fdset, subset).getSet());
      }
      // trivial
      repeat |= copy.getSet().addAll(trivial(copy).getSet());
      // transitivity
      repeat |= copy.getSet().addAll(transitive(copy).getSet());
    }
    return copy;
  }

  /**
   * Generates the power set of the given set (that is, all subsets of
   * the given set of elements)
   * 
   * @param set Any set of elements (Immutable)
   * @return the power set of the input set
   */
  @SuppressWarnings("unchecked")
  public static <E> Set<Set<E>> powerSet(final Set<E> set) {

    // base case: power set of the empty set is the set containing the empty set
    if (set.size() == 0) {
      Set<Set<E>> basePset = new HashSet<>();
      basePset.add(new HashSet<>());
      return basePset;
    }

    // remove the first element from the current set
    E[] attrs = (E[]) set.toArray();
    set.remove(attrs[0]);

    // recurse and obtain the power set of the reduced set of elements
    Set<Set<E>> currentPset = FDUtil.powerSet(set);

    // restore the element from input set
    set.add(attrs[0]);

    // iterate through all elements of current power set and union with first
    // element
    Set<Set<E>> otherPset = new HashSet<>();
    for (Set<E> attrSet : currentPset) {
      Set<E> otherAttrSet = new HashSet<>(attrSet);
      otherAttrSet.add(attrs[0]);
      otherPset.add(otherAttrSet);
    }
    currentPset.addAll(otherPset);
    return currentPset;
  }
}