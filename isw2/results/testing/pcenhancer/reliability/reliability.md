# PCEnhancer - Reliability Estimation

## Objective

Estimate the empirical reliability of
org.apache.openjpa.enhance.PCEnhancer using the final manually evolved
test suite.

## Final test suite

The reliability experiment uses the complete final manual suite:

- T_BB: 30 tests
- T_CF: 5 tests
- T_MT: 5 tests
- Total: 40 tests

The suite was executed again specifically for the reliability experiment.

## Operational profile

A uniform operational profile is assumed over the complete final test set.

N = 40

Probability assigned to every test:

p_i = 1 / 40 = 0.025000000000

The probabilities sum to 1.

## Reliability estimator

For each test:

success_i = 1 when the test passes
success_i = 0 otherwise

The empirical reliability estimate is:

R_hat = SUM(p_i * success_i)

With a uniform operational profile:

R_hat = passed_tests / total_tests

The estimated failure probability is:

Q_hat = 1 - R_hat

## Experimental result

- T_BB tests: 30
- T_CF tests: 5
- T_MT tests: 5
- Total tests: 40
- Passed: 40
- Failures: 0
- Errors: 0
- Skipped: 0
- Probability per test: 0.025000000000
- Reliability estimate: 1.000000
- Estimated failure probability: 0.000000

## Interpretation

The obtained value is an empirical reliability estimate relative to the
explicitly defined finite uniform operational profile represented by the
final manual test suite.

It must not be interpreted as evidence that PCEnhancer is perfectly reliable
for every possible execution in production.

Coverage and mutation testing remain complementary adequacy measures and are
reported separately.

## Evidence

- operational-profile.csv
- reliability-summary.csv

Commit: 333c5af055fb5283d00151b63339ce0489c91c5b