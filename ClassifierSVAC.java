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
 * Implements the SVAC classifier based on the research paper.
 * This involves a custom rule ranking, database coverage pruning,
 * a specific rule scoring model, and a rule voting mechanism for prediction.
 */
public class ClassifierSVAC extends RuleClassifier {
    private static final long serialVersionUID = 1L;
    private final int delta;
    private final int l;
    private final int n;
    private Short defaultClass = NOPREDICTION;
    private final Map<Short, Double> featureWeights;

    public ClassifierSVAC(List<Rule> generatedRules, Dataset training, int delta, int l, int n) {
        super("SVAC");
        this.delta = delta;
        this.l = l;
        this.n = n;

        // Set static variable needed for calculations in RuleCMAR/RuleSVAC
        RuleCMAR.NUMBER_INSTANCES = training.getInstances().size();

        // The paper mentions feature weights, but not how to calculate them.
        // As a placeholder, we are not using any specific weighting (equivalent to all weights being 1).
        // A more advanced implementation could calculate Information Gain here.
        this.featureWeights = new HashMap<>();

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
        // Find all rules that are applicable to the instance
        List<RuleSVAC> applicableRules = new ArrayList<>();
        for (Rule rule : this.rules) {
            if (rule.matching(instance.getItems())) {
                applicableRules.add((RuleSVAC) rule);
            }
        }

        if (applicableRules.isEmpty()) {
            return this.defaultClass;
        }

        // Stage 3 & 4: Rule Scoring and Rule Voting
        // Score each applicable rule
        Map<RuleSVAC, Double> scoredRules = new HashMap<>();
        for (RuleSVAC rule : applicableRules) {
            scoredRules.put(rule, rule.calculateScore(instance.getItems(), this.featureWeights));
        }

        // Elicit top-l matching rules (sorted by score)
        List<Map.Entry<RuleSVAC, Double>> sortedRules = new ArrayList<>(scoredRules.entrySet());
        sortedRules.sort(Map.Entry.<RuleSVAC, Double>comparingByValue().reversed());

        List<Map.Entry<RuleSVAC, Double>> topLRules = sortedRules.subList(0, Math.min(this.l, sortedRules.size()));

        // Group rules by class and aggregate their scores (Rule Voting)
        Map<Short, Double> classScores = new HashMap<>();
        for (Map.Entry<RuleSVAC, Double> entry : topLRules) {
            short klass = entry.getKey().getKlass();
            double score = entry.getValue();
            classScores.put(klass, classScores.getOrDefault(klass, 0.0) + score);
        }

        // Find the class with the highest aggregated score
        if (classScores.isEmpty()) {
            return this.defaultClass;
        }

        // Return the top-n class labels (for n=1)
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
}