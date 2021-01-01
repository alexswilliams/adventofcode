(ns day15.day15
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))

(defn parse-input [input]
  (->> (string/split input #",")
       (map #(Long/valueOf %))))

(def example-input "0,3,6")
(def parsed-example (parse-input example-input))
(def problem-input "2,1,10,11,0,6")
(def parsed-problem (parse-input problem-input))


(with-test
  (defn expand-sequence
    "Generates a lazy infinite sequence of 'ages' as defined by the problem."
    ([input]
     (let [index-map (->> (map-indexed vector (drop-last input))
                          (group-by second)
                          (map (fn [[n pairs]] [n (->> (map (comp inc first) pairs)
                                                       (apply max))]))
                          (into {}))

           expand     (fn expand [index-map last-number last-index]
                        (let [index-of-last-number (get index-map last-number)
                              this-index (inc last-index)
                              this-number (if (nil? index-of-last-number)
                                            0
                                            (- this-index (inc index-of-last-number)))
                              new-index-map (assoc index-map last-number last-index)]
                          (cons this-number
                                (lazy-seq (expand new-index-map this-number this-index)))))]
       (concat input (expand index-map (last input) (count input))))))


  (is (= [0 3 6 0 3 3 1 0 4 0] (take 10 (expand-sequence parsed-example))))
  (is (= 436 (-> (expand-sequence parsed-example) (nth 2019))))
  (is (= 1 (-> (parse-input "1,3,2") (expand-sequence) (nth 2019))))
  (is (= 10 (-> (parse-input "2,1,3") (expand-sequence) (nth 2019))))
  (is (= 27 (-> (parse-input "1,2,3") (expand-sequence) (nth 2019))))
  (is (= 78 (-> (parse-input "2,3,1") (expand-sequence) (nth 2019))))
  (is (= 438 (-> (parse-input "3,2,1") (expand-sequence) (nth 2019))))
  (is (= 1836 (-> (parse-input "3,1,2") (expand-sequence) (nth 2019))))
  ; This passes but is slow - "Elapsed time: 21338.149584 msecs"
  #_(is (= 175594 (time (-> parsed-example (expand-sequence) (nth (dec 30000000)))))))

(comment
  ; Part 1
  ; 
  (= 232
     (-> parsed-problem (expand-sequence) (nth (dec 2020))))

  ; Part 2
  ; "Elapsed time: 23099.307569 msecs" - eh, acceptable
  (= 18929178
     (time (-> parsed-problem (expand-sequence) (nth (dec 30000000))))))
