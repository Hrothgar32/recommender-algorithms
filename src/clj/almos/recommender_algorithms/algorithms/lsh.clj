(ns almos.recommender-algorithms.algorithms.lsh
  (:require
   [almos.recommender-algorithms.algorithms.stats :as s]
   [almos.recommender-algorithms.data-io.data :refer [fill-missing-with-mean
                                                      load-items load-ratings
                                                      serialize-from-file! serialize-to-file!]]
   [clojure.java.io :as io]
   [tablecloth.api :as tc]
   [tech.v3.dataset.neanderthal :as dt->n]
   [uncomplicate.fluokitten.core :refer [fmap]]
   [uncomplicate.neanderthal.core :as ncore]
   [uncomplicate.neanderthal.linalg :as nlin]
   [uncomplicate.neanderthal.native :as nnat]
   [uncomplicate.neanderthal.random :as nrand]))

(def threshold
  "The sign function which translates a value into a binary 1 or 0."
  (fn [x]
    (if (> x 0.0)
      1.0
      0.0)))

(defn generate-random-vectors [d nbits]
  (nrand/rand-normal! 0 1 (nnat/dge d nbits)))

(defn calculate-lsh-hash
  "Used to calculate the LSH hash of a random vector."
  [user-vector rand-normals]
  (. Integer parseInt (->> (ncore/mv rand-normals user-vector)
                           (fmap threshold)
                           (into [])
                           (map int)
                           (apply str)) 2))

(defn get-sparse-ratings
  "Getting a sparse version of a rating vector"
  [all-items user-ratings]
  (let [user-id (get-in user-ratings [:user 0])
        missing-values
        (-> (tc/anti-join all-items user-ratings :item)
            (tc/add-columns {:rating 0.0
                             :user user-id})
            (tc/reorder-columns :user [:item :rating]))] (-> (tc/union user-ratings missing-values)
                                                             (tc/order-by :item))))

(defn get-sparse-vector [sparse-ratings]
  (-> sparse-ratings
      (tc/select-columns :rating)
      dt->n/dataset->dense
      (ncore/col 0)))

(defn generate-lsh-buckets
  "Generating the buckets for the stuff."
  [all-items grouped-ratings rand-normals]
  (loop [current-rating (first grouped-ratings)
         remaining-ratings (rest grouped-ratings)
         current-user (get-in current-rating [:user 0])
         buckets {}]
    (let [sparse-vector (-> (get-sparse-ratings all-items current-rating)
                            (get-sparse-vector))
          lsh-hash (calculate-lsh-hash sparse-vector rand-normals)
          new-buckets (if (contains? buckets lsh-hash)
                        (update buckets lsh-hash conj current-user)
                        (conj buckets [lsh-hash [current-user]]))]
      (if (empty? remaining-ratings)
        new-buckets
        (recur (first remaining-ratings) (rest remaining-ratings) (get-in (first remaining-ratings) [:user 0]) new-buckets)))))

(defn- save-unique-items
  [dataset-path]
  (-> (load-ratings dataset-path)
      (tc/dataset)
      (tc/unique-by :item)
      (tc/select-columns :item)
      (tc/write! (str "resources/" dataset-path "unique-items.csv.gz"))))

(defn load-unique-items
  [dataset-path]
  (tc/rename-columns
   (tc/dataset (str "resources/" dataset-path "unique-items.csv.gz")) {"item" :item}))

(defn generate-query-hash
  [all-items user-ratings rand-normals]
  (let [sparse-vector (-> (get-sparse-ratings all-items user-ratings)
                          (get-sparse-vector))]
    (calculate-lsh-hash sparse-vector rand-normals)))

(defn extract-item-from-bucket [bucket grouped-ratings items]
  (let [user-id (first (second bucket))
        user-ratings (map #(nth % 2) (-> (get grouped-ratings user-id)
                                         (tc/head 3)
                                         (tc/rows)))]
    (map (fn [x] (get items x)) user-ratings)))

(defn lsh-recommend [lsh-buckets user-query all-items rand-normals top-n grouped-ratings items]
  (let [query-hash (generate-query-hash all-items user-query rand-normals)
        sorted-buckets (sort-by (s/hamming-distance-sort query-hash) < lsh-buckets)
        first-n-buck (take top-n sorted-buckets)]
    (distinct (apply concat (for [buck first-n-buck
                                  :let [y (extract-item-from-bucket buck grouped-ratings items)]]
                              y)))))

(comment
  ;; Example usage of locality sensitive hashing

  ;;; Ml-100k
  ;; Saving all unique items for ml-100k
  (save-unique-items "datasets/ml-100k/u1.base")

  ;; Loading back these items
  (def ml-100k-all-items (load-unique-items "datasets/ml-100k/u1.base"))

  (def ml-100k-d (first (tc/shape ml-100k-all-items)))

  ;; Grouped ratings
  (def grouped-ratings
    (-> (load-ratings "datasets/ml-100k/u1.base")
        (tc/dataset)
        (tc/group-by :user {:result-type :as-map})))


  (def completed-dataset-for-svd
    (apply tc/concat (map fill-missing-with-mean (-> grouped-ratings-seq
                                                     (tc/complete :user :item)
                                                     (tc/group-by :user {:result-type :as-seq})))))

  (:sigma (-> (nnat/dge 943 1650 (:rating completed-dataset-for-svd))
              (nlin/svd true true)))

  (defn my-fun [ds]
    (reduce (fn [coll row]
              (let [value (nth row 2)
                    i (dec (first row))
                    j (dec (second row))]
                (assoc-in coll [i j] value))) [] (tc/rows ds)))

  (first (tc/shape (tc/unique-by completed-dataset-for-svd :user)))
  (first (tc/shape (tc/unique-by completed-dataset-for-svd :item)))

  (-> grouped-ratings-seq
      (tc/complete :user :item))

  (map #(nth % 2) (tc/rows (tc/head (get grouped-ratings 893) 3)))

  (tc/select-rows (first grouped-ratings))

  (io/resource  "datasets/ml-100k/u1.base")

  (def test-ratings
    (-> (load-ratings "datasets/ml-100k/u1.test")
        (tc/dataset)
        (tc/group-by :user {:result-type :as-map})))

  (def items (load-items "datasets/ml-100k/u.item"))

  ;; Getting random normal vectors
  (def ml-100k-rand-normals (generate-random-vectors 20 ml-100k-d))
  (def buckets (time (generate-lsh-buckets ml-100k-all-items grouped-ratings ml-100k-rand-normals)))

  (def query-hash (generate-query-hash ml-100k-all-items (nth test-ratings 3)  ml-100k-rand-normals))

  (serialize-to-file! "resources/datasets/ml-100k/frozen-bucket.data" buckets)

  (serialize-to-file! "resources/datasets/ml-100k/frozen-normals.data" ml-100k-rand-normals)

  (def buckets (serialize-from-file! "resources/datasets/ml-100k/frozen-bucket.data"))

  (lsh-recommend buckets (get test-ratings 357) ml-100k-all-items ml-100k-rand-normals 10 grouped-ratings items)
  ;;;
  ;;; Ml-1M
  )
