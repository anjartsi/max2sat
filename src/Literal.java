package bin;

/**
 * Created by Armen on 5/21/2017.
 */
public class Literal {
    private int index;
    private boolean value;

    private int occurences;
    private int negatedOccurences;

    public Literal(int index) {
        this.occurences = 1;
        this.value = false;
        this.index = index;

    }

    public void appears() {
        this.occurences++;
    }

    public void appearsNegated() {
        this.negatedOccurences++;
    }

    public int getOccurences() {
        return this.occurences;
    }
    public int getNegatedOccurences() {
        return this.negatedOccurences;
    }

    public boolean getValue() {
        return this.value;
    }

    public boolean getOppositeValue() {
        return !this.value;
    }

    public void set() {
        this.value = true;
    }
    public void reset() {
        this.value = false;
    }
    public boolean flip() {
        this.value = !this.value;

        return this.value;
    }

    public void make(boolean val) {
        this.value = val;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Literal)) {
            return false;
        }

        Literal user = (Literal) o;
        return index == user.index;
    }

    @Override
    public int hashCode() {
        return index;
    }
}
