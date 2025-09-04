import java.util.ArrayList;
import java.util.List;

import data.Dataset;
import general.ClassificationAlgorithm;
import general.Classifier;
import general.Rule;

/**
 * Implementation of the SVAC (Scoring and Voting for Association Classification) algorithm.
 * This class orchestrates the rule generation and classifier building process.
 */
public class AlgoSVAC extends ClassificationAlgorithm {

    private final double minSup;
    private final double minConf;
    private final int delta;
    private final int l; // For top-l rule voting
    private final int n; // For top-n class prediction

    public AlgoSVAC(double minSup, double minConf, int delta, int l, int n) {
        this.minSup = minSup;
        this.minConf = minConf;
        this.delta = delta;
        this.l = l;
        this.n = n;
    }

    @Override
    public Classifier train(Dataset dataset) {
        // Stage 1: Rule Generation (Reusing from CMAR)
        FPGrowthForCMAR fpgrowth = new FPGrowthForCMAR(dataset, this.minSup, this.minConf);
        List<Rule> generatedRules = fpgrowth.run();

        // Convert RuleCMAR to RuleSVAC and calculate additional metrics
        List<Rule> svacRules = new ArrayList<>();
        for (Rule rule : generatedRules) {
            RuleCMAR cmarRule = (RuleCMAR) rule;

            // Manually convert List<Short> to short[] to match the constructor signature.
            List<Short> antecedentList = cmarRule.getAntecedent();
            short[] antecedentArray = new short[antecedentList.size()];
            for(int i=0; i < antecedentList.size(); i++) {
                antecedentArray[i] = antecedentList.get(i); // Auto-unboxing from Short to short
            }
            RuleSVAC newRule = new RuleSVAC(antecedentArray, cmarRule.getKlass());
            newRule.setSupportAntecedent(cmarRule.getSupportAntecedent());
            newRule.setSupportRule(cmarRule.getSupportRule());
            newRule.setSupportKlass(cmarRule.getSupportKlass());
            newRule.calculateLift();
            svacRules.add(newRule);
        }

        // Stages 2, 3, 4 are handled within the ClassifierSVAC
        return new ClassifierSVAC(svacRules, dataset, this.delta, this.l, this.n);
    }

    @Override
    public String getName() {
        return "SVAC";
    }
}