(ns almos.recommender-algorithms.algorithms.stats
  (:require [uncomplicate.neanderthal.core :as ncore]
            [uncomplicate.neanderthal.native :as nnat]
            [uncomplicate.neanderthal.vect-math :as nvmath]
            [uncomplicate.neanderthal.math :as nmath]))

(defn mean [input]
  (let [native-vector (nnat/dv input)
        size (ncore/dim native-vector)]
    (/ (ncore/sum native-vector) size)))

(defn magnitude [x]
  (nmath/sqrt (ncore/sum (nvmath/sqr x))))

(defn cosine-similarity [vector-a vector-b]
  (/ (ncore/dot vector-a vector-b) (* (magnitude vector-a) (magnitude vector-b))))

(defn hamming-distance [first-hash second-hash]
    (. Integer bitCount (bit-xor first-hash second-hash)))

(defn hamming-distance-sort
  "Key function by which we can sort items."
  [query-hash]
  (fn [a]
    (hamming-distance (key a) query-hash)))
