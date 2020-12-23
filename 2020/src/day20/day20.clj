(ns day20.day20
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))



(defn flip-horiz [grid] (vec (reverse grid)))
(defn flip-vert [grid] (vec (map (comp vec reverse) grid)))
(defn rot-90-cw [grid]
  (vec (map (fn [row-num] (vec (map #(nth % row-num)
                                    (reverse grid))))
            (range (count grid)))))
(def xform-func {:start #(vec (map vec %))
                 :rot-90-cw rot-90-cw
                 :rot-180-cw #(rot-90-cw (rot-90-cw %))
                 :rot-270-cw #(rot-90-cw (rot-90-cw (rot-90-cw %)))
                 :flip-horiz flip-horiz
                 :flip-vert flip-vert
                 :rot-90-cw-then-flip-horiz #(flip-horiz (rot-90-cw %))
                 :rot-90-cw-then-flip-vert #(flip-vert (rot-90-cw %))})

(defn start-tile-to-tile-edges
  "Applies the transformation given by `orientation` and returns a tile with the transformed body
   along with the top, bottom, left and right edges cached as keys.  Note: `tile` should have an
   orientation of :start, otherwise the :orientation key will be incorrect in the result."
  [orientation tile]
  (let [xformed-body ((xform-func orientation) (:body tile))]
    {:id (:id tile)
     :orientation orientation
     :key [(:id tile) orientation]
     :body xformed-body
     :top (vec (first xformed-body))
     :bottom (vec (last xformed-body))
     :left (vec (map first xformed-body))
     :right (vec (map last xformed-body))}))


(defn parse-input [input]
  (let [id-from-first-line (fn [line] (->> (re-seq #"^Tile ([0-9]+):$" line)
                                           (first)
                                           (second)
                                           (Long/valueOf)))
        lines-to-structure (fn [lines] (start-tile-to-tile-edges :start
                                                                 {:id (id-from-first-line (first lines))
                                                                  :body (drop 1 lines)}))]
    (->> (string/split input #"\r?\n\r?\n")
         (map string/split-lines)
         (map lines-to-structure))))

(defonce example-input (slurp "src/day20/example.txt"))
(def parsed-example-input (parse-input example-input))

(defonce problem-input (delay (slurp "src/day20/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))


(defn all-transformations
  "Produces a vector of all transformations made through rotating and flipping.
   There are only 8 main transformations that aren't symmetrical to others.  All others
   are symmetries, e.g. R1,FV,R1 is just FV"
  [tile]
  [tile
   (start-tile-to-tile-edges :rot-90-cw tile)
   (start-tile-to-tile-edges :rot-180-cw tile)
   (start-tile-to-tile-edges :rot-270-cw tile)
   (start-tile-to-tile-edges :flip-horiz tile)
   (start-tile-to-tile-edges :flip-vert tile)
   (start-tile-to-tile-edges :rot-90-cw-then-flip-horiz tile)
   (start-tile-to-tile-edges :rot-90-cw-then-flip-vert tile)])
(def example-all-xforms (mapcat all-transformations parsed-example-input))


(def opposite-side-to {:top :bottom :bottom :top :left :right :right :left})
(with-test
  (defn find-candidates
    "Searches through `tiles-superset` (the flat collection of all possible transformation of
     all tiles) for all the tiles which could abut the provided `tile` on its given `tile-side`.
     Returns a set of pairs [id orientation] of all tiles which could abut this edge."
    [tiles-superset tile-side tile]
    (->> tiles-superset
         (filter #(not= (:id tile) (:id %)))
         (filter #(= (tile-side tile) ((opposite-side-to tile-side) %)))
         (map #(vector (:id %) (:orientation %)))
         (set)))

  (is (= #{[1427 :flip-horiz]}
         (find-candidates example-all-xforms :bottom
                          (some #({[2311 :flip-horiz] %} [(:id %) (:orientation %)]) example-all-xforms)))))


(defn abutting-tile-possibilities
  "Finds the possible 'candidate' abutting tiles against each edge of the given `tile`"
  [tiles-superset tile]
  {:id (:id tile)
   :orientation (:orientation tile)
   :key [(:id tile) (:orientation tile)]
   :top-candidates (find-candidates tiles-superset :top tile)
   :bottom-candidates (find-candidates tiles-superset :bottom tile)
   :left-candidates (find-candidates tiles-superset :left tile)
   :right-candidates (find-candidates tiles-superset :right tile)})


(defn tiles-with-abutment-candidates
  "Helper function to transform the problem input into a set of abutment candidates"
  [tiles]
  (let [all-xforms (mapcat all-transformations tiles)]
    (map (partial abutting-tile-possibilities all-xforms)
         all-xforms)))
(def example-abutments (tiles-with-abutment-candidates parsed-example-input))


(defn is-top-left-corner? [tile] (and (empty? (:top-candidates tile)) (empty? (:left-candidates tile))))
(defn is-bottom-left-corner? [tile] (and (empty? (:bottom-candidates tile)) (empty? (:left-candidates tile))))
(defn is-top-right-corner? [tile] (and (empty? (:top-candidates tile)) (empty? (:right-candidates tile))))
(defn is-bottom-right-corner? [tile] (and (empty? (:bottom-candidates tile)) (empty? (:right-candidates tile))))

(with-test
  (defn is-corner? [tile] (or (is-top-left-corner? tile)
                              (is-top-right-corner? tile)
                              (is-bottom-left-corner? tile)
                              (is-bottom-right-corner? tile)))
  (is (is-corner? (some #({1171 %} (:id %)) example-abutments)))
  (is (not (is-corner? (some #({2311 %} (:id %)) example-abutments)))))


(with-test
  (defn find-top-left [abutments]
    (->> (filter is-top-left-corner? abutments)
         (filter #(#{:start} (:orientation %))) ; Remove symmetry - there are 8 xforms of the solution
         (first)))
  (is (= [2971 :start]
         (->> (find-top-left example-abutments) (:key)))))
(def example-top-left (find-top-left example-abutments))


(defn tile-map-from-all-tiles
  "Creates a mapping from [id orientation] to a tile object.
   Can be used with either abutment lists or with tile lists."
  [all-tiles]
  (zipmap (map :key all-tiles) all-tiles))
(def example-tile-map (tile-map-from-all-tiles example-abutments))


(with-test
  (defn build-row-ids-from-leftmost-tile
    "Builds a row of [id orientation] pairs based on the :right-candidate links within each tile.
     Works left to right, starting with the `leftmost-tile`."
    [tile-map leftmost-tile]
    (loop [tile leftmost-tile
           row-so-far []]
      (cond
        (empty? (:right-candidates tile)) (->> (concat row-so-far [tile]) (map :key))
        (not= 1 (count (:right-candidates tile))) (throw (Exception. (.concat "Tile had more than one possible right-candidate: " (.toString tile))))
        :else (let [id-to-the-right (first (:right-candidates tile))]
                (recur (tile-map id-to-the-right)
                       (concat row-so-far [tile]))))))

  (is (= [[2971 :start] [1489 :start] [1171 :rot-180-cw]]
         (->> (build-row-ids-from-leftmost-tile example-tile-map example-top-left)))))

(with-test
  (defn build-grid-ids-from-top-left-tile
    "Forms a grid, beginning at the provided `top-left-tile`, by following the :*-candidate
     links within each tile.  `tile-map` is used as dictionary of ids and orientations into
     the abutment list."
    [tile-map top-left-tile]
    (loop [leftmost-tile top-left-tile
           grid-so-far []]
      (let [current-row (build-row-ids-from-leftmost-tile tile-map leftmost-tile)]
        (cond
          (empty? (:bottom-candidates leftmost-tile)) (concat grid-so-far [current-row])
          (not= 1 (count (:bottom-candidates leftmost-tile))) (throw (Exception. (.concat "Tile had more than one possible bottom-candidate:" (.toString leftmost-tile))))
          :else (let [id-to-bottom (first (:bottom-candidates leftmost-tile))]
                  (recur (tile-map id-to-bottom)
                         (concat grid-so-far [current-row])))))))

  (is (= [[[2971 :start] [1489 :start] [1171 :rot-180-cw]]
          [[2729 :start] [1427 :start] [2473 :rot-90-cw]]
          [[1951 :start] [2311 :start] [3079 :flip-horiz]]]
         (build-grid-ids-from-top-left-tile example-tile-map example-top-left))))

(def example-grid-ids (build-grid-ids-from-top-left-tile example-tile-map example-top-left))



(defn inner-body
  "Trims the outer edges of a body by 1 row/column in every direction."
  [tile]
  (->> (:body tile)
       (drop 1)
       (drop-last)
       (map (partial drop 1))
       (map drop-last)))

(defn row-to-grid-data
  "Reduces a row of tiles to a collection of full-width rows, where each tile has had its
   borders trimmed and has been joined to its adjacent tiles."
  [all-tile-xforms row]
  (->> (map (fn [key] (inner-body (some #({key %} (:key %)) all-tile-xforms))) row)
       (apply map vector)
       (map flatten)))

(defn inner-grid-from-id-grid
  "Splice together the inner-bodies of all tiles to a single 2d grid."
  [id-grid all-tile-xforms]
  (->> (map (partial row-to-grid-data all-tile-xforms) id-grid)
       (apply concat)
       (map vec)
       (vec)))

(with-test
  (def example-inner-grid (inner-grid-from-id-grid example-grid-ids example-all-xforms))
  (is (= [[\. \. \. \# \# \# \. \. \. \# \# \. \. \. \# \. \. \. \# \. \. \# \# \#]
          [\. \# \. \# \# \# \. \. \# \# \. \. \# \# \. \. \# \# \# \# \. \# \# \.]
          [\# \. \# \# \. \. \# \. \. \# \. \. \. \# \. \. \# \# \# \# \. \. \. \#]
          [\# \# \# \# \# \. \. \# \# \# \# \# \. \. \. \# \# \# \. \. \. \. \# \#]
          [\# \. \. \# \# \# \# \. \. \. \# \. \# \. \# \. \# \# \# \. \# \# \# \.]
          [\. \. \# \. \# \. \. \# \. \. \# \. \# \. \# \. \# \# \# \# \. \# \# \#]
          [\. \# \# \# \# \. \# \# \# \. \# \. \. \. \# \# \# \. \# \. \. \# \. \#]
          [\. \# \. \# \. \# \# \# \. \# \# \. \# \# \. \# \. \. \# \. \# \# \. \.]
          [\# \# \# \. \# \. \. \. \# \. \. \# \. \# \# \. \# \# \# \# \# \# \. \.]
          [\. \# \. \# \. \. \. \. \# \. \# \# \. \# \. \. \. \# \# \# \. \# \# \.]
          [\. \. \. \# \. \. \# \. \. \# \. \# \. \# \# \. \. \# \# \# \. \# \# \#]
          [\# \# \. \. \# \# \. \# \. \. \. \# \. \. \. \# \. \# \. \# \. \# \. \.]
          [\# \. \# \# \# \# \. \. \. \. \# \# \. \. \# \# \# \# \# \# \# \# \. \#]
          [\# \# \# \. \# \. \# \. \. \. \# \. \# \# \# \# \# \# \. \# \. \. \# \#]
          [\# \. \# \# \# \# \. \. \# \. \# \# \# \# \. \# \. \# \. \# \# \# \. \.]
          [\# \. \. \# \. \# \# \. \. \# \. \. \# \# \# \. \# \. \# \# \. \. \. \.]
          [\. \# \# \# \# \. \. \. \# \. \. \# \. \. \. \. \. \# \. \. \. \. \. \.]
          [\. \. \. \. \# \. \. \# \. \. \. \# \# \. \. \# \. \# \. \# \# \# \. \.]
          [\. \. \. \# \# \# \# \# \# \# \# \. \# \. \. \. \. \# \# \# \# \# \. \#]
          [\# \# \. \# \. \. \. \. \# \. \# \# \. \# \# \# \# \. \. \. \# \. \# \#]
          [\# \# \# \. \# \# \# \# \# \. \. \. \# \. \# \# \# \# \# \. \# \. \. \#]
          [\# \# \. \# \# \. \# \# \# \. \# \. \# \. \. \# \# \# \# \# \# \. \. \.]
          [\# \# \# \. \. \. \. \# \. \# \. \. \. \. \# \. \. \# \. \. \. \. \. \.]
          [\. \# \. \# \. \. \# \. \# \# \. \. \. \# \. \# \# \. \. \# \# \# \# \#]]
         example-inner-grid)))


(defn all-xformed-inner-grids
  "Takes a `grid` and return a list of all 8 grids derived from the 8 possible 2D transformations."
  [grid]
  [grid
   ((xform-func :rot-90-cw) grid)
   ((xform-func :rot-180-cw) grid)
   ((xform-func :rot-270-cw) grid)
   ((xform-func :flip-horiz) grid)
   ((xform-func :flip-vert) grid)
   ((xform-func :rot-90-cw-then-flip-horiz) grid)
   ((xform-func :rot-90-cw-then-flip-vert) grid)])
(def example-xformed-inner-grids (all-xformed-inner-grids example-inner-grid))



(def sea-monster-strings ["                  # "
                          "#    ##    ##    ###"
                          " #  #  #  #  #  #   "])

(defn index-of-matches
  "Looks for an item in a collection, and return the list of indexes it was found at."
  [char line]
  (map first (filter #(= char (second %)) (map-indexed vector line))))

(with-test
  (def sea-monster-shape (->> (map (partial index-of-matches \#) sea-monster-strings)
                              (map-indexed (fn [i matches] (map #(vector % i) matches)))
                              (apply concat)
                              (vec)))
  (is (= [[18 0]
          [0 1] [5 1] [6 1] [11 1] [12 1] [17 1] [18 1] [19 1]
          [1 2] [4 2] [7 2] [10 2] [13 2] [16 2]]
         sea-monster-shape)))


(with-test
  (defn shape-at-coords
    "Takes a `shape` represented by a list of coordinates (top left at [0 0]) and
     offsets it by a set of coordinates [x y].  Returns true if the `grid` is entirely
     wave (#) pixels beneath each of these offset coordinates."
    [shape grid [x y]]
    (->> (map (fn [[shape-x shape-y]]
                (get-in grid [(+ y shape-y) (+ x shape-x)]))
              shape)
         (every? #(= % \#))))

  (is (= true (shape-at-coords sea-monster-shape
                               (nth example-xformed-inner-grids 1)
                               [2 2])))
  (is (= true (shape-at-coords sea-monster-shape
                               (nth example-xformed-inner-grids 1)
                               [1 16]))))
(comment
  (get-in (nth example-xformed-inner-grids 4) [1 16])
  (count (nth example-xformed-inner-grids 4)))


(defn count-sea-monsters-in-grid
  "For the given `grid`, walk through every combination of coordinates that the monster could
   appear at, and test whether a monster is at those coordinates.  Returns the number of
   coordinates which had a monster."
  [grid]
  (let [height (count grid)
        width height
        starting-coords (for [y (range (- height 2))
                              x (range (- width 19))]
                          [x y])
        monster-at-coords (map (partial shape-at-coords sea-monster-shape grid)
                               starting-coords)
        monsters-on-grid (count (filter identity monster-at-coords))]
    monsters-on-grid))


(with-test
  (defn choppiness-of-sea
    "Finds the grid orientation with the most monsters, and subtracts the number of monster-
     covered wave (#) pixels from the total number of wave pixels."
    [inner-grid]
    (let [all-grids (all-xformed-inner-grids inner-grid)
          monsters-on-grid (apply max (map count-sea-monsters-in-grid all-grids))
          waves-taken-up-by-monsters (* monsters-on-grid (count sea-monster-shape))]
      (->> (map (partial filter #(= \# %)) inner-grid)
           (map count)
           (reduce +)
           (#(- % waves-taken-up-by-monsters)))))
  (is (= 273
         (choppiness-of-sea example-inner-grid))))




(def problem-all-xforms (delay (mapcat all-transformations @parsed-problem-input)))
(def problem-abutments (delay (tiles-with-abutment-candidates @parsed-problem-input)))
(def problem-grid-ids (delay (build-grid-ids-from-top-left-tile (tile-map-from-all-tiles @problem-abutments)
                                                                (find-top-left @problem-abutments))))
(def problem-inner-grid (delay (inner-grid-from-id-grid @problem-grid-ids @problem-all-xforms)))

(comment

  ; Part 1
  (= 111936085519519 (->> @problem-abutments
                          (filter is-corner?)
                          (map :id)
                          (distinct)
                          (reduce *)))

  ; Part 2
  (= 1792 (choppiness-of-sea @problem-inner-grid)))
