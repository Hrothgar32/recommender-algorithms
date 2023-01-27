(ns almos.recommender-algorithms.data-io.data
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [tablecloth.api :as tc]
            [taoensso.nippy :as nippy])
  (:import [java.io DataInputStream DataOutputStream]))

(defn to-long [s]
  (Long/parseLong s))

(defn line->rating [line]
  (->> (s/split line #"\t")
       (map to-long)
       (zipmap [:user :item :rating])))

(zipmap [:user :item :rating] '(1 2 3 5))

(defn load-ratings [file]
  (with-open [rdr (io/reader (io/resource file))]
    (->> (line-seq rdr)
         (map line->rating)
         (into []))))

(defn load-sparse-ratings [ratings]
  (let [DS (tc/dataset ratings)
        grouped-ratings (-> DS
                            (tc/group-by :user {:result-type :as-map})
                            (vals))]
    grouped-ratings))


(defn line->item-tuple [line]
  (let [[id name] (s/split line #"\|")]
    (vector (to-long id) name)))

(defn load-items [path]
  (with-open [rdr (io/reader (io/resource path))]
    (->> (line-seq rdr)
         (map line->item-tuple)
         (into {}))))

(defn item->name [file]
  (let [items (load-items file)]
    (fn [{:keys [id]}]
      (get items id))))

(defn serialize-to-file! [path data]
  (with-open [w (io/output-stream path)]
    (nippy/freeze-to-out! (DataOutputStream. w) data)))

(defn serialize-from-file! [path]
 (with-open [r (io/input-stream path)]
    (nippy/thaw-from-in! (DataInputStream. r))) )
