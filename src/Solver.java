package bin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by Armen on 5/21/2017.
 */
public class Solver {
    private Max2SAT instance;
    private int numLiterals;
    private int numClauses;
    private ArrayList<Literal> solution;
    private String fileName, outputFileName;
    private BufferedWriter bufferedWriter;

    public Solver(String fileName, String outputFileName) {
        this.instance = new Max2SAT(fileName);

        this.fileName = fileName;
        this.outputFileName = outputFileName;
        FileWriter fw = null;
        try {
            fw = new FileWriter(this.outputFileName, false);
        } catch (IOException e) {e.printStackTrace();}
        bufferedWriter = new BufferedWriter(fw);

        this.numClauses = instance.getNumClauses();
        this.numLiterals = instance.getNumLiterals();

        this.solution = new ArrayList<Literal>(numLiterals);

        // Copy refrences to the literals from the hashmap into
        // an arraylist. This arraylist is our interface to the literals
        instance.getLiterals().forEach((k, v) -> {
            this.solution.add(v);
        });
    }


    // Writes some analysis-type information about the problem instance to the output file
    public void analyzeInstance() {
        try {
            bufferedWriter.write("-__---____-----______-------________---------__________-----------____________\n");
            bufferedWriter.write(String.format("Finished reading the problem %s\n", fileName ));
            instance.printInstanceAnalysis(bufferedWriter);			
            bufferedWriter.write("_--___----_____------_______--------_________----------___________------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Runs the Compound Local Search algorithm (defined below) and prints the result
    // to the console (also writes it to the output file)
    // Parameters:
    //  number of tries: how many failed attempts should be made before stopping
    //  number of repeats: how many times the algorithm should be run from beginning to end
    //  printIntermediate: whether the intermediate solutions should be printed, or only the final result
    public void printCompoundLocalSearch(int numberOfTries, int numberOfRepeats, boolean printIntermediate) {
        long t1 = System.currentTimeMillis();
        BitSet bestBitSet, currentBitSet;
        int bestValue, currentValue;

        bestBitSet = turnLiteralsToBitSet();
        bestValue = checkValue();

        for(int i = 0; i < numberOfRepeats; i++) {
            // Run the compoundLocalSearch algorithm
            currentBitSet = compoundLocalSearch(numberOfTries, printIntermediate);
            updateLiteralsFromBitSet(currentBitSet);
            // Check the value of compoundLocalSearch solution
            currentValue = checkValue();
            // If that value is better than the current best, update the current best
            if(currentValue > bestValue) {
                bestValue = currentValue;
                bestBitSet = currentBitSet;
                if(printIntermediate) { printSolution(); System.out.println(); }
            }
        }
        updateLiteralsFromBitSet(bestBitSet);

        long t2 = System.currentTimeMillis();
        String str = NumberFormat.getIntegerInstance().format(t2-t1);
        System.out.println(String.format("Process took %sms", str ));
        try {
            bufferedWriter.write("Compound Local Search Algorithm Result:\n");
            printSolution();
        } catch (IOException e) {e.printStackTrace();}
    }

    /**********************************************************************************************
     * Local Search - Methodical
     * A solution is a BitSet representing the literals that are actually used
     * A neighbor is a solution that differs by some number of bits (this is the parameter: width)
     * Move condition - move to the best neighbor
     * Stop condition - stop when no neighbors are better
     **********************************************************************************************/
    private BitSet localSearchMethodical(int width, boolean printIntermediate) {
        int globalMaxValue, neighborhoodMaxValue, currentValue;
        BitSet globalMaxSolution, neighborhoodMaxSolution, currentSolution;

        globalMaxSolution = turnLiteralsToBitSet();
        globalMaxValue = checkValue();

        boolean foundBetterNeighbor = true;

        while(foundBetterNeighbor) {
            foundBetterNeighbor = false;

            // Look through the neighborhood for the best solution
            neighborhoodMaxSolution = checkNeighborhood(width);

            // Check the value of that solution
            updateLiteralsFromBitSet(neighborhoodMaxSolution);
            neighborhoodMaxValue = checkValue();

            if(globalMaxValue < neighborhoodMaxValue) {
                foundBetterNeighbor = true;
                globalMaxSolution = neighborhoodMaxSolution;
                globalMaxValue = neighborhoodMaxValue;
                if(printIntermediate) {
                    printSolution();
                }
            }
        } // End while(foundBetterNeighbor)
        return globalMaxSolution;
    }

    /**********************************************************************************************
     * Local Search - Jumpy
     * A solution is a BitSet representing the literals that are actually used
     * A neighbor is a solution that differs by some number of bits (this is the parameter: width)
     * Move condition - move to the first neighbor that is better
     * Stop condition - stop when no neighbors are better
     **********************************************************************************************/
    private BitSet localSearchJumpy(boolean printIntermediate) {
        int neighborhoodMaxValue, currentValue;

        neighborhoodMaxValue = checkValue();

        boolean betterNeighbor = true;
        while(betterNeighbor) {
            betterNeighbor = false;
            for(int i = 0; i < numLiterals; i++) {
                // flip a bit
                flip(i);
                currentValue = checkValue();

                // If it produces a better value, great
                if(neighborhoodMaxValue  < currentValue) {
                    neighborhoodMaxValue = currentValue;
                    // Continue as long as a better neighbor has been found
                    betterNeighbor = true;
                    if(printIntermediate) {printSolution();}
                }
                // If it doesn't flip it back
                else { flip(i); }

            }// End for(i)
        } // End while
        return turnLiteralsToBitSet();
    }


    /*******************************************************************
     * Compound Local Search Algorithm
     * Take a random solution
	 * Run the jumpy local search algorithm on it
	 * If its value is better than the current max, save it
	 * If it's not, then you lose a try (think of it as lives in a video game)
	 * When you run out of tries, take the best solution, and do a full (methodical) 
	 * local search (width 2) of that solution
    *******************************************************************/
    private BitSet compoundLocalSearch(int numTries, boolean printIntermediate) {

        BitSet bestSolution, currentSolution;
        int bestValue, currentValue;

        // Start with a random BitSet
        currentSolution = randomBitSet();
        updateLiteralsFromBitSet(currentSolution);

        bestSolution = turnLiteralsToBitSet();
        bestValue = checkValue();

        // How many times the algorithm can try again after not finding a better solution
        int extraTries = numTries;
        boolean foundBetter = true;

        while(foundBetter || extraTries > 0) {
            foundBetter = false;

            updateLiteralsFromBitSet(randomBitSet());
            currentSolution = localSearchJumpy(printIntermediate);
            currentValue = checkValue();


            if(currentValue > bestValue) {
                // If this value is the best one yet, run local search on it
                bestSolution = currentSolution;
                bestValue = currentValue;

                foundBetter = true;
                if(printIntermediate) {printSolution();}
            }
            // If this solution wasn't better
            else {
                // You lose one of your tries
                extraTries--;
                if(printIntermediate) {System.out.println("Tries left: " + extraTries); }
            }

        }// End While
        updateLiteralsFromBitSet(bestSolution);
        bestSolution = localSearchMethodical(2, printIntermediate);
        return bestSolution;
    }


    /****************************************************************************************
     * Check Neighborhood
     * Looks at all the neighbors (defined above under Local Search) of the current solution
     * Recursive function - if width is greater than 1, it calls itself to find the best neighbor
     *   in a wider neighborhood (example: all solutions that are off by 2 bits)
     * Returns the bitset of the highest-value neighbor found.
     ****************************************************************************************/
    private BitSet checkNeighborhood(int width) {
        BitSet neighborhoodMaxSolution, currentSolution;
        int neighborhoodMaxValue, currentValue;

        neighborhoodMaxSolution = turnLiteralsToBitSet();

        neighborhoodMaxValue = 0;
        for(int i = 0; i < numLiterals; i++) {

            // flip the ith bit
            flip(i);

            // If doing a wide search, recurse
            if(width > 1) {
                currentSolution = checkNeighborhood(width - 1);
                updateLiteralsFromBitSet(currentSolution);
            }

            currentValue = checkValue();

            // If this solution is better than the best in the neighborhood
            if(neighborhoodMaxValue < currentValue) {
                neighborhoodMaxValue = currentValue;
                neighborhoodMaxSolution = turnLiteralsToBitSet();
            }
            // Flip the ith bit back so we can check the next one
            flip(i);
        } // End for(i)
        return neighborhoodMaxSolution;
    }


    //====================== Helper Functions 1 =========================//
    // Check the value generated from the current state of the literals
    public int checkValue() {
        return instance.checkSolution();
    }

    // Print the state of all the literals followed by the value
    // Also write it to the output file
    public void printSolution() {
        String str = String.format(
                "%s %d\n"
                ,this.instance.getAnswerString()
                , this.checkValue()
        );
        System.out.print(str);

        try {
            bufferedWriter.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=- Helper Functions 2 =-=-=-=-=-=-=-=-=-=-=-=-=-=-=//
    // Converting between BitSets and the literals in the ArrayList

    // Takes a BitSet object as a parameter, and uses it to set the
    // literal values in the arraylist
    private void updateLiteralsFromBitSet(BitSet b) {
        for(int i = 0; i < numLiterals; i++) {
            make(i, b.get(i));
        }
    }

    // Returns a BitSet object with the bits set randomly
    private BitSet randomBitSet() {
        BitSet b = new BitSet(numLiterals);
        Random randomGenerator = new Random(System.currentTimeMillis());

        for(int i = 0; i < numLiterals; i++) {
            b.set(i, randomGenerator.nextBoolean());
        }
        return b;
    }

    // Returns a BitSet object based on the current state of the literals
    private BitSet turnLiteralsToBitSet() {
        BitSet b = new BitSet(numLiterals);
        for(int i = 0; i < numLiterals; i++) {
            b.set(i, this.solution.get(i).getValue());
        }
        return b;
    }

    // --------------------- Helper Functions  3 --------------------//

    // Set, reset, flip, or make the ith bit
    // set means set it to true
    private void set(int bit) {
        this.solution.get(bit).set();
    }

    // reset means set it to false
    private void reset(int bit) {
        this.solution.get(bit).reset();
    }

    // flip means set it to is opposite
    private void flip(int bit) {
        this.solution.get(bit).flip();
    }

    // make(b) means set it to b
    private void make(int bit, boolean b) {
        this.solution.get(bit).make(b);
    }

    public int getNumLiterals() {
        return this.numLiterals;
    }

    public int getNumClauses() {
        return this.numClauses;
    }

    public int getNumTautologies() {
        return this.instance.getNumTautologies();
    }



    // Close the output file
    public void closeFile() {
        try {
            this.bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} // End class Solver




