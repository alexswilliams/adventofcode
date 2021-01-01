(ns day16.day16
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.test :refer [with-test is]]))


(defn parse-ticket [line] (->> (string/split line #",") (map #(Long/valueOf %)) (vec)))
(defn parse-criterion [line]
  (->> (re-seq #"^([a-z ]+): ([0-9]+)-([0-9]+) or ([0-9]+)-([0-9]+)$" line)
       (first)
       ((fn [[_ field min-1 max-1 min-2 max-2]]
          [field
           (set (concat (range (Long/valueOf min-1) (inc (Long/valueOf max-1)))
                        (range (Long/valueOf min-2) (inc (Long/valueOf max-2)))))]))))
(defn parse-input [input]
  (->> (string/split input #"\r?\n\r?\n")
       (map string/split-lines)
       ((fn [[criteria yours nearby]] {:criteria (apply array-map (mapcat parse-criterion criteria))
                                       :your-ticket (parse-ticket (last yours))
                                       :nearby-tickets (map parse-ticket (drop 1 nearby))}))))


(def problem-input (delay (slurp "src/day16/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))

(defonce example-input (delay (slurp "src/day16/example.txt")))
(def parsed-example-input (delay (parse-input @example-input)))

(def example-input-2 "class: 0-1 or 4-19
row: 0-5 or 8-19
seat: 0-13 or 16-19

your ticket:
11,12,13

nearby tickets:
3,9,18
15,1,5
5,14,9")
(def parsed-example-input-2 (parse-input example-input-2))


(defn is-invalid-field? [criteria field] (not-any? #(% field) criteria))
(defn is-valid-ticket? [criteria fields] (not-any? (partial is-invalid-field? criteria) fields))

(with-test
  (defn fields-failing-all-criteria
    "Finds all fields across all tickets which match none of the criteria for a valid field"
    [{:keys [criteria nearby-tickets]}]
    (filter (partial is-invalid-field? (vals criteria))
            (flatten nearby-tickets)))
  (is (= [4 55 12] (fields-failing-all-criteria @parsed-example-input))))


(with-test
  (defn find-valid-tickets
    "Removes any tickets containing invalid fields"
    [{:keys [criteria nearby-tickets]}]
    (filter (partial is-valid-ticket? (vals criteria))
            nearby-tickets))
  (is (= [[7 3 47]] (find-valid-tickets @parsed-example-input)))
  (is (= [[3 9 18] [15 1 5] [5 14 9]] (find-valid-tickets parsed-example-input-2))))


(with-test
  (defn candidate-criteria-for-field
    "Determines which field a given list of values could belong to.
     Returns the names of the possible fields as a set."
    [criteria field-values]
    (->> (filter (fn [criterion] (every? #((val criterion) %)
                                         field-values))
                 criteria)
         (map first)
         (set)))
  (is (= #{"B"} (candidate-criteria-for-field {"A" #{1 2 3} "B" #{1 3 5 7}} [1 3 5])))
  (is (= #{"A" "B"} (candidate-criteria-for-field {"A" #{1 2 3} "B" #{1 3 5 7}} [1 3])))
  (is (= #{"A"} (candidate-criteria-for-field {"A" #{1 2 3} "B" #{1 3 5 7}} [2 3]))))

(with-test
  (defn candidates-for-input
    "Collates per-field candidate information together for the whole problem."
    [{:keys [criteria] :as input}]
    (->> (find-valid-tickets input)
         (apply map vector)
         (map (partial candidate-criteria-for-field criteria))
         (map-indexed vector)
         (into {})))
  (is (= {0 #{"row"} 1 #{"class" "row"} 2 #{"class" "row" "seat"}} (candidates-for-input parsed-example-input-2))))

(with-test
  (defn reduce-by-progressive-elimination
    "Look for fields that can only possibly be a single field name; keep note of and then remove these from the
     problem.  Repeat until all fields have been eliminated, or an underconstrained situation is found."
    [candidate-map]
    (let [singles (->> (filter #(= 1 (count (val %))) candidate-map)
                       (map (fn [[index names]] [index (some identity names)]))
                       (into (sorted-map)))
          eliminatable-names (set (concat (vals singles)))
          after-elimination (->> (filter #(< 1 (count (val %))) candidate-map)
                                 (map (fn [[index names]] [index (set/difference names eliminatable-names)]))
                                 (into {}))]
      (cond
        (empty? candidate-map) {}
        (empty? singles) (throw (Exception. "Problem does not reduce cleanly"))
        :else (into singles (reduce-by-progressive-elimination after-elimination)))))
  
  (is (= {0 "row"
          1 "class"
          2 "seat"}
         (->> (candidates-for-input parsed-example-input-2)
              (reduce-by-progressive-elimination)))))

(defn find-departure-fields
  "Glue code for part 2"
  [input]
  (->> input
       (candidates-for-input)
       (reduce-by-progressive-elimination)
       (filter #(string/starts-with? (val %) "departure"))
       (map #(get (:your-ticket input) (first %)))))


(comment

  ; Part 1
  (= 18142
     (reduce + (fields-failing-all-criteria @parsed-problem-input)))

  ; Part 2
  (= 1069784384303
     (reduce * (find-departure-fields @parsed-problem-input))))