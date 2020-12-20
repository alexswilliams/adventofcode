(ns day18.day18
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))

(defn parse-line [input] (->> (filter #(not (= \space %)) input)
                              (map #(case %
                                      \+ :plus
                                      \* :mult
                                      \( :open
                                      \) :close
                                      (- (int %) 0x30))))) ; big ol assumption - only numbers 0-9 will be used
(defn parse-input [input] (->> (string/split-lines input)
                               (map parse-line)))

(defonce problem-input (delay (slurp "src/day18/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))


(defn shunting-yard
  "Transforms infix notation into RPN, according to an operator precedence 
   specified by `prio`  E.g. if prio={:plus 1 :mult 1} then operators will be
   processed left to right; if prio={:plus 2 :mult 1} then addition will be
   performed before multiplications.  No effort made to validate the expression"
  [prio input]
  (loop [output-queue                       []
         [ops-head & ops-tail :as op-stack] []
         [head & tail :as tokens]           input]
    (cond
      (nil? head) (concat output-queue op-stack)
      (int? head) (recur (conj (vec output-queue) head) op-stack tail)
      (#{:plus :mult} head)
      (if (or (nil? ops-head) (= :open ops-head) (< (prio ops-head) (prio head)))
        (recur output-queue (cons head op-stack) tail)
        (recur (conj (vec output-queue) ops-head) ops-tail tokens))
      (= :open head) (recur output-queue (cons :open op-stack) tail)
      (= :close head) (recur (->> (take-while #(not (= :open %)) op-stack)
                                  (concat output-queue))
                             (->> (drop-while #(not (= :open %)) op-stack)
                                  (drop 1))
                             tail))))

(with-test
  (def equal-priorities {:plus 1 :mult 1})

  (is (= [1 2 :plus 3 :mult]
         (shunting-yard equal-priorities [1 :plus 2 :mult 3])))
  (is (= [1 2 3 :mult :plus]
         (shunting-yard equal-priorities [1 :plus :open 2 :mult 3 :close])))
  (is (= [3 4 :plus 2 :mult 1 5 :plus :mult]
         (shunting-yard equal-priorities [3 :plus 4 :mult 2 :mult :open 1 :plus 5 :close])))
  (is (= [1 2 3 :mult :plus 4 5 6 :plus :mult :plus]
         (shunting-yard equal-priorities (parse-line "1 + (2 * 3) + (4 * (5 + 6))"))))
  (is (= [2 4 :plus 9 :mult 6 9 :plus 8 :mult 6 :plus :mult 6 :plus 2 :plus 4 :plus 2 :mult]
         (shunting-yard equal-priorities (parse-line "((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2"))))
  (is (= [1 2 :plus 3 :plus 4 :plus]
         (shunting-yard equal-priorities (parse-line "(((((1 + 2))) + 3)) + 4")))))

(with-test
  (def plus-has-priority {:plus 2 :mult 1})

  (is (= [1 2 :plus 3 :mult]
         (shunting-yard plus-has-priority (parse-line "1 + 2 * 3"))))
  (is (= [1 2 3 :plus :mult]
         (shunting-yard plus-has-priority (parse-line "1 * 2 + 3"))))
  (is (= [1 2 3 :mult :plus]
         (shunting-yard plus-has-priority (parse-line "1 + (2 * 3)"))))
  (is (= [3 4 :plus 2 :mult 1 5 :plus :mult]
         (shunting-yard plus-has-priority (parse-line "3 + 4 * 2 * (1 + 5)"))))
  (is (= [3 4 :mult 2 1 5 :plus :plus :mult]
         (shunting-yard plus-has-priority (parse-line "3 * 4 * 2 + (1 + 5)"))))
  (is (= [1 2 3 :mult :plus 4 5 6 :plus :mult :plus]
         (shunting-yard plus-has-priority (parse-line "1 + (2 * 3) + (4 * (5 + 6))"))))
  (is (= [2 4 :plus 9 :mult 6 9 :plus 8 6 :plus :mult 6 :plus :mult 2 :plus 4 :plus 2 :mult]
         (shunting-yard plus-has-priority (parse-line "((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2"))))
  (is (= [1 2 :plus 3 :plus 4 :plus]
         (shunting-yard plus-has-priority (parse-line "(((((1 + 2))) + 3)) + 4")))))

(def part-1-shunter (partial shunting-yard equal-priorities))
(def part-2-shunter (partial shunting-yard plus-has-priority))


(with-test
  (defn evaluate
    "Processes an RPN program created with the specified `shunter-func` from the
     given tokenised `expression`.  Numbers are puhed to a stack, and operators
     consume two stack items, transform them according to the operation, and push
     the answer to the top of the stack.  No effort is made to validate correctness
     of the program (indicated by an empty stack at any point beyond the initial
     state, or multiple items in the stack at the end of the program.)"
    [shunter-func expression]
    (loop [[head & tail] (shunter-func expression)
           stack []]
      (cond
        (nil? head) (first stack)
        (int? head) (recur tail (cons head stack))
        (#{:mult :plus} head)
        (recur tail (cons (({:mult * :plus +} head) (first stack) (second stack))
                          (drop 2 stack))))))

  (is (= 7 (evaluate part-1-shunter [1 :plus :open 2 :mult 3 :close])))
  (is (= 71 (evaluate part-1-shunter (parse-line "1 + 2 * 3 + 4 * 5 + 6"))))
  (is (= 51 (evaluate part-1-shunter (parse-line "1 + (2 * 3) + (4 * (5 + 6))"))))
  (is (= 26 (evaluate part-1-shunter (parse-line "2 * 3 + (4 * 5)"))))
  (is (= 437 (evaluate part-1-shunter (parse-line "5 + (8 * 3 + 9 + 3 * 4 * 3)"))))
  (is (= 12240 (evaluate part-1-shunter (parse-line "5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))"))))
  (is (= 13632 (evaluate part-1-shunter (parse-line "((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2"))))

  (is (= 7 (evaluate part-2-shunter [1 :plus :open 2 :mult 3 :close])))
  (is (= 231 (evaluate part-2-shunter (parse-line "1 + 2 * 3 + 4 * 5 + 6"))))
  (is (= 51 (evaluate part-2-shunter (parse-line "1 + (2 * 3) + (4 * (5 + 6))"))))
  (is (= 46 (evaluate part-2-shunter (parse-line "2 * 3 + (4 * 5)"))))
  (is (= 1445 (evaluate part-2-shunter (parse-line "5 + (8 * 3 + 9 + 3 * 4 * 3)"))))
  (is (= 669060 (evaluate part-2-shunter (parse-line "5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))"))))
  (is (= 23340 (evaluate part-2-shunter (parse-line "((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2")))))


(comment
  ; Part 1

  (= 15285807527593
     (reduce + (map (partial evaluate part-1-shunter) @parsed-problem-input)))

  (= 461295257566346
     (reduce + (map (partial evaluate part-2-shunter) @parsed-problem-input))))
