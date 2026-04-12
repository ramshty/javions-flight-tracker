package javions;

/**
 * the purpose of this class is to facilitate the checking of some methods' arguments
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class Preconditions {
    private Preconditions() {
    }

    /**
     * @param shouldBeTrue the argument that is needed to be true
     * @throws IllegalArgumentException if shouldBetrue is false otherwise it does nothing.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
