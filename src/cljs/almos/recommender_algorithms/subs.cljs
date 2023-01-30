(ns almos.recommender-algorithms.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 :slope/user-ratings
 (fn [db _]
   (:slope/user-ratings db)))

(rf/reg-sub
 :slope/user-ratings-loading
 (fn [db _]
   (:slope/user-ratings-loading db)))

(rf/reg-sub
 :slope/user-ratings-empty?
 :<- [:slope/user-ratings]
 (fn [user-ratings _]
   (empty? user-ratings)))

(rf/reg-sub
 :slope/recommendations
 (fn [db _]
   (:slope/recommendations db)))

(rf/reg-sub
 :slope/recommendations-loaded?
 (fn [db _]
   (:slope/recommendations-loaded? db)))
