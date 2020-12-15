(ns day3.day3
  (:require [clojure.string :as string]))

(defonce problem-input (delay (slurp "src/day3/input.txt")))
(defonce parsed-problem-input (->> (string/split-lines @problem-input)
                                   (map vec)
                                   (map cycle)))


(defn path-through [run fall forest-rows]
  (for [y (range 0 (count (take-nth run forest-rows)))]
    (let [x (* y fall)]
      [x y (nth (nth (take-nth run forest-rows) y) x)])))

(defn number-of-trees [path]
  (count (filter #{\#} (map #(nth % 2) path))))



(defonce example-input (delay (slurp "src/day3/example.txt")))
(defonce parsed-example-input (->> (string/split-lines @example-input)
                                   (map vec)
                                   (map cycle)))

(comment
  ; Part 1
  (path-through 2 1 parsed-example-input)
  
  (number-of-trees (path-through 1 1 parsed-example-input))
  (number-of-trees (path-through 1 3 parsed-example-input))
  (number-of-trees (path-through 1 5 parsed-example-input))
  (number-of-trees (path-through 1 7 parsed-example-input))
  (number-of-trees (path-through 2 1 parsed-example-input))
  (path-through 2 1 parsed-example-input)

  (*  (number-of-trees (path-through 1 1 parsed-problem-input))
      (number-of-trees (path-through 1 3 parsed-problem-input))
      (number-of-trees (path-through 1 5 parsed-problem-input))
      (number-of-trees (path-through 1 7 parsed-problem-input))
      (number-of-trees (path-through 2 1 parsed-problem-input)))

  (number-of-trees [[1 1 \.] [1 1 \#]])
  (path-through 1 3 parsed-problem-input))