(ns on-the-code-again.eratosthenes
  (:require [clojure.set])
  (:import [java.time Instant Duration]))


(defn sieve [input]
  (let [numbers (set (range 2 (inc input)))
        limit (Math/sqrt input)]
    (reduce (fn [acc n]
              (if (> n limit)
                acc
                (let [multiples (set (range (* 2 n) (inc input) n))]
                  (clojure.set/difference acc multiples)))) numbers numbers)))

(comment
  (sieve 1)
  (sieve 2)
  (sieve 3)
  (sieve 10)
  (sieve 100)
  (count (sieve 100))
  (time (sieve 1000000))
  (time (count (sieve 1000000)))
  (require '[criterium.core :refer [quick-bench with-progress-reporting]])
  (quick-bench (count (sieve 1000000)))
  (with-progress-reporting (quick-bench (count (sieve 1000000))))
  )

(comment
  (require '[clj-async-profiler.core :as prof])
  (prof/serve-files 8080))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def prev-results
  "Previous results to check against sieve results."
  {1           0
   10          4
   100         25
   1000        168
   10000       1229
   100000      9592
   1000000     78498
   10000000    664579
   100000000   5761455
   1000000000  50847534
   10000000000 455052511})

(defn benchmark
  "Benchmark Sieve of Eratosthenes algorithm."
  [sieve]
  (let [limit       1000000
        start-time  (Instant/now)
        end-by      (+ (.toEpochMilli start-time) 5000)]
    (loop [pass 1]
      (let [primes   (sieve limit)
            cur-time (System/currentTimeMillis)]
        (if (<= cur-time end-by)
          (recur (inc pass))
          ;; Return benchmark report.
          {:primes primes
           :passes pass
           :limit  limit
           :time   (Duration/between start-time (Instant/now))})))))


;; Reenable overflow checks on mathematical ops and turn off warnings.
(set! *warn-on-reflection* false)
(set! *unchecked-math* false)


(defn format-results
  "Format benchmark results into expected output."
  [{:keys [primes passes limit time variant count-f threads bits]}]
  (let [nanos (.toString (.toNanos time))
        timef (str (subs nanos 0 1) "." (subs nanos 1))
        valid? (= (count-f primes)
                  (prev-results limit))]
    (str "Passes: " passes ", "
         "Time: " timef ", "
         "Avg: " (float (/ (/ (.toNanos time) 1000000000) passes)) ", "
         "Limit: " limit ", "
         "Count: " (count-f primes) ", "
         "Valid: " (if valid? "True" "False")
         "\n"
         "pez-clj-" (name variant) ";" passes ";" timef ";" threads ";algorithm=base,faithful=yes,bits=" bits)))

(def confs
  {:oca {:sieve sieve
         :count-f count
         :threads 1
         :bits "?"}})

(defn run [{:keys [variant warm-up?]
            :or   {variant :oca
                   warm-up? false}}]
  (let [conf (confs variant)
        sieve (:sieve conf)]
    (when warm-up?
      ;; Warm-up reduces the variability of results.
      (format-results (merge conf (benchmark sieve) {:variant variant})))
    (println (format-results (merge conf (benchmark sieve) {:variant variant})))))

(comment
  (run {:warm-up? true})
  (run {:warm-up? false})
  (run {:variant :oca :warm-up? true})
  )