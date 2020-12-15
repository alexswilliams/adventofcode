(ns day7.day7
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [instaparse.core :as insta]
            [clojure.test :refer [with-test is deftest]]))

(with-test
  (def problem-grammar
    (insta/parser
     "<S> = COLOUR <' bags contain '> RULES <'.'>;
    <RULES> = (RULE <[', ']>)+ | <NO_BAGS>;
    NO_BAGS = <'no other bags'>;
    <RULE> = NUMBER <' '> COLOUR <' bag' ['s']>;
    <COLOUR> = #'[a-z]+ [a-z]+';
    <NUMBER> = #'[0-9]+';
    "))
  (is (= (problem-grammar "light red bags contain 1 bright white bag, 2 muted yellow bags.")
         ["light red", "1" "bright white", "2" "muted yellow"]))
  (is (= (problem-grammar "faded blue bags contain no other bags.")
         ["faded blue"])))

(with-test
  (defn grammar-to-bags-struct [[colour & rules]]
    [colour
     (->> (partition 2 rules)
          (map (fn [[bag-count colour]] [colour (Long/valueOf bag-count)]))
          (into {}))])

  (is (= ["light red" {"bright white" 1, "muted yellow" 2}]
         (grammar-to-bags-struct ["light red", "1" "bright white", "2" "muted yellow"])))
  (is (= ["faded blue" {}]
         (grammar-to-bags-struct ["faded blue"]))))


(defn parse-input [input] (->> (string/split input #"\r?\n")
                               (map problem-grammar)
                               (map grammar-to-bags-struct)
                               (into {})))
(deftest parse-input-tests
  (is (= (parse-input "vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
faded blue bags contain no other bags.")
         {"vibrant plum" {"faded blue" 5, "dotted black" 6}, "faded blue" {}})))


(defonce problem-input (delay (slurp "src/day7/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))

(defonce example-input (delay (slurp "src/day7/example.txt")))
(def parsed-example-input (delay (parse-input @example-input)))

(deftest parsed-example
  (is (= @parsed-example-input
         {"muted yellow" {"shiny gold" 2, "faded blue" 9}
          "light red"    {"bright white" 1, "muted yellow" 2}
          "dotted black" {}
          "dark orange"  {"bright white" 3, "muted yellow" 4}
          "bright white" {"shiny gold" 1}
          "shiny gold"   {"dark olive"   1, "vibrant plum" 2}
          "faded blue"   {}
          "vibrant plum" {"faded blue"   5, "dotted black" 6}
          "dark olive"   {"faded blue"   3, "dotted black" 4}})))

(with-test
  (defn transitive-parents-of
    "Locates all parents of the given colour, and all parents of those parents, until
   either all parents have already been seen, or there are no more ancestors."
    ([rules colour] (transitive-parents-of rules #{colour} colour))
    ([rules already-seen colour]
     (let [unseen-rules   (filter #(not (already-seen (key %))) rules)
           direct-parents (->> (filter #(contains? (val %) colour) unseen-rules)
                               (keys)
                               (set))
           now-seen       (set/union direct-parents already-seen)]
       (if (empty? direct-parents)
         #{}
         (first (reduce
                 (fn collect-parents [[all-parents-so-far seen-so-far] parent-colour]
                   (let [result (transitive-parents-of rules seen-so-far parent-colour)]
                     [(set/union all-parents-so-far result)
                      (set/union seen-so-far result)]))
                 [direct-parents now-seen]
                 direct-parents))))))

  (is (= (transitive-parents-of @parsed-example-input "shiny gold")
         #{"muted yellow" "light red" "dark orange" "bright white"})))


(with-test
  (defn transitive-sum-of-children [rules colour]
    (let [children            (get rules colour)
          total-child-bags    (apply + (vals children))
          transitive-children (map #(* (get children %)
                                       (transitive-sum-of-children rules %))
                                   (keys children))]
      (apply + (conj transitive-children total-child-bags))))

  (is (= (transitive-sum-of-children @parsed-example-input "shiny gold")
         32)
      (= (transitive-sum-of-children (parse-input "shiny gold bags contain 2 dark red bags.
dark red bags contain 2 dark orange bags.
dark orange bags contain 2 dark yellow bags.
dark yellow bags contain 2 dark green bags.
dark green bags contain 2 dark blue bags.
dark blue bags contain 2 dark violet bags.
dark violet bags contain no other bags.") "shiny gold")
         126)))


(comment

  ; Part 1
  (= (count (transitive-parents-of @parsed-problem-input "shiny gold"))
     254)

  ; Part 2
  (= (transitive-sum-of-children @parsed-problem-input "shiny gold")
     6006))
