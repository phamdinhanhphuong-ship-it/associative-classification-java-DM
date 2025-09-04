
import data.Instance;
import data.StringDataset;
import general.Classifier;
import general.RuleClassifier;

/**
 * Example of how to run the CMAR algorithm
 * 
 * @author Philippe Fournier-Viger, 2021
 *
 */
public class MainTestCMAR_single_prediction {

	public static void main(String[] args) throws Exception {

		// ********************************************************
		// **************** READ A DATASET IN MEMORY ************
		// ********************************************************
		System.out.println("========= Step 1: Read the dataset in memory ==========");

		// We choose "play" as the target attribute that we want to predict using the
		// other attributes
		String targetClassName = "play";

		// Load the dataset in memory.
		// If the dataset is in SPMF format:
		// The fileToPath method can sometimes fail depending on the project setup.
		// A more reliable way is to provide a direct path to the file.
		// Make sure "tennisExtended.txt" is in the same folder where you run the program,
		// or provide the full path to it.
		String datasetPath = "tennisExtended.txt";
		StringDataset dataset = new StringDataset(datasetPath, targetClassName);

		// If the dataset is in ARFF format, then use these lines instead:
//		String datasetPath = fileToPath("weather-train.arff");
//		ARFFDataset dataset = new ARFFDataset(datasetPath, targetClassName);
		
		// If the dataset is in CSV format, then use these lines instead:
//		String datasetPath = fileToPath("tennisExtendedCSV.txt");
//		CSVDataset dataset = new CSVDataset(datasetPath, targetClassName);

		// Use the following line to see statistics about the dataset
		dataset.printStats();

		// For debugging we could print the dataset as it is loaded in memory:
//		dataset.printInternalRepresentation();
//		dataset.printStringRepresentation();

		// ********************************************************
		// **************** TRAIN THE MODEL (classifier) **********
		// ********************************************************
		System.out.println();
		System.out
				.println("==== Step 2: Train the model and run automated classification experiments on the dataset===");
		System.out.println();
		// Parameters of the algorithm for training
		double minSup = 0.3;
		double minConf = 0.5;
		int delta = 2;

		// Train the model on the training data and make predictions on the testing data
		Classifier classifier = new AlgoCMAR(minSup, minConf, delta).trainAndCalculateStats(dataset);

		// ****************************************
		// **************** OPTIONAL **************
		// ****************************************
		// If you want to keep the rules of the trained model, you may create
		// a file in SPMF format and put the rules inside.
		String rulesPath = "rulesPath.txt";
		((RuleClassifier) classifier).writeRulesToFileSPMFFormatAsStrings(rulesPath, dataset);
//		((RuleClassifier)classifier).writeRulesToFileSPMFFormatAsNumbers(rulesPath);

		// ****************************************
		// **************** OPTIONAL **************
		// ****************************************
		// If you want to save a trained model so that you can load it into memory
		// later,
		// you can do as follows.
		// First, save the classifier to a file using serialization:
//		System.out.println(" Save the classifier to a file");
//		classifier.saveTrainedClassifierToFile("classifier.ser");

		// Second, you can the classifier into memory:
//		System.out.println(" Read the classifier from a file");
//		classifier = Classifier.loadTrainedClassifierToFile("classifier.ser");

		// ********************************************************
		// ***** USE THE MODEL TO MAKE A PREDICTION **********
		// ********************************************************
		System.out.println(" Making a prediction for the record: {rainy, mild, high, strong, monday, small}");
		Instance instance = dataset
				.stringToInstance(new String[] { "rainy", "mild", "high", "strong", "monday", "small" });
		short result = classifier.predict(instance);
		System.out.println("    The predicted value is: " + dataset.getStringCorrespondingToItem(result));

	}

	// This method is no longer used with the above change and can be removed or commented out.
	/*
	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestCMAR_single_prediction.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}*/
}
