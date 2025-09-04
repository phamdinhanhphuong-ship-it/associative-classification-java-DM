import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import data.Dataset;

/**
 * An extension of FPGrowthForCMAR to generate RuleSVAC objects instead of RuleCMAR.
 * This is necessary to include new metrics like lift required by the SVAC algorithm.
 */
public class FPGrowthForSVAC extends FPGrowthForCMAR {

    private final double minSup;

    public FPGrowthForSVAC(Dataset dataset, double minSup, double minConf) {
        super(dataset, minSup, minConf);
        this.minSup = minSup;
    }

    /**
     * Overridden to generate RuleSVAC objects, calculate their lift, and add them
     * to the rule set if they meet the minimum support and confidence thresholds.
     */
    @Override
    protected void generateRules(short[] antecedent, int antecedentLength, long support, Map<Short, Long> counterByKlass) {
        short[] itemsetOutputBuffer = new short[antecedentLength];
        System.arraycopy(antecedent, 0, itemsetOutputBuffer, 0, antecedentLength);
        Arrays.sort(itemsetOutputBuffer, 0, antecedentLength);

        long minSupportRelative = (long) Math.ceil(this.minSup * dataset.getInstances().size());

        for (Entry<Short, Long> entry : counterByKlass.entrySet()) {
            RuleSVAC rule = new RuleSVAC(itemsetOutputBuffer, entry.getKey());
            rule.setSupportAntecedent(support);
            rule.setSupportRule(entry.getValue());
            rule.setSupportKlass(dataset.getMapClassToFrequency().get(rule.getKlass()));
            rule.calculateLift();

            if (rule.getSupportRule() >= minSupportRelative && rule.getConfidence() >= this.minConf)
                rules.add(rule);
        }
    }
}