(ns day9.day9
  (:require [clojure.string :as string]))

(defn parse-input [input preamble-length]
  {:window-size preamble-length
   :data        (->> (string/split-lines input)
                     (map #(Long/valueOf %)))})

(defonce problem-input (delay (slurp "src/day9/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input 25)))

(defonce example-input (delay (slurp "src/day9/example.txt")))
(def parsed-example-input (delay (parse-input @example-input 5)))


(defn can-be-made-from-sum-of-pair [[number candidates]]
  (vector number 
          (first (for [x     candidates
                       y     candidates
                       :when (not (= x y))
                       :when (= number (+ x y))]
                   [x y]))))

(defn find-non-summing-number [{:keys [window-size data]}]
  (let [numbers-to-test (drop window-size data)
        windows         (partition window-size 1 data)
        tests           (map vector numbers-to-test windows)]
    (->> (map can-be-made-from-sum-of-pair tests)
         (filter #(nil? (nth % 1)))
         (first)
         (first))))


(defn all-contiguous-sequences [input]
  (->> (range 0 (dec (count input)))
       (map #(drop % input))))

(defn find-range-summing-to-target [target input]
  (loop [seqs (all-contiguous-sequences input)]
    (let [summing-seq (loop [candidates (first seqs)
                             used       []]
                        (let [sum (apply + used)]
                          (if (= target sum)
                            used
                            
                            (if (or (empty? candidates) (> sum target))
                              :overrun
                              
                              (recur (drop 1 candidates) (conj used (first candidates))))))
                        )]
      (if (= :overrun summing-seq)
        (recur (drop 1 seqs))
        summing-seq))))

(defn part-2-submission [span] (+ (reduce min span) (reduce max span)))


(comment
  
  ; Part 1
  (= (find-non-summing-number @parsed-example-input)
     127)
  (= (find-non-summing-number @parsed-problem-input)
     776203571)
  
  ; Part 2
  (= (->> (:data @parsed-example-input)
          (find-range-summing-to-target (find-non-summing-number @parsed-example-input))
          (part-2-submission))
     62)
  (= (->> (:data @parsed-problem-input)
          (find-range-summing-to-target (find-non-summing-number @parsed-problem-input))
          (part-2-submission))
     104800569)
  )
