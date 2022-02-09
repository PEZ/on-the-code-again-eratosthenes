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
  (with-progress-reporting (quick-bench (count (sieve 1000000)))))

(defn sieve-vector
  "Using a Clojure immutable vector"
  [n]
  (if (< n 2)
    []
    (let [primes (vec (repeat (inc n) true))
          primes (assoc primes 0 false 1 false)
          sqrt-n (Math/ceil (Math/sqrt n))]
      (loop [p 2
             primes primes]
        (if (<= p sqrt-n)
          (if (nth primes p)
            (recur (inc p)
                   (loop [i (* p p)
                          primes primes]
                     (if (<= i n)
                       (recur (+ i p)
                              (assoc primes i false))
                       primes)))
            (recur (inc p)
                   primes))
          primes)))))

(comment
  (defn loot [primes]
    (keep-indexed (fn [i v]
                    (when v i))
                  primes))
  (sieve-vector 1)
  (loot (sieve-vector 2))
  (loot (sieve-vector 3))
  (loot (sieve-vector 10))
  (loot (sieve-vector 100))
  (count (loot (sieve-vector 100)))
  (time (do (sieve-vector 1000000) nil))
  (time (count (loot (sieve-vector 1000000))))
  (require '[criterium.core :refer [quick-bench with-progress-reporting]])
  (with-progress-reporting (quick-bench (sieve-vector 1000000))))

(set! *warn-on-reflection* true)
(set! *unchecked-math* false)

(defn sieve-vector
  "Using a Clojure immutable vector"
  [n]
  (if (< n 2)
    []
    (let [half-n (/ n 2)
          sqrt-n (long (Math/ceil (Math/sqrt n)))]
      (loop [p 3
             primes (vec (repeat half-n true))]
        (if (< p sqrt-n)
          (if (nth primes (/ p 2))
            (recur (+ p 2)
                   (loop [i (int (/ (* p p) 2))
                          primes primes]
                     (if (< i half-n)
                       (recur (+ i p)
                              (assoc primes i false))
                       primes)))
            (recur (+ p 2)
                   primes))
          primes)))))

(comment
  (defn loot [primes]
    (keep-indexed (fn [i v]
                    (if (zero? i)
                      2
                      (when v (inc (* i 2)))))
                  primes))
  (sieve-vector 1)
  (loot (sieve-vector 2))
  (loot (sieve-vector 5))
  (loot (sieve-vector 10))
  (loot (sieve-vector 100))
  (count (loot (sieve-vector 100)))
  (time (do (sieve-vector 1000000) nil))
  (time (count (loot (sieve-vector 1000000))))
  (require '[criterium.core :refer [quick-bench with-progress-reporting]])
  (with-progress-reporting (quick-bench (sieve-vector 1000000))))

(defn sieve-vector-transient
  "Using a Clojure transient vector"
  [n]
  (if (< n 2)
    []
    (let [half-n (/ n 2)
          sqrt-n (long (Math/ceil (Math/sqrt n)))
          primes (transient (vec (repeat half-n true)))]
      (loop [p 3]
        (when (< p sqrt-n)
          (when (nth primes (/ p 2))
            (loop [i (int (/ (* p p) 2))]
              (when (< i half-n)
                (assoc! primes i false)
                (recur (+ i p)))))
          (recur (+ p 2))))
      (persistent! primes))))

(comment
  (defn loot [primes]
    (keep-indexed (fn [i v]
                    (if (zero? i)
                      2
                      (when v (inc (* i 2)))))
                  primes))
  (sieve-vector-transient 1)
  (loot (sieve-vector-transient 2))
  (loot (sieve-vector-transient 5))
  (loot (sieve-vector-transient 10))
  (loot (sieve-vector-transient 100))
  (count (loot (sieve-vector-transient 100)))
  (time (do (sieve-vector-transient 1000000) nil))
  (time (count (loot (sieve-vector-transient 1000000))))
  (require '[criterium.core :refer [quick-bench with-progress-reporting]])
  (with-progress-reporting (quick-bench (sieve-vector-transient 1000000))))

(set! *unchecked-math* true)

(defn sieve-boolean-array
  "Using a Java array of `boolean`s vector"
  [n]
  (if (< n 2)
    []
    (let [half-n (/ n 2)
          sqrt-n (long (Math/ceil (Math/sqrt n)))
          primes (boolean-array half-n true)]
      (loop [p 3]
        (when (< p sqrt-n)
          (when (aget primes (/ p 2))
            (loop [i (int (/ (* p p) 2))]
              (when (< i half-n)
                (aset primes i false)
                (recur (+ i p)))))
          (recur (+ p 2))))
      primes)))

(comment
  (defn loot [primes]
    (keep-indexed (fn [i v]
                    (if (zero? i)
                      2
                      (when-not v (inc (* i 2)))))
                  primes))
  (sieve-boolean-array 1)
  (loot (sieve-boolean-array 2))
  (loot (sieve-boolean-array 5))
  (loot (sieve-boolean-array 10))
  (loot (sieve-boolean-array 100))
  (count (loot (sieve-boolean-array 100)))
  (time (do (sieve-boolean-array 1000000) nil))
  (time (count (loot (sieve-boolean-array 1000000))))
  (require '[criterium.core :refer [quick-bench with-progress-reporting]])
  (with-progress-reporting (quick-bench (sieve-boolean-array 1000000))))

(defn sieve-boolean-array-help-compiler
  "Using a Java array of `boolean`s vector"
  [^long n]
  (if (< n 2)
    []
    (let [half-n (unchecked-int (bit-shift-right n 1))
          sqrt-n (unchecked-long (Math/ceil (Math/sqrt (unchecked-double n))))
          primes (boolean-array half-n)]
      (loop [p 3]
        (when (< p sqrt-n)
          (when-not (aget primes (bit-shift-right p 1))
            (loop [i (int (bit-shift-right (* p p) 1))]
              (when (< i half-n)
                (aset primes i true)
                (recur (+ i p)))))
          (recur (+ p 2))))
      primes)))

(comment
  (defn loot [primes]
    (keep-indexed (fn [i v]
                    (if (zero? i)
                      2
                      (when-not v (inc (* i 2)))))
                  primes))
  (sieve-boolean-array-help-compiler 1)
  (loot (sieve-boolean-array-help-compiler 2))
  (loot (sieve-boolean-array-help-compiler 5))
  (loot (sieve-boolean-array-help-compiler 10))
  (loot (sieve-boolean-array-help-compiler 100))
  (count (loot (sieve-boolean-array-help-compiler 100)))
  (time (do (sieve-boolean-array-help-compiler 1000000) nil))
  (time (count (loot (sieve-boolean-array-help-compiler 1000000))))
  (require '[criterium.core :refer [quick-bench with-progress-reporting]])
  (with-progress-reporting (quick-bench (sieve-boolean-array-help-compiler 1000000))))

(comment
  (require '[clj-async-profiler.core :as prof])
  (prof/serve-files 8080))

(set! *warn-on-reflection* false)
(set! *unchecked-math* false)

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
         (name variant) ";" passes ";" timef ";" threads ";algorithm=base,faithful=yes,bits=" bits)))

(def confs
  {:set {:sieve sieve
         :count-f count
         :threads 1
         :bits "?"}
   :vector {:sieve sieve-vector
            :count-f (fn [primes] (count (filter true? primes)))
            :threads 1
            :bits "?"}
   :vector-transient {:sieve sieve-vector-transient
                      :count-f (fn [primes] (count (filter true? primes)))
                      :threads 1
                      :bits "?"}
   :boolean-array {:sieve sieve-boolean-array
                   :count-f (fn [primes] (count (filter true? primes)))
                   :threads 1
                   :bits 8}
   :boolean-array-help-compiler {:sieve sieve-boolean-array-help-compiler
                                 :count-f (fn [primes] (count (filter true? primes)))
                                 :threads 1
                                 :bits 8}})

(defn run [{:keys [variant warm-up?]
            :or   {variant :set
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
  (run {:variant :set :warm-up? true})
  (run {:variant :vector :warm-up? true})
  (run {:variant :vector-transient :warm-up? true})
  (run {:variant :boolean-array :warm-up? true})
  (run {:variant :boolean-array-help-compiler :warm-up? true})
  )