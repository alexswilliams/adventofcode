(ns day5
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(defn parse-input [input] (string/split-lines input))

(defonce problem-input (delay (slurp "src/day5/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))
(defonce example-input (delay (slurp "src/day5/example.txt")))
(def parsed-example-input (delay (parse-input @example-input)))


(defn whittle [code remaining]
  (if (empty? code)
    remaining
    (case (first code)
      (\F \L) (recur (drop 1 code) (take (/ (count remaining) 2) remaining))
      (\B \R) (recur (drop 1 code) (drop (/ (count remaining) 2) remaining)))))

(defn seat-id
  ([row col] (+ col (* 8 row)))
  ([seat-code] (seat-id (first (whittle (take 7 seat-code) (range 0 128)))
                        (first (whittle (drop 7 seat-code) (range 0 8))))))

(defn missing-seats [seat-ids]
  (set/difference (set (range 0 (* 8 128)))
                  seat-ids))

(defn missing-seats-with-adjacent-filled-seats [seat-ids]
  (let [missing (missing-seats seat-ids)]
    (set (filter #(and (contains? seat-ids (inc %))
                       (contains? seat-ids (dec %)))
                 missing))))

(comment
  ; Part 1
  (= (map seat-id @parsed-example-input)
     [567 119 820])

  (= (reduce max (map seat-id @parsed-problem-input))
     996)

  ; Part 2
  (= (missing-seats-with-adjacent-filled-seats (set (map seat-id @parsed-problem-input)))
     #{671}))
