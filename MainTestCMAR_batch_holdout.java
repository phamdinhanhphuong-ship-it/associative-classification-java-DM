
import java.io.UnsupportedEncodingException;
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
public class MainTestCMAR_batch_holdout {

	public static void main(String[] args) throws Exception {

		System.out.println("========= Step 1: Read the dataset ==========");

		// We choose "play" as the target attribute that we want to predict using other
		// attributes
		String targetClassName = "Class";

		String datasetPath = fileToPath("breastcancerwisconsin.txt");
		StringDataset dataset = new StringDataset(datasetPath, targetClassName);

		dataset.printStats();

		
		// If the dataset is in ARFF format, then use these lines instead:
//		String datasetPath = fileToPath("weather-train.arff");
//		ARFFDataset dataset = new ARFFDataset(datasetPath, targetClassName);
		
		// If the dataset is in CSV format, then use these lines instead:
//		String datasetPath = fileToPath("tennisExtendedCSV.txt");
//		CSVDataset dataset = new CSVDataset(datasetPath, targetClassName);

		// Print stats about the dataset
		
		// For debugging (optional)
//		dataset.printInternalRepresentation();
//		dataset.printStringRepresentation();

		System.out.println("==== Step 2: Training:  Apply the algorithm to build a model (a set of rules) ===");
		// Parameters of the algorithm
		double minSup = 0.01;
		double minConf = 0.5;
		int delta = 2;

		// Create the algorithm
		ClassificationAlgorithm algorithmCMAR = new AlgoCMAR(minSup, minConf, delta);

		// We create an object Evaluator to run the experiment using k-fold cross
		// validation
		Evaluator experiment1 = new Evaluator();

		// The following line indicates that 50% of the dataset will be used for
		// training.
		// The rest of the dataset will be used for testing.
		double percentage = 0.5;

		// We run the experiment
		ClassificationAlgorithm[] algorithms = new ClassificationAlgorithm[] { algorithmCMAR };
		OverallResults allResults = experiment1.trainAndRunClassifiersHoldout(algorithms, dataset, percentage);

		// Save statistics about the execution to files (optional)
		String forTrainingPath = "outputReportForTraining.txt";
		String onTrainingPath = "outputReportOnTraining.txt";
		String onTrestingPath = "outputReportOnTesting.txt";
		allResults.saveMetricsResultsToFile(forTrainingPath, onTrainingPath, onTrestingPath);

		// Print statistics to the console (optional)
		allResults.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestCMAR_batch_holdout.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
