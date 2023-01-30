(ns almos.recommender-algorithms.web.controllers.slope
  (:require
   [almos.recommender-algorithms.algorithms.slopeone :refer [
                                                             slope-one-recommend slope-one-recommender]]
   [almos.recommender-algorithms.data-io.data :refer [extract-ratings-from-dataset! load-ratings load-items]]
   [ring.util.http-response :as response]))

(defn get-users
  [req]
  (response/ok
   (extract-ratings-from-dataset! "datasets/ml-100k/ua.base" "datasets/ml-100k/u.item")))

(defn get-recs-for-user
  [req]
  (response/ok
   (let [ratings (->> (load-ratings "datasets/ml-100k/ua.base")
                      (group-by :user)
                      (vals))
         items   (load-items "datasets/ml-100k/u.item")
         recommender (->> ratings
                          (slope-one-recommender))]
     (->> (slope-one-recommend recommender (nth ratings (Integer/parseInt (get-in req [:path-params :id]))) 10)
          (map #(get items (:item %)))))))
