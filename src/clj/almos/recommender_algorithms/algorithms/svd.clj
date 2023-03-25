(ns almos.recommender-algorithms.algorithms.svd
  (:require
   [almos.recommender-algorithms.data-io.data :refer [fill-missing-with-mean
                                                      load-ratings]]
   [tablecloth.api :as tc]
   [uncomplicate.neanderthal.linalg :as nlin]
   [uncomplicate.neanderthal.native :as nnat]))

(defn get-svd-matrix [dataset]
  (let [user-dim (-> dataset
                     (tc/unique-by :user)
                     (tc/shape)
                     (first))
        item-dim (-> dataset
                     (tc/unique-by :item)
                     (tc/shape)
                     (first))
        matrix (->> dataset
                    (:rating)
                    (nnat/dge user-dim item-dim))]
    (nlin/svd matrix true true)))

(defn get-user-index [dataset]
  (let [unique-users (:user (tc/unique-by dataset :user))
        length (count unique-users)]
    (zipmap unique-users (range length))))


(defn generate-masked-grouped-ratings [dataset-path]
  (-> (load-ratings dataset-path)
      (tc/dataset)
      (tc/complete :user :item)
      (tc/group-by :user {:result-type :as-seq})))

(defn fill-subdatasets [subdatasets]
  (apply tc/concat (map fill-missing-with-mean subdatasets)))
