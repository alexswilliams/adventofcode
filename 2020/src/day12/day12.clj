(ns day12.day12
  (:require [clojure.string :as string]
            [clojure.test :refer [with-test is]]))

(def example-input "F10\nN3\nF7\nR90\nF11")

(with-test
  (defn parse-input [input]
    (->> (string/split-lines input)
         (map (fn [line] [({\F :forward
                            \L :left
                            \R :right
                            \N :north
                            \S :south
                            \E :east
                            \W :west} (first line))
                          (Long/valueOf (string/join (drop 1 line)))]))))
  (is (= '([:forward 10] [:north 3] [:forward 7] [:right 90] [:forward 11])
         (parse-input example-input))))

(defonce problem-input (delay (slurp "src/day12/input.txt")))
(def parsed-problem-input (delay (parse-input @problem-input)))

(def parsed-example-input (parse-input example-input))


(defn displace
  "Starting from [x y], moves `magnitude` units in the cardinal `direction`"
  [[x y] direction magnitude]
  (case direction
    :north [x (+ y magnitude)]
    :south [x (- y magnitude)]
    :east [(+ x magnitude) y]
    :west [(- x magnitude) y]))




(ns day12.part1
  (:require [day12.day12 :as day12]))

(def initial-state {:direction :east
                    :location [0 0]})

(def rotate-left {[:east 90]   :north
                  [:north 90]  :west
                  [:west 90]   :south
                  [:south 90]  :east
                  [:east 180]  :west
                  [:west 180]  :east
                  [:north 180] :south
                  [:south 180] :north
                  [:east 270]  :south
                  [:south 270] :west
                  [:west 270]  :north
                  [:north 270] :east})

(defn step
  "Processes a single command and returns a new state object indicating the changes that command made.
   Note that rotation is clamped to 90°, 180° or 270°, and all rotations are converted to anti-clockwise."
  [{:keys [direction location]
             :as   state} command]
  (case (first command)
    :left (assoc state :direction (rotate-left [direction (second command)]))
    :right (assoc state :direction (rotate-left [direction (- 360 (last command))]))
    :forward (assoc state :location (day12/displace location direction (second command)))
    (:north :south :east :west) (assoc state :location (day12/displace location (first command) (second command)))))

(defn process-command-list [commands]
  (reduce step initial-state commands))




(ns day12.part2
  (:require [day12.day12 :as day12]))

(def initial-state {:location [0 0]
                    :waypoint [10 1] #_(10 east, 1 north, relative to location)})


(defn move-forward
  "Adds `multiple` multiples of [way-x,way-y] to [ship-x,ship-y], simulating moving forward
   in the direction of the waypoint"
  [[ship-x ship-y] [way-x way-y] multiple]
  [(+ ship-x (* multiple way-x))
   (+ ship-y (* multiple way-y))])

(def rotate-anti-cw {90  (fn [[x y]] [(- y) (+ x)])
                     180 (fn [[x y]] [(- x) (- y)])
                     270 (fn [[x y]] [(+ y) (- x)])})

(defn step
  "Processes a single command and returns a new state object indicating the changes that command made.
   Note that rotation is clamped to 90°, 180° or 270°, and all rotations are converted to anti-clockwise."
  [{:keys [location waypoint]
             :as   state} command]
  (case (first command)
    :forward (assoc state :location (move-forward location waypoint (second command)))
    :left (assoc state :waypoint ((rotate-anti-cw (second command)) waypoint))
    :right (assoc state :waypoint ((rotate-anti-cw (- 360 (second command))) waypoint))
    (:north :south :east :west) (assoc state :waypoint (day12/displace waypoint (first command) (second command)))))

(defn process-command-list [commands]
  (reduce step initial-state commands))



(ns day12.day12
  (:require [day12.part1 :as part1]
            [day12.part2 :as part2]))


(defn manhattan-distance [[x y]]
  (let [abs #(max % (- %))]
    (+ (abs x) (abs y))))

(comment
  ; Part 1
  (= 25
     (->> (part1/process-command-list parsed-example-input)
          (:location)
          (manhattan-distance)))

  (= 1482
     (->> (part1/process-command-list @parsed-problem-input)
          (:location)
          (manhattan-distance)))

  ; Part 2
  (= 286
     (->> (part2/process-command-list parsed-example-input)
          (:location)
          (manhattan-distance)))

  (= 48739
     (->> (part2/process-command-list @parsed-problem-input)
          (:location)
          (manhattan-distance))))
