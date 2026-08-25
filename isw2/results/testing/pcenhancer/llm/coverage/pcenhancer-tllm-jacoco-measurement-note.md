# PCEnhancer T_LLM - JaCoCo Measurement

## Scope

Post-freeze adequacy measurement for the LLM-generated suite `T_LLM` targeting:

`org.apache.openjpa.enhance.PCEnhancer`

Baseline: Apache OpenJPA 4.1.1.

The suite was frozen before this measurement and was not modified using
coverage feedback.

## Test suite

```text
Suite       : T_LLM
Tests       : 30
Failures    : 0
Errors      : 0
Skipped     : 0
Stability   : 5/5 PASS
```

## JaCoCo result

```text
LINE_COVERED    : 119
LINE_MISSED     : 2580
LINE_TOTAL      : 2699
LINE_COVERAGE   : 4.41%

BRANCH_COVERED  : 43
BRANCH_MISSED   : 1174
BRANCH_TOTAL    : 1217
BRANCH_COVERAGE : 3.53%
```

The denominators match the canonical `PCEnhancer` measurement universe:

```text
Lines    : 2699
Branches : 1217
```

## Additional JaCoCo counters

```text
INSTRUCTION_COVERED : 416
INSTRUCTION_MISSED  : 13993
METHOD_COVERED      : 25
METHOD_MISSED       : 138
COMPLEXITY_COVERED  : 40
COMPLEXITY_MISSED   : 753
```

Line Coverage and Branch Coverage remain the primary adequacy metrics for the
project.

## Artifacts

The canonical JaCoCo artifacts for this measurement are:

- `jacoco.csv`
- `jacoco.xml`
- `coverage-summary.csv`
- `measurement-note.md`

No JaCoCo feedback was used to modify the frozen suite.
