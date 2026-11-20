# Associative Classification in Java: CMAR & SVAC

![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=openjdk&logoColor=white)
![Data Mining](https://img.shields.io/badge/Data%20Mining-8B5CF6?style=flat-square&logoColor=white)
![Course · DM](https://img.shields.io/badge/Course%20%C2%B7%20DM-2563EB?style=flat-square)

Java implementations of associative-classification algorithms that mine class-association
rules with FP-growth and classify new instances by rule voting.

## Implemented Algorithms

| Algorithm | Description |
|---|---|
| **CMAR** | *Classification based on Multiple Association Rules* (Li, Han & Pei, ICDM 2001). Mines class-association rules with FP-growth, stores them in a CR-tree, prunes redundant/low-quality rules, and classifies with weighted rule voting (`ClassifierCMAR`, `FPGrowthForCMAR`, `CRNode`, `CRTree`). |
| **SVAC** | Associative classifier with parameters `minSup`, `minConf`, `delta`, plus **top-l rule voting** and **top-n class prediction** (`AlgoSVAC`, `ClassifierSVAC`, `FPGrowthForSVAC`). |
| **SVAC + InfoGain** | SVAC variant that scores rules by information gain (`AlgoSVAC_InfoGain`, `ClassifierSVAC_InfoGain`). |

## Datasets

Experiments are included on classic categorical datasets: Wisconsin breast cancer, car,
chess, iris, mushrooms, retail, tic-tac-toe and tennis (extended) — see the `*.txt` files
in the repository root.

Sample outputs of mined and pruned rule sets are checked in
(`CMAR_rules_prunning*.txt`, `outputReport*.txt`).

## Build & Run

Plain Java sources with compiled `.class` files — no external dependencies.
Compile and run the classifier entry points, e.g.:

```bash
javac *.java
java ClassifierCMAR
java ClassifierSVAC
```

> Course project — Data Mining (DM)
