(ns day1.day1
  (:require [clojure.string :as string]))

(defonce problem-input (delay (slurp "src/day1/input.txt")))
(defonce parsed-problem-input (delay (->> (string/split-lines @problem-input)
                                          (map #(Long/valueOf %))
                                          sort)))

(defn pair-adds-to-2020 [input]
  (first (for [x input y (drop-while #(<= % x) input)
               :while (<= (+ x y) 2020)
               :when (= 2020 (+ x y))] [x y])))

(defn triple-adds-to-2020 [input]
  (first (for [x input
               y (drop-while #(<= % x) input)
               z (drop-while #(<= % y) input)
               :let [sum (+ x y z)]
               :while (<= sum 2020)
               :when (= 2020 sum)] [x y z])))


(comment
  (apply * (pair-adds-to-2020 @parsed-problem-input))
  (apply * (triple-adds-to-2020 @parsed-problem-input))

  (apply * (pair-adds-to-2020 (sort [1721 979 366 299 675 1456])))
  (apply * (triple-adds-to-2020 (sort [1721 979 366 299 675 1456]))))
