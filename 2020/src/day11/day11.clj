
(ns day11.day11
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))



(defn parse-input [input] (->> (string/split-lines input)
                               (vec)
                               ((fn [layout] {:width  (count (first layout))
                                              :height (count layout)
                                              :layout layout}))))

(defonce problem-input (delay (slurp "src/day11/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))

(defonce example-input (delay (slurp "src/day11/example.txt")))
(def parsed-example-input (delay (parse-input @example-input)))



(with-test
  (defn rays-from
    "Gather coordinates hit by casting a ray from [row col] in steps of [dv dh]
     whilst bounded by a box of dimensions [height width].  Does not include [row col]
     in the output.
     Passing limit := nil will use the bounding box as a limit; otherwise specifying
     a limit will truncate the result to a maximum of `limit` number of entries."
    [limit [row col] [height width] [dv dh]]
    (->> (range) ; 0, 1, 2, 3, ...
         (map inc); 1, 2, 3, 4, ...
         (map (fn [i] [(* dv i) (* dh i)])); [0,1], [0,2], [0,3], [0,4], ...
         (map (fn [[r c]] [(+ row r) (+ col c)])); [5,2], [5,3], [5,4], [5,5], ...
         (take (min (if (nil? limit) (max height width) limit)))
         (filter (fn [[r c]] (and (>= r 0)
                                  (>= c 0)
                                  (< r height)
                                  (< c width))))))

  (is (= '([4 2] [3 3] [2 4] [1 5] [0 6])
         (rays-from 10 [5 1] [10 10] [-1 1])))
  (is (= '([4 2] [3 3])
         (rays-from 2 [5 1] [10 10] [-1 1])) "Applying a limit will truncate the result")
  (is (= '([4 5] [5 4] [6 3] [7 2] [8 1] [9 0])
         (rays-from nil [3 6] [10 10] [1 -1]))))


(with-test
  (defn raytrace-occupied-seats
    "Maps rays of coordinates into the first encountered seats in each direction, and
     counts how many of those are occupied."
    [limit {:keys [width height layout]} [row col]]
    (let [directions [[-1 0] [+1 0] [0 -1] [0 +1] [-1 -1] [-1 +1] [+1 -1] [+1 +1]]]
      (->> directions
           (map (partial rays-from limit [row col] [height width]))
           (map (fn [ray] (map #(get-in layout %) ray)))
           (map (partial some #{\L \#}))
           (filter #{\#})
           (count))))

  (is (= 2 (raytrace-occupied-seats
            1 {:width 2 :height 2 :layout ["#."
                                           "##"]} [0 0])))
  (is (= 4 (raytrace-occupied-seats
            1 {:width 10 :height 2 :layout ["#.##.##.##"
                                            "#######.##"]} [0 3])))
  (is (= 8 (raytrace-occupied-seats
            9 {:width 9 :height 9 :layout [".......#."
                                           "...#....."
                                           ".#......."
                                           "........."
                                           "..#L....#"
                                           "....#...."
                                           "........."
                                           "#........"
                                           "...#....."]} [4 3])))
  (is (= 0 (raytrace-occupied-seats
            13 {:width 13 :height 3 :layout ["............."
                                             ".L.L.#.#.#.#."
                                             "............."]} [1 1])))
  (is (= 0 (raytrace-occupied-seats
            7 {:width 7 :height 7 :layout [".##.##."
                                           "#.#.#.#"
                                           "##...##"
                                           "...L..."
                                           "##...##"
                                           "#.#.#.#"
                                           ".##.##."]} [3 3]))))


(defn new-seat-state
  "Calculates new state of a seat given the occupancy of surrounding seats.
   If `occupancy-threshold` is breached, a seat will return to being empty."
  [occupancy-func occupancy-threshold state row col seat]
  (let [occupied (occupancy-func state [row col])]
    (cond
      (and (= \L seat) (= 0 occupied))  \#
      (and (= \# seat) (>= occupied occupancy-threshold)) \L
      :else seat)))

(with-test
  (def part-1-new-seat-state (partial new-seat-state (partial raytrace-occupied-seats 1) 4))
  (is (= \# (part-1-new-seat-state {:width 2 :height 2 :layout ["#." "##"]} 0 0 \#)))
  (is (= \L (part-1-new-seat-state {:width 10 :height 2 :layout ["#.##.##.##" "#######.##"]} 0 3 \#))))
(def part-2-new-seat-state (partial new-seat-state (partial raytrace-occupied-seats nil) 5))


(with-test
  (defn step
    "Applies the provided new-state function to every seat in the current state."
    [{:keys [layout] :as state} new-state-func]
    (->> (map-indexed (fn [row row-content]
                        (string/join (map-indexed (partial new-state-func state row) row-content)))
                      layout)
         (vec)
         (assoc state :layout)))

  (is (= {:width 10 :height 10 :layout ["#.##.##.##"
                                        "#######.##"
                                        "#.#.#..#.."
                                        "####.##.##"
                                        "#.##.##.##"
                                        "#.#####.##"
                                        "..#.#....."
                                        "##########"
                                        "#.######.#"
                                        "#.#####.##"]}
         (step @parsed-example-input part-1-new-seat-state)))

  (is (= {:width 10 :height 10 :layout ["#.LL.L#.##"
                                        "#LLLLLL.L#"
                                        "L.L.L..L.."
                                        "#LLL.LL.L#"
                                        "#.LL.LL.LL"
                                        "#.LLLL#.##"
                                        "..L.L....."
                                        "#LLLLLLLL#"
                                        "#.LLLLLL.L"
                                        "#.#LLLL.##"]}
         (-> @parsed-example-input
             (step part-1-new-seat-state)
             (step part-1-new-seat-state))))

  (is (= {:width 10 :height 10 :layout ["#.##.##.##"
                                        "#######.##"
                                        "#.#.#..#.."
                                        "####.##.##"
                                        "#.##.##.##"
                                        "#.#####.##"
                                        "..#.#....."
                                        "##########"
                                        "#.######.#"
                                        "#.#####.##"]}
         (step @parsed-example-input part-2-new-seat-state)))

  (is (= {:width 10 :height 10 :layout ["#.LL.LL.L#"
                                        "#LLLLLL.LL"
                                        "L.L.L..L.."
                                        "LLLL.LL.LL"
                                        "L.LL.LL.LL"
                                        "L.LLLLL.LL"
                                        "..L.L....."
                                        "LLLLLLLLL#"
                                        "#.LLLLLL.L"
                                        "#.LLLLL.L#"]}
         (-> @parsed-example-input
             (step part-2-new-seat-state)
             (step part-2-new-seat-state)))))


(defn step-until-stable
  "Returns a state which, when passed through the new-state function, results in no change"
  [state new-state-func]
  (let [new-state (step state new-state-func)]
    (if (= state new-state)
      new-state
      (recur new-state new-state-func))))


(with-test
  (defn count-taken-seats [state]
    (->> (:layout state)
         (map vec)
         (flatten)
         (filter #{\#})
         (count)))
  (is (= 37 (count-taken-seats
             (step-until-stable @parsed-example-input part-1-new-seat-state))))
  (is (= 26 (count-taken-seats
             (step-until-stable @parsed-example-input part-2-new-seat-state)))))


(comment

  ; Part 1
  (= 2251
     (->> (step-until-stable @parsed-problem-input part-1-new-seat-state)
          (count-taken-seats)))

  ; Part 2
  (= 2019
     (->> (step-until-stable @parsed-problem-input part-2-new-seat-state)
          (count-taken-seats))))
