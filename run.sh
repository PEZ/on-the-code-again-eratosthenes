#!/bin/bash

RUNS=$(if [ -z "$1" ]; then echo 1; else echo $1; fi)
for ((i = 0; i < RUNS ; i++))
do
  echo "run: ${i}"
  clojure -X on-the-code-again.eratosthenes/run :variant :set :warm-up? true
  clojure -X on-the-code-again.eratosthenes/run :variant :vector :warm-up? true
  clojure -X on-the-code-again.eratosthenes/run :variant :vector-transient :warm-up? true
  clojure -X on-the-code-again.eratosthenes/run :variant :boolean-array :warm-up? true
  clojure -X on-the-code-again.eratosthenes/run :variant :boolean-array-help-compiler :warm-up? true
done
