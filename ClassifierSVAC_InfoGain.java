import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import data.Dataset;
import data.Instance;
import general.Rule;
import general.RuleClassifier;

/**
 * An enhanced version of the SVAC classifier that calculates and uses
 * feature weights based on the Information Gain metric. This allows the model
 * to penalize mismatches on more important features more heavily.
 */
public class ClassifierSVAC_InfoGain extends RuleClassifier {
    private static final long serialVersionUID = 1L;
    private final int delta;
    private final int l;
    private final int n;
    private Short defaultClass = NOPREDICTION;
    private final Map<Short, Double> featureWeights;

    public ClassifierSVAC_InfoGain(List<Rule> generatedRules, Dataset training, int delta, int l, int n) {
        super("SVAC_InfoGain");
        this.delta = delta;
        this.l = l;
        this.n = n;

        RuleCMAR.NUMBER_INSTANCES = training.getInstances().size();

        // Calculate feature weights using Information Gain
        this.featureWeights = calculateFeatureWeightsByInfoGain(training);

        // Stage 2: Classifier Building
        // 1. Rank rules according to SVAC criteria
        List<RuleSVAC> svacRules = generatedRules.stream()
                .map(r -> (RuleSVAC) r)
                .collect(Collectors.toList());
        Collections.sort(svacRules);

        // 2. Prune using Database Coverage method
        List<Instance> remainingInstances = new ArrayList<>(training.getInstances());
        List<Rule> classifierRules = new ArrayList<>();

        for (RuleSVAC rule : svacRules) {
            if (remainingInstances.isEmpty()) {
                break;
            }

            List<Instance> correctlyClassified = new ArrayList<>();
            for (Instance inst : remainingInstances) {
                if (rule.matching(inst.getItems()) && rule.getKlass() == inst.getKlass()) {
                    correctlyClassified.add(inst);
                }
            }

            if (correctlyClassified.size() >= this.delta) {
                classifierRules.add(rule);
                remainingInstances.removeAll(correctlyClassified);
            }
        }
        this.rules = classifierRules;

        // 3. Set Default Class from remaining instances
        if (!remainingInstances.isEmpty()) {
            this.defaultClass = findMostFrequentClass(remainingInstances);
        } else if (!training.getInstances().isEmpty()) {
            this.defaultClass = findMostFrequentClass(training.getInstances());
        }
    }

    @Override
    public short predict(Instance instance) {
        List<RuleSVAC> applicableRules = new ArrayList<>();
        for (Rule rule : this.rules) {
            if (rule.matching(instance.getItems())) {
                applicableRules.add((RuleSVAC) rule);
            }
        }

        if (applicableRules.isEmpty()) {
            return this.defaultClass;
        }

        Map<RuleSVAC, Double> scoredRules = new HashMap<>();
        for (RuleSVAC rule : applicableRules) {
            scoredRules.put(rule, rule.calculateScore(instance.getItems(), this.featureWeights));
        }

        List<Map.Entry<RuleSVAC, Double>> sortedRules = new ArrayList<>(scoredRules.entrySet());
        sortedRules.sort(Map.Entry.<RuleSVAC, Double>comparingByValue().reversed());

        List<Map.Entry<RuleSVAC, Double>> topLRules = sortedRules.subList(0, Math.min(this.l, sortedRules.size()));

        Map<Short, Double> classScores = new HashMap<>();
        for (Map.Entry<RuleSVAC, Double> entry : topLRules) {
            short klass = entry.getKey().getKlass();
            double score = entry.getValue();
            classScores.put(klass, classScores.getOrDefault(klass, 0.0) + score);
        }

        if (classScores.isEmpty()) {
            return this.defaultClass;
        }

        return classScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    private short findMostFrequentClass(List<Instance> instances) {
        if (instances == null || instances.isEmpty()) {
            return NOPREDICTION;
        }

        Map<Short, Long> classFrequencies = instances.stream()
                .collect(Collectors.groupingBy(Instance::getKlass, Collectors.counting()));

        return classFrequencies.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    /**
     * Calculates feature weights using the Information Gain metric.
     * The gain of an attribute is assigned as the weight to all of its items (values).
     * @param training The training dataset.
     * @return A map where the key is the item (feature) and the value is its weight.
     */
    private Map<Short, Double> calculateFeatureWeightsByInfoGain(Dataset training) {
        Map<Short, Double> weights = new HashMap<>();
        int totalInstances = training.getInstances().size();
        if (totalInstances == 0) {
            return weights;
        }

        double totalEntropy = calculateEntropyForInstances(training.getInstances());

        for (data.Attribute attribute : training.getAttributes()) {
            double attributeWeightedEntropy = 0.0;

            for (short itemValue : training.getAttributeValuesAsShort(attribute)) {
                List<Instance> subset = training.getInstances().stream()
                        .filter(inst -> {
                            for (short itemInInstance : inst.getItems()) {
                                if (itemInInstance == itemValue) return true;
                            }
                            return false;
                        })
                        .collect(Collectors.toList());

                if (!subset.isEmpty()) {
                    double subsetEntropy = calculateEntropyForInstances(subset);
                    double proportion = (double) subset.size() / totalInstances;
                    attributeWeightedEntropy += proportion * subsetEntropy;
                }
            }

            double infoGain = totalEntropy - attributeWeightedEntropy;

            for (short itemValue : training.getAttributeValuesAsShort(attribute)) {
                weights.put(itemValue, infoGain);
            }
        }
        return weights;
    }

    /**
     * Helper method to calculate the entropy for a given list of instances.
     * Entropy(S) = -sum(p_i * log2(p_i))
     */
    private double calculateEntropyForInstances(List<Instance> instances) {
        if (instances.isEmpty()) {
            return 0.0;
        }

        Map<Short, Long> classCounts = instances.stream()
                .collect(Collectors.groupingBy(Instance::getKlass, Collectors.counting()));

        return classCounts.values().stream()
                .mapToDouble(count -> (double) count / instances.size())
                .filter(p -> p > 0)
                .map(p -> -p * (Math.log(p) / Math.log(2)))
                .sum();
    }
}