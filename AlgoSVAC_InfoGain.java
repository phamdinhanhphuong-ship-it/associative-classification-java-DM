import java.util.ArrayList;
import java.util.List;

import data.Dataset;
import general.ClassificationAlgorithm;
import general.Classifier;
import general.Rule;

/**
 * An implementation of the SVAC algorithm that incorporates Information Gain
 * for feature weighting. This class orchestrates the rule generation and
 * passes the data to the ClassifierSVAC_InfoGain for model building.
 */
public class AlgoSVAC_InfoGain extends ClassificationAlgorithm {

    private final double minSup;
    private final double minConf;
    private final int delta;
    private final int l; // For top-l rule voting
    private final int n; // For top-n class prediction

    public AlgoSVAC_InfoGain(double minSup, double minConf, int delta, int l, int n) {
        this.minSup = minSup;
        this.minConf = minConf;
        this.delta = delta;
        this.l = l;
        this.n = n;
    }

    @Override
    public Classifier train(Dataset dataset) {
        FPGrowthForCMAR fpgrowth = new FPGrowthForCMAR(dataset, this.minSup, this.minConf);
        List<Rule> generatedRules = fpgrowth.run();

        List<Rule> svacRules = new ArrayList<>();
        for (Rule rule : generatedRules) {
            RuleCMAR cmarRule = (RuleCMAR) rule;
            RuleSVAC newRule = new RuleSVAC(cmarRule.getAntecedentAsArray(), cmarRule.getKlass());
            newRule.setSupportAntecedent(cmarRule.getSupportAntecedent());
            newRule.setSupportRule(cmarRule.getSupportRule());
            newRule.setSupportKlass(cmarRule.getSupportKlass());
            newRule.calculateLift();
            svacRules.add(newRule);
        }

        return new ClassifierSVAC_InfoGain(svacRules, dataset, this.delta, this.l, this.n);
    }

    @Override
    public String getName() {
        return "SVAC_InfoGain";
    }
}