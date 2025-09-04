import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a rule for the SVAC algorithm, extending RuleCMAR.
 * It includes calculations for lift, geometric mean, and the new scoring model
 * as described in the research paper. It also implements a custom comparison
 * method for ranking.
 */
public class RuleSVAC extends RuleCMAR implements Comparable<RuleSVAC> {
    private static final long serialVersionUID = 1L;
    private double lift;

    public RuleSVAC(short[] antecedent, short klass) {
        super(antecedent, klass);
    }

    /**
     * Calculates the lift of the rule. Lift(A -> C) = Conf(A -> C) / Supp(C).
     */
    public void calculateLift() {
        if (getSupportKlass() == 0 || NUMBER_INSTANCES == 0) {
            this.lift = 0;
            return;
        }
        double supportOfConsequent = (double) getSupportKlass() / NUMBER_INSTANCES;
        if (supportOfConsequent == 0) {
            this.lift = 0;
        } else {
            this.lift = getConfidence() / supportOfConsequent;
        }
    }

    public double getLift() {
        return lift;
    }

    /**
     * Calculates the geometric mean of confidence and support.
     * @return The geometric mean value.
     */
    public double getGeometricMean() {
        if (NUMBER_INSTANCES == 0) {
            return 0;
        }
        double relativeSupport = (double) getSupportRule() / NUMBER_INSTANCES;
        return Math.sqrt(getConfidence() * relativeSupport);
    }

    /**
     * Compares this rule with another for sorting based on the research paper's criteria:
     * 1. Specificity (LHS size, descending)
     * 2. Geometric mean of support and confidence (descending)
     * 3. Lift (descending)
     */
    @Override
    public int compareTo(RuleSVAC other) {
        // 1. Specificity (number of features on the LHS, descending)
        if (this.getAntecedent().size() != other.getAntecedent().size()) {
            return Integer.compare(other.getAntecedent().size(), this.getAntecedent().size());
        }
        // 2. Geometric mean (descending)
        double geoMeanThis = this.getGeometricMean();
        double geoMeanOther = other.getGeometricMean();
        int geoMeanCompare = Double.compare(geoMeanOther, geoMeanThis);
        if (geoMeanCompare != 0) {
            return geoMeanCompare;
        }
        // 3. Lift (descending)
        return Double.compare(other.getLift(), this.getLift());
    }

    /**
     * Calculates the rule's score for a given instance based on the formula:
     * Sk = sqrt(Confk * Suppk) * e^(-(sum of weights of mismatched features))
     */
    public double calculateScore(Short[] instanceItems, Map<Short, Double> featureWeights) {
        Set<Short> ruleAntecedent = new HashSet<>(getAntecedent());
        double penalty = 0.0;
        // The penalty is the sum of weights of features present in the instance but NOT in the rule's antecedent.
        for (Short item : instanceItems) {
            if (!ruleAntecedent.contains(item)) {
                penalty += featureWeights.getOrDefault(item, 1.0); // Assume weight 1.0 if not provided
            }
        }
        return getGeometricMean() * Math.exp(-penalty);
    }

    @Override
    public String getMeasuresToString() {
        return super.getMeasuresToString() + " #LIFT: " + String.format("%.4f", getLift());
    }
}