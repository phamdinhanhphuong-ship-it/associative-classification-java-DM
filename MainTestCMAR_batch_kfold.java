
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.net.URL;

//import AlgoCMAR;
import data.StringDataset;
import general.ClassificationAlgorithm;
import general.Evaluator;
import general.OverallResults;

/**
 * Example of how to run the CMAR algorithm
 * 
 * @author Philippe Fournier-Viger, 2021
 *
 */
public class MainTestCMAR_batch_kfold {

	// Constants for output files to improve readability and correct a minor typo.
	private static final String OUTPUT_REPORT_FOR_TRAINING = "outputReportForTraining.txt";
	private static final String OUTPUT_REPORT_ON_TRAINING = "outputReportOnTraining.txt";
	private static final String OUTPUT_REPORT_ON_TESTING = "outputReportOnTesting.txt";

	public static void main(String[] args) throws Exception {

		System.out.println("========= Step 1: Read the dataset ==========");

		// We choose "play" as the target attribute that we want to predict using other
		    String targetClassName = "poisonous";
        String datasetPath = fileToPath("mushrooms.txt");
		StringDataset dataset = new StringDataset(datasetPath, targetClassName);

		// If the dataset is in ARFF format, then use these lines instead:
//		String datasetPath = fileToPath("weather-train.arff");
//		ARFFDataset dataset = new ARFFDataset(datasetPath, targetClassName);
		
		// If the dataset is in CSV format, then use these lines instead:
//		String datasetPath = fileToPath("tennisExtendedCSV.txt");
//		CSVDataset dataset = new CSVDataset(datasetPath, targetClassName);

		// Print stats about the dataset
		dataset.printStats();

		// For debugging (optional)
//		dataset.printInternalRepresentation();
//		dataset.printStringRepresentation();

		System.out.println("==== Step 2: Training:  Apply the algorithm to build a model (a set of rules) ===");
		// Parameters of the algorithm
		double minSup = 0.05;
		double minConf = 0.5;
		int delta = 2;

		// Create the algorithm
		ClassificationAlgorithm algorithmCMAR = new AlgoCMAR(minSup, minConf, delta);
		ClassificationAlgorithm algorithmVotingCMAR = new AlgoVotingCMAR(minSup, minConf, delta);
		//ClassificationAlgorithm algorithmCMARInfoGain = new AlgoCMARInformationGain(minSup, minConf, delta); // Create new algorithm
		ClassificationAlgorithm[] algorithms = new ClassificationAlgorithm[] { algorithmCMAR, algorithmVotingCMAR, algorithmCMARInfoGain }; // Add it to the array

		// We create an object Evaluator to run the experiment using k-fold cross
		// validation
		Evaluator experiment1 = new Evaluator();

		// We will test 3 folds
		int kFoldCount = 3;

		// We run the experiment
		OverallResults allResults = experiment1.trainAndRunClassifiersKFold(algorithms, dataset, kFoldCount);

		// Save statistics about the execution to files (optional)
		allResults.saveMetricsResultsToFile(OUTPUT_REPORT_FOR_TRAINING, OUTPUT_REPORT_ON_TRAINING, OUTPUT_REPORT_ON_TESTING);

		// Print statistics to the console (optional)
		allResults.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException, FileNotFoundException {
		URL url = MainTestCMAR_batch_kfold.class.getResource(filename);
		if (url == null) {
			throw new FileNotFoundException("File not found: " + filename + 
			". Please ensure it is in the classpath or provide a direct path.");
		}
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
