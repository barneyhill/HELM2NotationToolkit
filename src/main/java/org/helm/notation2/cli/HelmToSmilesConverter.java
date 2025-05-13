package org.helm.notation2.cli;

import org.helm.notation2.parser.notation.HELM2Notation;
import org.helm.notation2.tools.HELM2NotationUtils;
import org.helm.notation2.tools.SMILES;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Simple CLI tool to convert a file containing HELM notation strings (one per line)
 * to a file containing corresponding canonical SMILES strings (one per line).
 * If conversion fails for a specific HELM string, a blank line is outputted.
 */
public class HelmToSmilesConverter {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar <jar_file> <input_helm_file.txt> <output_smiles_file.txt>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        System.err.println("Input HELM file: " + inputFile);
        System.err.println("Output SMILES file: " + outputFile);

        // Use try-with-resources to ensure files are closed automatically
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            String helmString;
            int lineNum = 0;
            System.err.println("Starting conversion...");

            while ((helmString = reader.readLine()) != null) {
                lineNum++;
                String currentHelm = helmString.trim(); // Handle potential whitespace

                if (currentHelm.isEmpty()) {
                    // Write a blank line for an empty input line
                    writer.write("");
                    writer.newLine();
                    continue; // Skip processing empty lines
                }

                try {
                    // 1. Read and parse the HELM notation for the current line
                    HELM2Notation helmNotation = HELM2NotationUtils.readNotation(currentHelm);

                    // 2. Generate SMILES (using canonical)
                    String smiles = SMILES.getCanonicalSMILESForAll(helmNotation);

                    // 3. Write SMILES to output file
                    writer.write(smiles);
                    writer.newLine();

                } catch (Exception e) {
                    // Catch ANY exception during processing of this specific HELM string
                    System.err.println("Error processing line " + lineNum + " (Input: '" + currentHelm + "'): " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    // Optionally print stack trace for debugging specific line errors
                    // e.printStackTrace(System.err);

                    // Write a blank line to the output file as requested
                    writer.write("");
                    writer.newLine();
                }
            }

            System.err.println("Conversion finished successfully.");

        } catch (IOException e) {
            // Handle errors related to reading/writing files
            System.err.println("Fatal I/O Error: Could not read input file '" + inputFile + "' or write to output file '" + outputFile + "'.");
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(2); // Use a different exit code for file errors
        } catch (Exception e) {
            // Catch any other unexpected errors during setup/teardown
             System.err.println("An unexpected fatal error occurred: " + e.getMessage());
             e.printStackTrace(System.err);
             System.exit(99);
        }
    }
}