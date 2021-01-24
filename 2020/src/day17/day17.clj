(ns day17
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.test :refer [with-test is]]))


(defn grid-to-active-map
  "Maps a list of {#.} characters into a set of 2D points, where each point indicates the
   presence of a # character.  This assumes the top-left of the grid is (0,0).  Once built,
   a provided transform is applied to each point, which can be used to translate the origin
   or to add dimensions."
  [grid xform]
  (->> (for [y (range (count grid))
             x (range (count (first grid)))
             :when (= \# (get-in grid [y x]))]
         [x y])
       (map xform)
       (set)))
(defn xform 
  "Translates a 2D coordinate by an N-dimensional vector."
  [[i j & rest]] (fn [[x y]] (concat [(+ x i) (+ y j)] rest)))

(defn parse-input [input top-left]
  (-> (string/split-lines input)
      (grid-to-active-map (xform top-left))))

(def example-input ".#.\n..#\n###")
(with-test
  (def parsed-example-input-3d (parse-input example-input [0 0 0]))
  (is (= #{[1 0 0] [2 1 0] [0 2 0] [1 2 0] [2 2 0]} parsed-example-input-3d)))
(with-test
  (def parsed-example-input-4d (parse-input example-input [0 0 0 0]))
  (is (= #{[1 0 0 0] [2 1 0 0] [0 2 0 0] [1 2 0 0] [2 2 0 0]} parsed-example-input-4d)))

(defonce problem-input (delay (slurp "src/day17/input.txt")))
(def parsed-problem-input-3d (delay (parse-input @problem-input [0 0 0])))
(def parsed-problem-input-4d (delay (parse-input @problem-input [0 0 0 0])))



(defn combinations-of
  "Returns a list of every possible combination of a list of lists.
   For example, (combination-of [[:a :b] [:c :d]]) => '([:a :c] [:a :d] [:b :c] [:b :d])"
  [leading [head & rest]]
  (let [combinations (map #(vec (conj (vec leading) %)) head)]
    (if (nil? rest)
      combinations
      (->> (map #(combinations-of % rest) combinations)
           (apply concat)))))

(defn find-neighbours-and-self [coord]
  (->> (map (fn [n] [(dec n) n (inc n)]) coord)
       (combinations-of [])))
(with-test
  (defn find-neighbours [coord]
    (->> (find-neighbours-and-self coord)
         (filter #(not= coord %))
         (set)))
  (is (= 26 (count (find-neighbours [0 0 0]))))
  (is (= 80 (count (find-neighbours [0 0 0 0]))))
  (is (not-any? #{[0 0 0]} (find-neighbours [0 0 0])))
  (is (not-any? #{[0 0 0 0]} (find-neighbours [0 0 0 0])))
  (is (contains? (find-neighbours [1 2 3]) [2 2 2]))
  (is (contains? (find-neighbours [1 2 3]) [0 2 3]))
  (is (contains? (find-neighbours [1 2 3 4]) [2 2 3 3]))
  (is (contains? (find-neighbours [1 2 3 4]) [0 2 3 4])))

(with-test
  (defn find-active-neighbours [active-coords coord]
    (set/intersection active-coords (find-neighbours coord)))
  (is (= #{[1 0 0]} (find-active-neighbours parsed-example-input-3d [0 0 0])))
  (is (= #{[1 0 0]} (find-active-neighbours parsed-example-input-3d [0 0 -1])))
  (is (= #{[1 0 0 0]} (find-active-neighbours parsed-example-input-4d [0 0 0 0])))
  (is (= #{[1 0 0 0]} (find-active-neighbours parsed-example-input-4d [0 0 -1 0])))
  (is (= parsed-example-input-3d (find-active-neighbours parsed-example-input-3d [1 1 0])))
  (is (= parsed-example-input-4d (find-active-neighbours parsed-example-input-4d [1 1 0 0]))))


(def ruleset {:active #{2 3}
              :inactive #{3}})

(with-test
  (defn new-state
    "Determines whether a point should be active in the next cycle, based on the current state of
     the provided coordinate, and a provided ruleset.  If it should be active, the point is returned.
     If it should be inactive, nil is returned.
     The ruleset should be a mapping of :active or :inactive to a boolean function which determines
     whether the current point should be activated when provided with the number of activated neighbours."
    [ruleset active-coords coord]
    (let [active-neigbours (count (find-active-neighbours active-coords coord))
          old-state (if (active-coords coord) :active :inactive)]
      (if ((old-state ruleset) active-neigbours)
        coord
        nil)))
  (is (= [0 1 0] (new-state ruleset parsed-example-input-3d [0 1 0])))
  (is (= [1 2 0] (new-state ruleset parsed-example-input-3d [1 2 0])))
  (is (nil? (new-state ruleset parsed-example-input-3d [0 0 0])))
  (is (= [0 1 0 0] (new-state ruleset parsed-example-input-4d [0 1 0 0])))
  (is (= [1 2 0 0] (new-state ruleset parsed-example-input-4d [1 2 0 0])))
  (is (nil? (new-state ruleset parsed-example-input-4d [0 0 0 0]))))


(defn all-coords-in-next-cycle
  "Compiles (naïvely) a set of all coordinates that could possibly be active in the next cycle, and so
   need checking.  This implementation uses the neighbour of every active coordinate, but it may be
   quicker in some dense states to iterate every possible coordinate within a bounding box."
  [active-coords]
  (->> (map find-neighbours-and-self active-coords)
       (apply set/union)))

(defn at-z-of [n coords] (set (filter (fn [[_ _ z]] (= n z)) coords)))
(with-test
  (defn next-cycle
    "Applies the new-state function to every point that needs considering in the next cycle,
     and returns a new set of these points, representing the next cycle's state."
    [ruleset active-coords]
    (->> (all-coords-in-next-cycle active-coords)
         (keep (partial new-state ruleset active-coords))
         (set)))
  (is (= (grid-to-active-map ["#.."
                              "..#"
                              ".#."] (xform [0 1 -1]))
         (at-z-of -1 (next-cycle ruleset parsed-example-input-3d))))
  (is (= (grid-to-active-map ["#.#"
                              ".##"
                              ".#."] (xform [0 1 0]))
         (at-z-of 0 (next-cycle ruleset parsed-example-input-3d))))
  (is (= (grid-to-active-map ["#.."
                              "..#"
                              ".#."] (xform [0 1 1]))
         (at-z-of 1 (next-cycle ruleset parsed-example-input-3d)))))


(with-test
  (defn cycle-n-times [ruleset active-coords n]
    (if (<= n 0)
      active-coords
      (recur ruleset (next-cycle ruleset active-coords) (dec n))))
  (is (= 112 (count (cycle-n-times ruleset parsed-example-input-3d 6))))
  ; "Elapsed time: 13768.074952 msecs" :(
  #_(is (= 848 (time (count (cycle-n-times ruleset parsed-example-input-4d 6))))))


(comment

  (require '[flames.core :as flames])
  (def flames (flames/start! {:port 54321, :host "localhost"}))
  (count (cycle-n-times ruleset parsed-example-input-4d 10))
  (flames/stop! flames)

  ; Part 1
  (= 280
     (count (cycle-n-times ruleset @parsed-problem-input-3d 6)))

  ; Part 2
  ; "Elapsed time: 34557.896584 msecs" :(
  (= 1696
     (time (count (cycle-n-times ruleset @parsed-problem-input-4d 6))))

  @parsed-problem-input-3d)
