
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;

import data.Dataset;
import general.ClassificationAlgorithm;
import general.Classifier;
import general.Rule;

/**
 * This is an implementation of the CMAR algorithm. CMAR is an algorithm for classification
 * based on association rules, proposed in this paper: <br\><br\>
 * 
 * Li, J. Han, and J. Pei, Cmar:
 * Accurate and efficient classification based on multiple class-association
 * rules,in 2002 IEEE International Conference on DataMining(ICDM01), 2001,
 * pp. 369-376
 */
public class AlgoCMAR extends ClassificationAlgorithm {
	

	/**  minimum support */
	double minSup;
	
	/**  minimum confience */
	double minConf;
	
	/** delta */
	int delta;
	
    /**
     * Default constructor
     * @param minsup minimum support
     * @param minconf minimum confidence
     * @param delta delta
     */
    public AlgoCMAR(double minSup, double minConf, int delta) {
    	this.minSup = minSup;   // 0.01
    	this.minConf = minConf;  // 0.5
    	this.delta = delta;   // 4
    }
    

    @Override
	public String getName() {
		return "CMAR";
	}

    /**
     * Train a classifier
     * @param dataset a training dataset
     * @return a rule classifier
     * @throws Exception if an error occurs
     */
    @Override
    public ClassifierCMAR train(Dataset dataset){
    	// Apply a modified FPGrowth algorithm to obtain the rules
        FPGrowthForCMAR fpgrowth = new FPGrowthForCMAR(dataset, minSup, minConf);
        List<Rule> rules = fpgrowth.run();
        printPeakHeapUsage();
        // Return a classifier that is created using these rules
        ClassifierCMAR classifier = new ClassifierCMAR(rules, dataset, delta);
        classifier.writeRulesToFileSPMFFormatAsNumbers("CMAR_rules_prunning.txt");
        return classifier;
    }
    
    public Classifier getClassifier() {
    	return this.classifier;
    }

    
    public static void printPeakHeapUsage()	// print out the peak heap usage
    {
    	long MEGABYTES = 1024L * 1024L;
    	try {
            List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
            // we print the result in the console
			double total = 0;
			for (MemoryPoolMXBean memoryPoolMXBean : pools) {
				if (memoryPoolMXBean.getType() == MemoryType.HEAP) {
					long peakUsed = memoryPoolMXBean.getPeakUsage().getUsed();
					//System.out.println(String.format("Peak used for: %s is %.2f", memoryPoolMXBean.getName(), (double)peakUsed/MEGABYTES));
					total = total + peakUsed;
				}
			}
			System.out.println(String.format("Total heap peak used: %f MB", total / MEGABYTES));
       } catch (Throwable t) { System.err.println("Exception in agent: " + t); }
    }

	public double peakHeapUsage()	// return the peak heap usage. 
	{

    	long MEGABYTES = 1024L * 1024L;
		double retVal = 0;
		try {
			List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
			double total = 0;
			for (MemoryPoolMXBean memoryPoolMXBean : pools) {
				if (memoryPoolMXBean.getType() == MemoryType.HEAP) {
					long peakUsed = memoryPoolMXBean.getPeakUsage().getUsed();
					total = total + peakUsed;
				}
			}
			retVal = total / MEGABYTES;
		} catch (Throwable t) { System.err.println("Exception in agent: " + t); }
		return retVal;
	}	
}