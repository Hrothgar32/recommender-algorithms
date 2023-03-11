(ns almos.recommender-algorithms.algorithms.svd
  (:require
   [almos.recommender-algorithms.data-io.data :refer [fill-missing-with-mean
                                                      load-ratings]]
   [tablecloth.api :as tc]
   [uncomplicate.neanderthal.linalg :as nlin]))

(defn get-svd-matrix [utility-matrix]
  (nlin/svd utility-matrix true true))


(defn generate-masked-grouped-ratings [dataset-path]
  (-> (load-ratings dataset-path)
      (tc/dataset)
      (tc/complete :user :item)
      (tc/group-by :user {:result-type :as-seq})))

(defn fill-subdatasets [subdatasets]
  (apply tc/concat (map fill-missing-with-mean subdatasets)))
