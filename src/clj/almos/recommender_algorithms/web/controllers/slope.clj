(ns almos.recommender-algorithms.web.controllers.slope
  (:require
   [almos.recommender-algorithms.algorithms.slopeone :refer [get-user-movies
                                                             slope-one-recommend slope-one-recommender]]
   [almos.recommender-algorithms.data-io.data :refer [load-items load-ratings]]
   [ring.util.http-response :as response]))

(defn get-users
  [req]
  (response/ok
   (let [ratings (group-by :user (load-ratings "datasets/ml-100k/ua.base"))
         items (load-items "datasets/ml-100k/u.item")]
     (map (partial get-user-movies items) ratings)))
  )

(defn get-recs-for-user
  [req]
  (response/ok
   (let [ratings (->> (load-ratings "datasets/ml-100k/ua.base")
                      (group-by :user)
                      (vals))
         items   (load-items "datasets/ml-100k/u.item")
         recommender (->> (rest ratings)
                          (slope-one-recommender))]
     (->> (slope-one-recommend recommender (nth ratings (Integer/parseInt (get-in req [:path-params :id]))) 10)
          (map #(get items (:item %)))))))
