package bin;

/**
 * Created by Armen on 5/21/2017.
 */
public class Clause {
    private Literal l1;
    private Literal l2;

    private boolean negate1;
    private boolean negate2;

    public boolean isTautology;

    public Clause(int l1, int l2) {
        this.isTautology = false;

        this.negate1 = false;
        this.negate2 = false;
        if(l1 < 0) {this.negate1 = true;}
        if(l2 < 0) {this.negate2 = true;}
        if(l1 == -l2) {this.isTautology = true;}
    }

    public void setL1(Literal lit) {
        this.l1 = lit;
        this.l1.appears();
        if(this.negate1) {
            this.l1.appearsNegated();
        }
    }
    public void setL2(Literal lit) {
        this.l2 = lit;
        this.l2.appears();
        if(this.negate2) {
            this.l2.appearsNegated();
        }
    }

    public boolean check() {
        if(isTautology) {
            return true;
        }
        else {
            boolean x1 = this.l1.getValue();
            boolean x2 = this.l2.getValue();
            if (this.negate1) {
                x1 = !x1;
            }
            if (this.negate2) {
                x2 = !x2;
            }

//        System.out.println(x1 || x2);
            return (x1 || x2);
        }
    }

}
