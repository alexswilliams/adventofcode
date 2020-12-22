(ns day20.day20
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))

(defn parse-input [input]
  (let [id-from-first-line (fn [line] (->> (re-seq #"^Tile ([0-9]+):$" line)
                                           (first)
                                           (second)
                                           (Long/valueOf)))
        lines-to-structure (fn [lines] {:id (id-from-first-line (first lines))
                                        :orientation :no-change
                                        :top (vec (second lines))
                                        :bottom (vec (last lines))
                                        :left (vec (flatten (map first (drop 1 lines))))
                                        :right (vec (flatten (map last (drop 1 lines))))})]
    (->> (string/split input #"\r?\n\r?\n")
         (map string/split-lines)
         (map lines-to-structure))))

(def example-input (delay (slurp "src/day20/example.txt")))
(def parsed-example-input (delay (parse-input @example-input)))

(def problem-input (delay (slurp "src/day20/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))

(defn all-transformations
  "Produces a vector of all transformations made through rotating and flipping.
   There are only 8 main transformations that aren't symmetrical to others.  All others
   are symmetries, e.g. R1,FV,R1 is just FV"
  [tile]
  [tile
   {:id (:id tile)
    :orientation :rot-90-cw
    :top (reverse (:left tile))
    :bottom (reverse (:right tile))
    :left (:bottom tile)
    :right (:top tile)}
   {:id (:id tile)
    :orientation :rot-180-cw
    :top (reverse (:bottom tile))
    :bottom (reverse (:top tile))
    :left (reverse (:right tile))
    :right (reverse (:left tile))}
   {:id (:id tile)
    :orientation :rot-270-cw
    :top (:right tile)
    :bottom (:left tile)
    :left (reverse (:top tile))
    :right (reverse (:bottom tile))}
   {:id (:id tile)
    :orientation :flip-vert
    :top (reverse (:top tile))
    :bottom (reverse (:bottom tile))
    :left (:right tile)
    :right (:left tile)}
   {:id (:id tile)
    :orientation :flip-horiz
    :top (:bottom tile)
    :bottom (:top tile)
    :left (reverse (:left tile))
    :right (reverse (:right tile))}
   {:id (:id tile)
    :orientation :rot-90-cw-then-flip-vert
    :top (:left tile)
    :bottom (:right tile)
    :left (:top tile)
    :right (:bottom tile)}
   {:id (:id tile)
    :orientation :rot-90-cw-then-flip-horiz
    :top (reverse (:right tile))
    :bottom (reverse (:left tile))
    :left (reverse (:bottom tile))
    :right (reverse (:top tile))}])


(def example-solution #{[1951 :flip-horiz]
                        [2311 :flip-horiz]
                        [3079 :no-change]
                        [2729 :flip-horiz]
                        [1427 :flip-horiz]
                        [2473 :rot-90-cw-then-flip-horiz]
                        [2971 :flip-horiz]
                        [1489 :flip-horiz]
                        [1171 :flip-vert]})

(def all-example-xforms (->> (map all-transformations @parsed-example-input)
                             (reduce concat [])))
(def full-example-solution
  (filter #(example-solution [(:id %) (:orientation %)]) all-example-xforms))



#_(with-test
  (defn do-distinct-tiles-abut? [a b]
    (and (not= (:id a) (:id b))
         (or (= (:top a) (:bottom b))
             (= (:bottom a) (:top b))
             (= (:left a) (:right b))
             (= (:right a) (:left b)))))
  (is (do-distinct-tiles-abut? (some #({2311 %} (:id %)) full-example-solution)
                               (some #({1951 %} (:id %)) full-example-solution))))


(def opposite-side-to {:top :bottom :bottom :top :left :right :right :left})
(defn find-candidates [tiles-superset tile-side tile]
  (filter #(and (not= (:id tile) (:id %))
                (= (tile-side tile)
                   ((opposite-side-to tile-side) %)))
          tiles-superset))

(defn abutting-tile-possibilities [tiles-superset tile]
  (-> tile
      (assoc :top-candidates (find-candidates tiles-superset :top tile))
      (assoc :bottom-candidates (find-candidates tiles-superset :bottom tile))
      (assoc :left-candidates (find-candidates tiles-superset :left tile))
      (assoc :right-candidates (find-candidates tiles-superset :right tile))))


(defn tiles-with-edge-candidates [tiles]
  (let [all-xforms (->> (map all-transformations tiles)
                        (reduce concat []))]
    (map (partial abutting-tile-possibilities all-xforms)
         tiles)))

(def example-with-candidates (tiles-with-edge-candidates @parsed-example-input))

(defn is-corner? [tile]
  (= 2 (+ (if (empty? (:top-candidates tile)) 1 0)
          (if (empty? (:bottom-candidates tile)) 1 0)
          (if (empty? (:left-candidates tile)) 1 0)
          (if (empty? (:right-candidates tile)) 1 0))))

(comment
  
  ; Part 1
  (->> (tiles-with-edge-candidates @parsed-problem-input)
       (filter is-corner?)
       (map :id)
       (distinct)
       (reduce *))
  ; 111936085519519
)