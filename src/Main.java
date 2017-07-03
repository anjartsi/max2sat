package bin;

import java.awt.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String inputFile, outputFile;
        
		if(args.length > 0) { inputFile = args[0]; }
		else { inputFile = "test1.txt"; }
		
		if(args.length > 1) { outputFile = args[1]; }
        else { outputFile = "output.txt"; }
		
		inputFile = System.getProperty("user.dir")+"\\input_files\\"+ inputFile;
		outputFile = System.getProperty("user.dir")+"\\output_files\\"+ outputFile;

	    Solver s = new Solver(inputFile, outputFile);
		

        int numTries = 10;
        int repeats = 2;
        boolean printIntermediate = false;
		
		if(args.length > 2 && args[2].equals("print")) { printIntermediate = true; } 
		
		
        s.analyzeInstance();

        System.out.println("Running Compound Local Search");
        s.printCompoundLocalSearch(numTries, repeats, printIntermediate);

        s.closeFile();
    }
}
