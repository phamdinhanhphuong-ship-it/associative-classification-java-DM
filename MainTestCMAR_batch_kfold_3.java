
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import data.StringDataset;
import general.ClassificationAlgorithm;
import general.Evaluator;
import general.OverallResults;

/**
 * Example of how to run the custom SVAC algorithm and evaluate it using k-fold cross-validation.
 * This class implements the research method described without modifying original CMAR code.
 */
public class MainTestCMAR_batch_kfold_3 {
 
    private static final String OUTPUT_REPORT_FOR_TRAINING = "outputReportForTraining_SVAC.txt";
    private static final String OUTPUT_REPORT_ON_TRAINING = "outputReportOnTraining_SVAC.txt";
    private static final String OUTPUT_REPORT_ON_TESTING = "outputReportOnTesting_SVAC.txt";
 
    public static void main(String[] args) throws Exception {
 
        System.out.println("========= Step 1: Read the dataset =========="); 

        String targetClassName = "class"; // The target class name in the dataset
        String datasetPath = fileToPath("chess.txt");
        StringDataset dataset = new StringDataset(datasetPath, targetClassName);
        dataset.printStats();

        System.out.println("\n==== Step 2: Training: Apply the SVAC algorithm to build a model ====");

        // Parameters for the SVAC algorithm based on the paper
        double minSup = 0.5 ;  // Minimum support for rule generation
        double minConf = 0.7;  // Minimum confidence for rule generation
        int delta = 4;         // Coverage threshold for rule pruning (empirically set to 4 in the paper)
        int l = 3;             // Top-l rules to use for voting
        int n = 1;             // Top-n classes to predict (1 for single-class classification) 

        // Create the algorithm instance
        ClassificationAlgorithm algorithmSVAC = new AlgoSVAC(minSup, minConf, delta, l, n); 
        ClassificationAlgorithm algorithmCMAR = new AlgoCMAR(minSup, minConf, delta); // Original CMAR
        ClassificationAlgorithm algorithmSVAC_InfoGain = new AlgoSVAC_InfoGain(minSup, minConf, delta, l, n); // New SVAC with InfoGain

        ClassificationAlgorithm[] algorithms = new ClassificationAlgorithm[]{algorithmSVAC, algorithmCMAR, algorithmSVAC_InfoGain};
 

        // We create an object Evaluator to run the experiment using k-fold cross-validation
        Evaluator experiment = new Evaluator();
        int kFoldCount = 10;

        System.out.println("Running " + kFoldCount + "-fold cross-validation...");
        OverallResults allResults = experiment.trainAndRunClassifiersKFold(algorithms, dataset, kFoldCount);

         // Save statistics about the execution to files
        allResults.saveMetricsResultsToFile(OUTPUT_REPORT_FOR_TRAINING, OUTPUT_REPORT_ON_TRAINING, OUTPUT_REPORT_ON_TESTING);

        // Print statistics to the console
        allResults.printStats();
    } 

    public static String fileToPath(String filename) throws UnsupportedEncodingException, FileNotFoundException {
        URL url = MainTestCMAR_batch_kfold_3.class.getResource(filename);
        if (url == null) {
            throw new FileNotFoundException("File not found: " + filename + 
            ". Please ensure it is in the same directory as the .class files or on the classpath.");
        }
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }
}