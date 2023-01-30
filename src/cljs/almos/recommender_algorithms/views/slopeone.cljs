(ns almos.recommender-algorithms.views.slopeone
  (:require
   [almos.recommender-algorithms.person-chooser :refer [person-chooser]]
   [almos.recommender-algorithms.utils :as utils]
   [re-frame.core :as rf]))

(def slope-one-controllers
  [{:start (fn [_] (rf/dispatch [:backend/get-user-ratings "slope"]))}])

(def slope-one-description
  "Slope one is a family of algorithms used for collaborative filtering, which is the simplest form of non-trivial item based filtering based on ratings, which uses the average difference between pairs of items as a baseline.")

(defn slope-one [_]
  (let [user-list (rf/subscribe [:backend/user-ratings])]
    (fn []
      [:div
       [:h2 "Slope one recommender"]
       [:br]
       [utils/introducing-text slope-one-description]
       (if @(rf/subscribe [:backend/user-ratings-loading])
         [:div "User ratings are loading..."]
         [person-chooser user-list "slope"])])))
