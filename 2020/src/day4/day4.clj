(ns day4.day4
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(def problem-input (delay (slurp "src/day4/input.txt")))
(defn parse-input [input] (->> (string/split input #"\r?\n\r?\n")
                               (map #(string/split % #"[\n ]"))
                               (map (fn [passport] (->> (map #(string/split % #"[:]") passport)
                                                        (map (fn [[k v]] [(keyword k) v])))))
                               (map #(into {} %))))
(def parsed-problem-input (parse-input @problem-input))

(def example-input (delay (slurp "src/day4/example.txt")))
(def parsed-example-input (parse-input @example-input))



(defn has-all-required-fields? [passport]
  (let [required-fields #{:byr :iyr :eyr :hgt :hcl :ecl :pid}
        valid-fields (set/intersection (set (keys passport)) required-fields)]
    (= valid-fields required-fields)))

(defn has-valid-birth-year? [passport] (<= 1920 (Long/valueOf (:byr passport)) 2002))
(defn has-valid-issue-year? [passport] (<= 2010 (Long/valueOf (:iyr passport)) 2020))
(defn has-valid-expiration-year? [passport] (<= 2020 (Long/valueOf (:eyr passport)) 2030))

(defn has-valid-height? [passport]
  (let [height (:hgt passport)
        height-val (.substring height 0 (min 0  (- (count height) 2)))]
    (and (not (string/blank? height-val))
         (or (and (string/includes? height "in")
                  (<= 59 (Long/valueOf height-val) 76))
             (and (string/includes? height "cm")
                  (<= 150 (Long/valueOf height-val) 193))))))

(defn has-valid-hair-colour? [passport]
  (some? (re-matches #"[#][0-9a-f]{6}" (:hcl passport))))

(defn has-valid-eye-colour? [passport]
  (some? (#{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"} (:ecl passport))))

(defn has-valid-passport-number? [passport]
  (and (= 9 (count (:pid passport)))
       (every? #{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9} (:pid passport))))

(def is-valid-passport? (comp (filter has-all-required-fields?)
                              (filter has-valid-birth-year?)
                              (filter has-valid-issue-year?)
                              (filter has-valid-expiration-year?)
                              (filter has-valid-height?)
                              (filter has-valid-hair-colour?)
                              (filter has-valid-eye-colour?)
                              (filter has-valid-passport-number?)))

(comment
  (count (filter has-all-required-fields? parsed-problem-input))
  (count (sequence is-valid-passport? parsed-problem-input))

  (has-valid-birth-year? {:byr 2003})
  (has-valid-height? {:hgt "190ab"})
  (has-valid-hair-colour? {:hcl "#123abc"})
  (has-valid-eye-colour? {:ecl "red"})
  (has-valid-passport-number? {:pid "000123456"})

  (.substring "123in" 0 (- (count "123in") 2)))
