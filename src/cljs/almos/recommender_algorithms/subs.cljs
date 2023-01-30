(ns almos.recommender-algorithms.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 :backend/user-ratings
 (fn [db _]
   (:backend/user-ratings db)))

(rf/reg-sub
 :backend/user-ratings-loading
 (fn [db _]
   (:backend/user-ratings-loading db)))

(rf/reg-sub
 :backend/user-ratings-empty?
 :<- [:backend/user-ratings]
 (fn [user-ratings _]
   (empty? user-ratings)))

(rf/reg-sub
 :backend/recommendations
 (fn [db _]
   (:backend/recommendations db)))

(rf/reg-sub
 :backend/recommendations-loaded?
 (fn [db _]
   (:backend/recommendations-loaded? db)))
