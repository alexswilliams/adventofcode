(ns day8.day8
  (:require [clojure.string :as string]))


(defn parse-input [input] (->> (string/split-lines input)
                               (map (partial re-seq #"([a-z]+) ([-+0-9]+)"))
                               (map first)
                               (map (fn [line] 
                                      {:op  (keyword (nth line 1))
                                       :val (Long/valueOf (nth line 2))}))))

(defonce problem-input (delay (slurp "src/day8/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))

(defonce example-input (delay (slurp "src/day8/example.txt")))
(def parsed-example-input (delay (parse-input @example-input)))


(def initial-state
  {:pc      0
   :acc     0
   :visited []})

(defn step [state program]
  (let [pc          (:pc state)
        instruction (nth program pc)
        op          (:op instruction)
        value       (:val instruction)
        visited     (conj (:visited state) pc)]
    (case op
      :nop {:pc      (inc pc)
            :acc     (:acc state)
            :visited visited}
      :acc {:pc      (inc pc)
            :acc     (+ (:acc state) value)
            :visited visited}
      :jmp {:pc      (+ pc value)
            :acc     (:acc state)
            :visited visited})))

(defn stop-just-before-loop [program]
  (loop [state initial-state]
    (let [post-step (step state program)]
      (if (.contains (:visited post-step) (:pc post-step))
        post-step
        (recur post-step)))))

(defn stop-just-before-loop-or-on-halt [program]
  (loop [state initial-state]
    (let [post-step      (step state program)
          has-inf-looped (.contains (:visited post-step) (:pc post-step))
          has-halted     (= (count program) (:pc post-step))]
      (if has-halted
        {:acc   (:acc post-step)
         :state :halted}

        (if has-inf-looped
          {:state :inf-looped}

          (recur post-step))))))


(defn gen-modified-programs
  "Creates a lazy sequence of possible alternative programs, generated by
   swapping every nop for a jmp and vice versa, one by one."
  [program]
  (let [program           (vec program)
        flippable-indexes (->> (range 0 (dec (count program)))
                               (filter #(#{:nop :jmp} (:op (nth program %)))))] 
    (map #(let [instruction (nth program %)]
            (assoc program 
                   % 
                   {:op  ({:nop :jmp
                           :jmp :nop} (:op instruction))
                    :val (:val instruction)}))
         flippable-indexes)))


(defn try-modifications-until-halt-or-exhausted [program]
  (let [programs (gen-modified-programs program)]
    (loop [remaining-programs programs]
      
      (if (empty? remaining-programs)
        :exhausted
        
        (let [program-result (stop-just-before-loop-or-on-halt (first remaining-programs))]
          (if (= :halted (:state program-result))
            program-result
            
            (recur (drop 1 remaining-programs))))))))


(comment

  ; Part 1
  (= (:acc (stop-just-before-loop @parsed-example-input))
     5)  
  (= (:acc (stop-just-before-loop @parsed-problem-input))
     1610)
  
  ; Part 2
  (= (:acc (try-modifications-until-halt-or-exhausted @parsed-example-input))
     8)
  (= (:acc (try-modifications-until-halt-or-exhausted @parsed-problem-input))
     1703))