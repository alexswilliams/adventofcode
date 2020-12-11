(ns day6.day6
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(defn parse-input [input] (->> (string/split input #"\r?\n\r?\n")))

(defonce problem-input (delay (slurp "src/day6/input.txt")))
(defonce parsed-problem-input (delay (parse-input @problem-input)))

(defonce example-input (delay (slurp "src/day6/example.txt")))
(defonce parsed-example-input (delay (parse-input @example-input)))


(defn sum-of-distinct-answers-per-group [input]
  (->> input
       (map distinct)
       (map #(remove #{\newline} %))
       (map count)
       (apply +)))


(defn sum-of-common-answers-per-group [input]
  (->> input
       (map #(string/split % #"\r?\n"))
       (map (fn [input] (->> input (map set) (apply clojure.set/intersection))))
       (map count)
       (apply +)))


(comment
  (= (sum-of-distinct-answers-per-group @parsed-problem-input)
     7120)

  (= (sum-of-common-answers-per-group @parsed-problem-input)
     3570))
