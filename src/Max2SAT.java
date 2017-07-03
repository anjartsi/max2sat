package bin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Armen on 5/21/2017.
 */
public class Max2SAT {
    private int answerLength;
    private int tatuologyCount;
    private ArrayList<Clause> clauses;
    private HashMap<Integer, Literal> literals;

    public Max2SAT(String fileName) {
        this.clauses = new ArrayList<Clause>();

        this.answerLength =this.getMaxLiteralValue(fileName);
        this.tatuologyCount = 0;

        // create a hash map with enough room for all the literals from 0 - max
        this.literals = new HashMap<Integer, Literal>(answerLength);

        this.readFile(fileName);

    }

    public int getNumLiterals() {
        return this.literals.size();
    }

    public int getNumClauses() {
        return this.clauses.size();
    }

    // Look through the file and record the highest literal value that is in it
    private int getMaxLiteralValue(String fileName) {
        int max = -1;
        int next;
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scanner.hasNextInt()) {
            next = scanner.nextInt();

            if(max < next) {max = next;};
        }
        scanner.close();
        return max;
    }

    // Go through the file and create Clause and Literal objects as needed
    private void readFile(String fileName) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int x1, x2;
        Clause c;
        Literal lit;

        // Go through each line of the input
        while(scanner.hasNextLine() && scanner.hasNextInt()) {
            x1 = scanner.nextInt();
            x2 = scanner.nextInt();

            // Initialize a new Clause object
            // Need x1 and x2 to have the correct sign for this
            c = new Clause(x1, x2);

            // x1 and x2 must be positive after these
            x1 = Math.abs(x1);
            x2 = Math.abs(x2);

            // If a clause is a tautology, do nothing.
            if(c.isTautology) {
                this.tatuologyCount++;
            }

            //  If a clause is not a tautology
            else {
                literals.putIfAbsent(x1, new Literal(x1));
                c.setL1(this.literals.get(x1));

                literals.putIfAbsent(x2, new Literal(x2));
                c.setL2(this.literals.get(x2));
            } // End else (tautology)

            // Add this clause to the arraylist
            this.clauses.add(c);

        } // End while(scanner.nextLine)

    } // End readFile

    public void printInstanceAnalysis(BufferedWriter bw) throws IOException {

        bw.write(String.format(
                "Number of clauses = %d, %d of which are tautologies\n"
                , this.clauses.size()
                ,this.tatuologyCount
        ));

        this.literals.forEach((k, v) -> {
            try {
                bw.write(String.format(
                        "Literal %d appears %d times, of which %.2f%% is negated\n"
                        , k
                        , v.getOccurences()
                        , 100 * ((float) v.getNegatedOccurences()) / v.getOccurences()
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw.write(String.format(
                "\nTotal number of literals: %d\n"
                , this.literals.size()
        ));
    }


    // Returns a string of T's and F's to be printed for the answer
    // Note: this prints as many letters as the highest-index literal
    // (for example, if literals 1-5 were unused, they still get printed)
    public String getAnswerString() {
        String answer = "";
        Literal lit;
        for(int i = 0; i < this.answerLength; i++) {
            lit = this.literals.get(i);
            if(lit == null) {
                // Unused literals get set to false by default
                answer += "F";
            }
            else {
                if(lit.getValue()) {
                    answer+="T";
                }
                else answer +="F";
            }
        }
        return answer;
    }


    public int getNumTautologies() {
        return this.tatuologyCount;
    }

    // Checks the number of clauses that are true with the current set of literal values
    public int checkSolution() {
        int sat = 0;
        Clause c;

        for(int i = 0; i < this.clauses.size(); i++) {
            c = this.clauses.get(i);
            if(c.check()) {
                sat++;
            }
        }
        return sat;
    }

    public HashMap<Integer, Literal> getLiterals() {
        return this.literals;
    }

} // End class Max2SAT

