(ns day18.day18
  (:require [clojure.test :refer [with-test is]]))

(def example-1 "1 + 2 * 3 + 4 * 5 + 6")
(def example-2 "1 + (2 * 3) + (4 * (5 + 6))")

(defn parse-input [input] (->> (filter #(not (= \space %)) input)
                               (map #(case % \+ :plus \* :mult \( :open \) :close (- (int %) 0x30)))))
(def parsed-1 (parse-input example-1))
(def parsed-2 (parse-input example-2))


(with-test
  (defn shunting-yard-part-1 [input]
    (loop [output-queue                       []
           [ops-head & ops-tail :as op-stack] []
           [head & tail :as tokens]           input]
      (cond
        (nil? head) (concat output-queue op-stack)
        (int? head) (recur (conj output-queue head) op-stack tail)
        (#{:plus :mult} head)
        (if (or (nil? ops-head) (= :open ops-head))
          (recur output-queue (conj op-stack head) tail)
          (recur (conj output-queue ops-head) ops-tail tokens))
        (= :open head) (recur output-queue (conj op-stack :open) tail)
        (= :close head) (recur (->> (take-while #(not (= :open %)) op-stack)
                                    (concat output-queue))
                               (->> (drop-while #(not (= :open %)) op-stack)
                                    (drop 1))
                               tail))))
  (is (= [1 2 :plus 3 :mult]
         (shunting-yard-part-1 [1 :plus 2 :mult 3])))
  (is (= [1 2 3 :mult :plus]
         (shunting-yard-part-1 [1 :plus :open 2 :mult 3 :close])))
  (is (= [3 4 :plus 2 :mult 1 5 :plus :mult]
         (shunting-yard-part-1 [3 :plus 4 :mult 2 :mult :open 1 :plus 5 :close]))))

(with-test
  (defn evaluate [shunter-func expression]
    (loop [[head & tail] (shunter-func expression)
           stack []]
      (cond
        (nil? head) stack
        (int? head) (recur tail (cons head stack))
        (#{:mult :plus} head)
        (recur tail (cons (({:mult * :plus +} head) (first stack) (second stack))
                          (drop 2 stack)))))))


(comment

  
  (evaluate shunting-yard-part-1 [1 :plus :open 2 :mult 3 :close])

  parsed-1
  parsed-2)
