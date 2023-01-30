(ns almos.recommender-algorithms.events
  (:require
   [ajax.json :refer [json-response-format]]
   [clojure.string :as str]
   [day8.re-frame.http-fx]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(def api-url "/api")

(defn endpoint
  "Concat any params to api-url separated by /"
  [& params]
  (str/join "/" (cons api-url params)))

(reg-event-fx
 :slope/get-user-ratings
 (fn [{:keys [db]} _]
   {:db (assoc db :slope/user-ratings-loading true)
    :http-xhrio {:method :get
                 :uri (endpoint "slope")
                 :response-format (json-response-format {:keywords? true})
                 :on-success [:slope/get-user-ratings-success]
                 :on-failure [:slope/api-request-error]}}))

(reg-event-fx
 :slope/get-recommendations
 (fn [{:keys [db]} [_ person-id]]
   {:http-xhrio {:method :get
                 :uri (endpoint "slope" person-id)
                 :response-format (json-response-format {:keywords? true})
                 :on-success [:slope/get-recommendations-success]
                 :on-failure [:slope/api-request-error]}}))

(reg-event-db
 :slope/get-recommendations-success
 (fn [db [_ recommendations]]
   (assoc db :slope/recommendations recommendations
             :slope/recommendations-loaded? true)))

(reg-event-db
 :slope/reset-recommendations
 (fn [db _]
   (assoc db :slope/recommendations []
             :slope/recommendations-loaded? false)))


(reg-event-db
 :slope/get-user-ratings-success
 (fn [db [_ user-ratings]]
   (assoc db :slope/user-ratings user-ratings
             :slope/user-ratings-loading false)))

(reg-event-db
 :slope/api-request-error
 (fn [db _]
   (assoc db :error true)))
