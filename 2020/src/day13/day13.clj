(ns day13.day13)

(def nums-example [7 13 59 31 19])
(def target-example 939)
(def nums-problem [13 41 569 29 19 23 937 37 17]) ; These are all suspiciously prime numbers >.>
(def target-problem 1007125)


; Part 1
(defn lowest-wait [map] [(apply min-key map (keys map)) (apply min (vals map))])
(defn lowest-wait-for-services [services target]
  (->> (map #(- % (rem target %)) services)
       (zipmap services)
       (lowest-wait)))
(comment
  (= [59 5]
     (lowest-wait-for-services nums-example target-example))

  (= [569 5]
     (lowest-wait-for-services nums-problem target-problem))
  (= 2845 (* 569 5)))


; Part 2
; I'm aware this is just the chinese remainder theorem, but i have zero excitement
; for dredging up my school days right now.  Ask me again another day!