/*
 * This file is part of SPMF data mining library.
 * It is adapted from some GPL code obtained from the LAC library, which used some SPMF code.
 *
 * Copyright (C) SPMF, LAC
 *   
 * LAC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. You should have 
 * received a copy of the GNU General Public License along with 
 * this program.  If not, see http://www.gnu.org/licenses/
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import data.Dataset;
import data.Instance;
import general.Rule;
import general.RuleClassifier;

/**
 * Generates classifier by means of the previously obtained rules. It sorts the
 * rules by confidence, support and size. Then, general rules are only
 * considered removing low confidence and no general rules. Finally, only
 * positively correlated rules are considered
 * 
 * @see AlgoCMAR
 */
public class ClassifierCMAR extends RuleClassifier {
	
    /**
	 * UID
	 */
	private static final long serialVersionUID = 346166758556004366L;

	/**
     * Constructor
     * 
     * @param rules    forming the current classifier
     * @param training dataset used while training classifier
     * @param delta  delta
     * 
     * @throws Exception
     */
    public ClassifierCMAR(List<Rule> rules, Dataset training, int delta) {
        super("CMAR");
        RuleCMAR.NUMBER_INSTANCES = training.getInstances().size();
        CRTree.NUMBER_SINGLETONS = training.getDistinctItemsCount();
        CRTree crTree = new CRTree(training, delta);
        for (Rule rule : rules) {
            crTree.insert(rule);
        }
        crTree.pruneUsingCover();
        this.rules = crTree.getRules();
        
        // Pre-calculate scores for each rule after pruning
        for (Rule rule : this.rules) {
            ((RuleCMAR) rule).preCalculateScore();
        }
    }

    @Override
    public short predict(Instance instance) {
        // Find all rules that match the given instance
        List<RuleCMAR> matchingRules = obtainAllRulesForRecord(instance.getItems());

        // If no rules match the instance, no prediction can be made.
        if (matchingRules.isEmpty()) {
            return NOPREDICTION;
        }

        // The rules in the CRTree are already sorted by confidence, support, and size.
        // Therefore, the first rule in the list of matching rules is the "best" one.
        // Now we choose the rule with the highest pre-calculated score (although original sorting should make this first).
        RuleCMAR bestRule = matchingRules.get(0);
        for (int i = 1; i < matchingRules.size(); i++) {
            if (matchingRules.get(i).getPreCalculatedScore() > bestRule.getPreCalculatedScore()) {
                bestRule = matchingRules.get(i);
            }
        }
        // We use the class of the best rule (highest score) for the prediction.
        return bestRule.getKlass();
     }

    /**
     * Obtains all rules which are fired for current example
     * 
     * @param example to check rules
     * @return list of rules fired with current example
     */
    private List<RuleCMAR> obtainAllRulesForRecord(Short[] example) {
        List<RuleCMAR> result = new ArrayList<RuleCMAR>();
        for (Rule rule : rules) {
            if (rule.matching(example)) {
                result.add((RuleCMAR)rule);
            }
        }
        return result;
    }
}
