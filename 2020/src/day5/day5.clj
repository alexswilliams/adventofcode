(ns day5
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(defn parse-input [input] (string/split-lines input))
(defonce problem-input (delay (slurp "src/day5/input.txt")))
(defonce example-input (delay (slurp "src/day5/example.txt")))
(def parsed-problem-input (parse-input @problem-input))
(def parsed-example-input (parse-input @example-input))


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
  (whittle [\F \B \F \B \B \F \F] (range 0 128))
  (whittle [\R \L \R] (range 0 8))
  (seat-id 44 5)
  (seat-id "FBFBBFFRLR")
  (reduce max (map seat-id parsed-example-input))
  (missing-seats (set (map seat-id parsed-problem-input)))

  ; Part 1
  (reduce max (map seat-id parsed-problem-input))

  ; Part 2
  (missing-seats-with-adjacent-filled-seats (set (map seat-id parsed-problem-input))))
