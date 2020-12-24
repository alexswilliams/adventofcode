(ns day13.day13
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))

; Observation: these are all prime.
(def example-input "939
7,13,x,x,59,x,31,19")
(def problem-input "1007125
13,x,x,41,x,x,x,x,x,x,x,x,x,569,x,29,x,x,x,x,x,x,x,x,x,x,x,x,x,x,x,x,19,x,x,x,23,x,x,x,x,x,x,x,937,x,x,x,x,x,37,x,x,x,x,x,x,x,x,x,x,17")

(defn parse-pairs [line] (->> (string/split line #"[,]")
                              (map-indexed vector)
                              (filter #(not= "x" (second %)))
                              (map #(vector (- 0 (first %)) (Long/valueOf (second %))))))

(defn parse-input [input] (let [lines (string/split-lines input)
                                target (->> (first lines) (Long/valueOf))
                                pairs (parse-pairs (second lines))
                                numbers (map second pairs)]
                            {:target target
                             :numbers numbers
                             :pairs pairs}))
(def example (parse-input example-input))
(def problem (parse-input problem-input))


; Part 1

(defn lowest-wait [map] [(apply min-key map (keys map)) (apply min (vals map))])
(with-test
  (defn lowest-wait-for-services [services target]
    (->> (map #(- % (rem target %)) services)
         (zipmap services)
         (lowest-wait)))
  (is (= [59 5]
         (lowest-wait-for-services (:numbers example) (:target example)))))


; Part 2

; Specifically for this problem input, there is the following system of congruences:
; (The remainders are negative as the problem says the solution occurs `a` places *before*
; the multiple of each n.)
; x ≡ 0 (mod 13)    ≡ 0 (mod 13)
; x ≡ -3 (mod 41)   ≡ 38 (mod 41)
; x ≡ -13 (mod 569) ≡ 556 (mod 569)
; x ≡ -15 (mod 29)  ≡ 14 (mod 29)
; x ≡ -32 (mod 19)  ≡ 6 (mod 19)
; x ≡ -36 (mod 23)  ≡ 10 (mod 23)
; x ≡ -44 (mod 937) ≡ 893 (mod 937)
; x ≡ -50 (mod 37)  ≡ 24 (mod 37)
; x ≡ -61 (mod 17)  ≡ 7 (mod 17)
; such that 100,000,000,000,000 ≤ x < (13 × 41 × 569 × 29 × 19 × 23 × 937 × 37 × 17)
; (The problem wording suggests a minimum bound of 10e14 for x)
(= 2265213528143033 (apply * (map second (:pairs problem))))

; The example isn't much simpler, just shorter:
; x ≡ 0 (mod 7)   ≡ 
; x ≡ -1 (mod 13) ≡
; x ≡ -4 (mod 59) ≡
; x ≡ -6 (mod 31) ≡
; x ≡ -7 (mod 19) ≡
; such that 0 ≤ x < (7 × 13 × 59 × 31 × 19)
(= 3162341 (apply * (map second (:pairs example))))
; In this case, we're told x = 1068781


(with-test
  (defn reduce-to-smallest-form [[a n]] [(mod a n) n])
  (is (= [9 10] (reduce-to-smallest-form [-1 10])))
  (is (= [0 10] (reduce-to-smallest-form [-10 10])))
  (is (= [4 10] (reduce-to-smallest-form [14 10]))))

(with-test
  (defn simplify-mod-pairs [pairs] (map reduce-to-smallest-form pairs))
  (is (= [[0 7] [12 13] [55 59] [25 31] [12 19]]
         (simplify-mod-pairs (:pairs example))))
  (is (= [[0 13] [38 41] [556 569] [14 29] [6 19] [10 23] [893 937] [24 37] [7 17]]
         (simplify-mod-pairs (:pairs problem)))))

(with-test
  (defn bézout-coefficients
    "Implements the Extended Euclid algorithm to find two numbers [x y] such that
     x×a + b×y = 1.  Assumes a and b are at least co-prime."
    [[a b]]
    (loop [r0 a r1 b
           s0 1 s1 0
           t0 0 t1 1]
      (if (zero? r1)
        [s0 t0]
        (recur r1 (- r0 (* (quot r0 r1) r1))
               s1 (- s0 (* (quot r0 r1) s1))
               t1 (- t0 (* (quot r0 r1) t1))))))
  (is (= [-1 1] (bézout-coefficients [3 4])))
  (is (= [-2 5] (bézout-coefficients [12 5]))))


(with-test
  (defn reduce-eqn
    "Combines two constraints [x ≡ a₁ (mod n₁)] and [x ≡ a₂ (mod n₂)] into an equivalent
     single constraint [x ≡ a₁×b₂×n₂ + a₂×b₁×n₁ (mod n₁×n₂)], where b₁ and b₂ are the Bézout
     coefficients for n₁ and n₂.
     Results are wrapped in clojure Number objects as they're likely to get quite big."
    [[a₁ n₁] [a₂ n₂]]
    (let [[b₁ b₂] (bézout-coefficients [n₁ n₂])
          new-remainder (+' (*' a₁ b₂ n₂) (*' a₂ b₁ n₁))
          new-modulus (* n₁ n₂)]
      (reduce-to-smallest-form [new-remainder new-modulus])))
  (is (= [3 12] (reduce-eqn [0 3] [3 4]))))


(def example-pairs-0 (parse-pairs "17,x,13,19"))
(def example-pairs-1 (parse-pairs "67,7,59,61"))
(def example-pairs-2 (parse-pairs "67,x,7,59,61"))
(def example-pairs-3 (parse-pairs "67,7,x,59,61"))
(def example-pairs-4 (parse-pairs "1789,37,47,1889"))

(with-test
  (defn reduce-system
    "Reduces a collection of [a n] pairs (each representing the identity x ≡ a (mod n))
     to a single pair [A N], where:
      - `A` is the smallest solution to the system of equations, and
      - `N` is the period with which it repeats.
     It does this by reducing each pair of constraints to a single equivalent constraint."
    [[eq1 eq2 & pairs]]
    (if (nil? eq2)
      eq1
      (recur (cons (reduce-eqn eq1 eq2) pairs))))
  (is (= [39 60]
         (reduce-system [[0 3] [3 4] [4 5]])))
  (is (= [3417 (reduce * (map second example-pairs-0))]
         (reduce-system (simplify-mod-pairs example-pairs-0))))
  (is (= [754018 (reduce * (map second example-pairs-1))]
         (reduce-system (simplify-mod-pairs example-pairs-1))))
  (is (= [779210 (reduce * (map second example-pairs-2))]
         (reduce-system (simplify-mod-pairs example-pairs-2))))
  (is (= [1261476 (reduce * (map second example-pairs-3))]
         (reduce-system (simplify-mod-pairs example-pairs-3))))
  (is (= [1202161486 (reduce * (map second example-pairs-4))]
         (reduce-system (simplify-mod-pairs example-pairs-4))))
  (is (= [1068781 3162341]
         (reduce-system (simplify-mod-pairs (:pairs example))))))


(comment

  ; Part 1
  (= 2845
     (->> (lowest-wait-for-services (:numbers problem) (:target problem))
          (reduce *)))


  ; Part 2
  (= 487905974205117N
     (->> (:pairs problem)
          (simplify-mod-pairs)
          (reduce-system)
          (first))))
