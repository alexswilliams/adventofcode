(ns day14.day14
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]
            [instaparse.core :as insta]))

(def initial-state {:memory {}
                    :mask-func identity})

(with-test (defn grammar [input]
             (->> ((insta/parser "<S> = mask|mem;
                                  mask = <'mask = '> bitstring;
                                  <bitstring> = #'[X01]+';
                                  mem = <'mem['> number <'] = '> number;
                                  number = #'[0-9]+';") input)
                  (insta/transform {:number #(Long/valueOf %)})
                  (first)))
  (is (= [:mem 4616 8311689]
         (grammar "mem[4616] = 8311689")))
  (is (= [:mask "X1011100000X111X01001000001110X00000"]
         (grammar "mask = X1011100000X111X01001000001110X00000"))))


(defn parse-input [input] (->> (string/split-lines input)
                               (map grammar)))

(defonce problem-input (delay (slurp "src/day14/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))
(comment @parsed-problem-input)
(def example-program [[:mask "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X"]
                      [:mem 8 11]
                      [:mem 7 101]
                      [:mem 8 0]])

(with-test
  (defn exp [x power] (reduce * (repeat power x)))
  (is (= 1 (exp 2 0)))
  (is (= 2 (exp 2 1)))
  (is (= 4 (exp 2 2)))
  (is (= 32 (exp 2 5)))
  (is (= 0x400000000 (exp 2 34))))

(with-test
  (defn mask-from [bit-string]
    (let [one-bits  (keep-indexed (fn [idx x] (if (= \1 x) (exp 2 (- 35 idx)) nil)) bit-string)
          zero-bits (keep-indexed (fn [idx x] (if (= \0 x) (exp 2 (- 35 idx)) nil)) bit-string)
          or-mask   (reduce bit-or one-bits)
          and-mask  (bit-xor 0xfffffffff (reduce bit-or zero-bits))]
      (fn mask [number] (->> number
                             (bit-or or-mask)
                             (bit-and and-mask)))))
  (is (= 73 ((mask-from "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X") 11)))
  (is (= 101 ((mask-from "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X") 101)))
  (is (= 64 ((mask-from "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X") 0)))

  (is (= 0xDC1F483A0 ((mask-from "X1011100000X111X01001000001110X00000") 0xfffffffff)))
  (is (= (Long/valueOf "110111000001111101001000001110100000" 2)
         ((mask-from   "X1011100000X111X01001000001110X00000") 0xfffffffff))))

(with-test
  (defn step [{:keys [memory mask-func]
               :as   state} command]
    (case (first command)
      :mask (assoc state :mask-func (mask-from (second command)))
      :mem (assoc state :memory
                  (assoc memory (second command) (mask-func (last command))))))

  (is (= {} (:memory (step initial-state (first example-program)))))
  (is (= {8 73} (:memory (-> initial-state
                             (step (first example-program))
                             (step (second example-program))))))
  (is (= {8 73
          7 101} (:memory (-> initial-state
                              (step (first example-program))
                              (step (second example-program))
                              (step (nth example-program 2))))))
  (is (= {8 64
          7 101} (:memory (-> initial-state
                              (step (first example-program))
                              (step (second example-program))
                              (step (nth example-program 2))
                              (step (last example-program)))))))

(with-test
  (defn all-steps [program]
    (reduce step initial-state program))
  (is (= {8 64
          7 101} (:memory (all-steps example-program)))))

(with-test
  (defn sum-of-all-memory [memory]
    (reduce + (vals memory)))
  (is (= 165 (sum-of-all-memory {8 64
                                 7 101}))))

(comment
  (= 14722016054794
     (->> (all-steps @parsed-problem-input)
          (:memory)
          (sum-of-all-memory))))