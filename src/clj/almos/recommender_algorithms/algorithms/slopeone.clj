(ns almos.recommender-algorithms.algorithms.slopeone
  (:require [almos.recommender-algorithms.algorithms.stats :as s]
            [almos.recommender-algorithms.data-io.data :refer [load-ratings load-items]]
            [medley.core :refer [map-vals]]))

(defn conj-item-difference [dict [i j]]
  (let [difference (-  (:rating j) (:rating i))]
    (update-in dict [(:item i) (:item j)] conj difference)))

(defn collect-item-differences [dict items]
  (reduce conj-item-difference dict
          (for [i items
                j items
                :when (not= i j)]
            [i j])))

(defn item-differences [user-ratings]
  (reduce collect-item-differences {} user-ratings))

(defn summarize-item-differences [related-items]
  (let [f (fn [differences]
            {:mean  (s/mean differences)
             :count (count  differences)})]
    (map-vals f related-items)))

(defn slope-one-recommender [ratings]
  (->> (item-differences ratings)
       (map-vals summarize-item-differences)))

(defn candidates [recommender {:keys [rating item]}]
  (->> (get recommender item)
       (map (fn [[id {:keys [mean count]}]]
              {:item id
               :rating (+ rating mean)
               :count count}))))

(defn weighted-rating [[id candidates]]
  (let [ratings-count (reduce + (map :count candidates))
        sum-rating (map #(* (:rating %) (:count %)) candidates)
        weighted-rating (/ (reduce + sum-rating) ratings-count)]
    {:item id
     :rating weighted-rating
     :count  ratings-count}))

(defn slope-one-recommend [recommender rated top-n]
  (let [already-rated  (set (map :item rated))
        already-rated? (fn [{:keys [id]}]
                         (contains? already-rated id))
        recommendations (->> (mapcat #(candidates recommender %)
                                     rated)
                             (group-by :item)
                             (map weighted-rating)
                             (remove already-rated?)
                             (sort-by :rating >))]
    (take top-n recommendations)))

(comment
  ;; Example usage of slope one recommender
  (def recommender
    (let [user-ratings (->> (load-ratings "datasets/ml-100k/ua.base")
                            (group-by :user)
                            (vals))
          recommender  (->> (rest user-ratings)
                            (slope-one-recommender))]
      ;; items     (load-items "datasets/ml-100k/u.item")
      ;; item-name (fn [item]
      ;;             (get items (:item item)))]
      ;; (->> (slope-one-recommend recommender user-1 10)
      ;;      (map item-name))
      recommender))
  (def user-ratings (->> (load-ratings "datasets/ml-100k/ua.base")
                         (group-by :user)
                         (vals)))
  (def items (load-items "datasets/ml-100k/u.item"))
  (defn item-name [item]
    (get items (:item item)))

  (let [user (nth user-ratings 10)]
    (->> (slope-one-recommend recommender user 10)
         (map item-name))))
