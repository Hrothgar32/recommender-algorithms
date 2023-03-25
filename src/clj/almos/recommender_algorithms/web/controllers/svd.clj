(ns almos.recommender-algorithms.web.controllers.svd
  (:require
   [almos.recommender-algorithms.algorithms.svd :refer [get-svd-matrix get-user-index
                                                        generate-masked-grouped-ratings
                                                        fill-subdatasets]]
   [ring.util.http-response :as response]))

(defn get-recs-for-user [req]
  (response/ok
   (let [ratings (-> (generate-masked-grouped-ratings "datasets/ml-100k/ua.base")
                     (fill-subdatasets))
         user-index (get-user-index ratings)
         svd-stuff (get-svd-matrix ratings)])))
