(ns day10
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))


(defn parse-input [input] (->> (string/split-lines input)
                               (map #(Long/valueOf %))))


(defonce problem-input (delay (slurp "src/day10/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))

(defonce example-input (delay (slurp "src/day10/example.txt")))
(def parsed-example-input (delay (parse-input @example-input)))

(def smaller-example-input '(16 10 15 5 1 11 7 19 6 12 4))


(with-test
  (defn device-rating [adapter-ratings] (+ 3 (reduce max adapter-ratings)))
  (is (= 7 (device-rating [1 4])))
  (is (= 22 (device-rating smaller-example-input)))
  (is (= 52 (device-rating @parsed-example-input)))
  )

(with-test
  (defn all-ratings
    "Adds the artificial '0' rating of the wall plug, and the device's
   own rating of the (highest rating + 3) to a sorted list of device ratings"
    [ratings]
    (->> (device-rating ratings)
         (conj ratings)
         (cons 0)
         (sort)))
  (is (= '(0 1 2 3 4 7) (all-ratings [1 2 3 4]))))

(with-test
  (defn deltas-between-adapters
    "Finds the difference between adjacent pairs of adapters"
    [adapter-ratings]
    (let [ratings        (all-ratings adapter-ratings)
          pairs-to-delta (partition 2 1 ratings)]
      (map #(- (last %) (first %))
           pairs-to-delta)))
  (is (= '(1 3 1 1 1 3 1 1 3 1 3 3) 
         (deltas-between-adapters smaller-example-input))))

(with-test
  (defn part-1-answer [deltas]
    (let [ones   (count (filter #{1} deltas))
          threes (count (filter #{3} deltas))]
      (* ones threes)))
  (is (= 35 (part-1-answer (deltas-between-adapters smaller-example-input))))
  (is (= 220 (part-1-answer (deltas-between-adapters @parsed-example-input)))))



(with-test
  (defn candidate-pairs
    "Selects all pairs of adapters which satisfy being within a value of 3 energy
    units of one another."
    [ratings]
    (let [ratings (all-ratings ratings)]
      (for [a     ratings
            b     ratings
            :when (< a b)
            :when (<= (- b a) 3)]
        [a b])))
  (is (= '([0 1] [1 4] [4 5] [4 6] [4 7] [5 6] [5 7] [6 7] [7 10] [10 11]
           [10 12] [11 12] [12 15] [15 16] [16 19] [19 22])
         (candidate-pairs smaller-example-input))))


(with-test
  (defn pairs-grouped-by-lhs
    "Groups the adapter pairings by the lowest adapter rating.  For example, if
     an adapter of rating 4 can access adapters of rating 5 and 6, a map entry
     of {4 [5 6]} will be associated with the result."
    [pairs]
    (->> (group-by first pairs)
         (reduce (fn [altered [k v]] (assoc altered k (distinct (map last v)))) {})))
  (is (= {0  '(1)
          1  '(4)
          4  '(5 6 7)
          5  '(6 7)
          6  '(7)
          7  '(10)
          10 '(11 12)
          11 '(12)
          12 '(15)
          15 '(16)
          16 '(19)
          19 '(22)}
         (pairs-grouped-by-lhs (candidate-pairs smaller-example-input)))))


(with-test
  (defn weigh-keys 
    "Walk backwards through the list of possible routes, summing the number
     possible routes to the final destination that are reachable from that
     starting node, storing that result as a cache for future lookups.
     
     E.g. if Ø is a function which returns how many routes are possible to
     reach the final node, then for any node N which can reach other nodes
     N1...Nx then Ø(N) = Ø(N1) + ... + Ø(Nx).  As starting nodes can only
     refer to nodes higher up, Ø will have a value for all nodes > N.  This
     does need to be seeded with the target value, e.g. if the highest
     adapter rating was 19, an artificial base case must be added Ø(22) = 1"
    [weight-map node-map]
    (let [highest-node     (reduce max (keys node-map))
          connecting-nodes (node-map highest-node)
          node-weight      (->> (map weight-map connecting-nodes)
                                (reduce +))
          new-weight-map   (assoc weight-map highest-node node-weight)]
      (if (= 0 highest-node)
        node-weight
        (recur new-weight-map (dissoc node-map highest-node)))))
  (is (= 1 (weigh-keys {1 1} {0 [1]})))
  (is (= 2 (weigh-keys {2 1} {0 [1 2]
                              1 [2]})))
  (is (= 8 (weigh-keys {22 1} (pairs-grouped-by-lhs (candidate-pairs smaller-example-input)))))
  (is (= 19208 (weigh-keys {52 1} (pairs-grouped-by-lhs (candidate-pairs @parsed-example-input))))))


(defn part-2 [input]
  (let [candidates (candidate-pairs input)
        target     (device-rating input)
        node-map   (pairs-grouped-by-lhs candidates)]
    (weigh-keys {target 1} node-map)))


(comment

  ; Part 1
  (= (part-1-answer (deltas-between-adapters @parsed-problem-input))
     2040)

  ; Part 2
  (= (part-2 @parsed-problem-input)
     28346956187648))
