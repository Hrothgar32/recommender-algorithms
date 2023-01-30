(ns almos.recommender-algorithms.web.controllers.lsh
  (:require
   [almos.recommender-algorithms.algorithms.lsh :refer [generate-random-vectors load-unique-items
                                                        lsh-recommend]]
   [almos.recommender-algorithms.data-io.data :refer [extract-ratings-from-dataset! load-items
                                                      load-ratings serialize-from-file!]]
   [ring.util.http-response :as response]
   [tablecloth.api :as tc]))

(defn get-users
  [_]
  (response/ok
   (extract-ratings-from-dataset! "datasets/ml-100k/u1.test" "datasets/ml-100k/u.item")))

(defn get-recs-for-user
  [req]
  (response/ok
   (let [ratings (-> (load-ratings "datasets/ml-100k/u1.base")
                      (tc/dataset)
                      (tc/group-by :user {:result-type :as-map}))
         test-ratings (-> (load-ratings "datasets/ml-100k/u1.test")
                      (tc/dataset)
                      (tc/group-by :user {:result-type :as-map}))
         items   (load-items "datasets/ml-100k/u.item")
         ml-100k-all-items (load-unique-items "datasets/ml-100k/u1.base")
         ml-100k-d (first (tc/shape ml-100k-all-items))
         ml-100k-rand-normals (generate-random-vectors 20 ml-100k-d)
         buckets (serialize-from-file! "resources/datasets/ml-100k/frozen-bucket.data" )]
     (lsh-recommend buckets (get test-ratings (Integer/parseInt (get-in req [:path-params :id]))) ml-100k-all-items ml-100k-rand-normals 10 ratings items))))
