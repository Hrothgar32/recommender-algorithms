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
 :backend/get-user-ratings
 (fn [{:keys [db]} [_ algorithm]]
   {:db (assoc db :backend/user-ratings-loading true)
    :http-xhrio {:method :get
                 :uri (endpoint algorithm)
                 :response-format (json-response-format {:keywords? true})
                 :on-success [:backend/get-user-ratings-success]
                 :on-failure [:backend/api-request-error]}}))

(reg-event-fx
 :backend/get-recommendations
 (fn [{:keys [db]} [_ algorithm person-id]]
   {:http-xhrio {:method :get
                 :uri (endpoint algorithm person-id)
                 :response-format (json-response-format {:keywords? true})
                 :on-success [:backend/get-recommendations-success]
                 :on-failure [:backend/api-request-error]}}))

(reg-event-db
 :backend/get-recommendations-success
 (fn [db [_ recommendations]]
   (assoc db :backend/recommendations recommendations
             :backend/recommendations-loaded? true)))

(reg-event-db
 :backend/reset-recommendations
 (fn [db _]
   (assoc db :backend/recommendations []
             :backend/recommendations-loaded? false)))


(reg-event-db
 :backend/get-user-ratings-success
 (fn [db [_ user-ratings]]
   (assoc db :backend/user-ratings user-ratings
             :backend/user-ratings-loading false)))

(reg-event-db
 :backend/api-request-error
 (fn [db _]
   (assoc db :error true)))
