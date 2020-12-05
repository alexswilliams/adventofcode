(ns day2.day2
  (:require [clojure.string :as string]))

(defn coerce-input [[min-count max-count chr password]]
  [(Long/valueOf min-count) (Long/valueOf max-count) (first chr) password])

(defonce problem-input (delay (slurp "src/day2/input.txt")))
(defonce parsed-problem-input (delay (->> (string/split-lines @problem-input)
                                          (map #(string/split % #"[-: ]+"))
                                          (map coerce-input))))


(defn is-valid-password-by-range? [[min-count max-count chr password]]
  (let [count-in-password (count (filter #(= chr %) password))]
    (and (>= count-in-password min-count)
         (<= count-in-password max-count))))

(defn is-valid-password-by-position? [[first-pos second-pos chr password]]
  (let [first-char ((vec password) (dec first-pos))
        second-char (nth password (dec second-pos))
        xor (fn [a b] (or (and a (not b))
                          (and b (not a))))]
    (xor (= first-char chr)
         (= second-char chr))))


(comment
  (count (filter is-valid-password-by-range? @parsed-problem-input))
  (count (filter is-valid-password-by-position? @parsed-problem-input))
  (is-valid-password-by-range? [2 6 \c "fcpwjqhcgtffzlbj"])
  (is-valid-password-by-position? [2 6 \c "fcpwjqhcgtffzlbj"])
  @parsed-problem-input)
