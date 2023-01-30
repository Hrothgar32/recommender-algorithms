(ns almos.recommender-algorithms.views.lsh
  (:require
   [almos.recommender-algorithms.person-chooser :refer [person-chooser]]
   [almos.recommender-algorithms.utils :as utils]
   [re-frame.core :as rf]))

(def lsh-description
  "Locality sensitive hashing is an algorithmic technique that hashes similar input items into the same \"buckets\" with high probability. Similar items end up in the same buckets, so this technique can be used for data clustering and nearest neighbour search. It differs from other hashing techniques that hash collisions are not minimezed, but rather maximized.
The LSH algorithm used in this demo uses the random hyperplane projection based technique, developed by Moses Charikar.")

(def lsh-controllers [{:start (fn [_] (rf/dispatch [:backend/get-user-ratings "lsh"]))}])

(defn lsh
  [_]
  (let [user-list (rf/subscribe [:backend/user-ratings])]
    (fn [_]
      [:div
       [:h2 "LSH recommender"]
       [:br]
       [utils/introducing-text lsh-description]
       (if @(rf/subscribe [:backend/user-ratings-loading])
         [:div "User ratings are loading..."]
         [person-chooser user-list "lsh"])])))
