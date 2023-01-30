(ns almos.recommender-algorithms.core
    (:require
     [almos.recommender-algorithms.routes :refer [app-routes]]
     [almos.recommender-algorithms.subs :as subs]
     [almos.recommender-algorithms.events :as events]
     [re-frame.core :as rf]
     [reagent.dom :as d]
     [reitit.coercion.spec :as reitit-spec]
     [reitit.frontend :as rtf]
     [reitit.frontend.easy :as rtfe]
     [reitit.frontend.controllers :as rtfc]))


(defn navbar []
  (fn []
    [:nav.navbar.navbar-expand-lg.bg-primary
     [:div.container-fluid
      [:a.navbar-brand
       {:href "/"}
       "Recommender demo"]
      [:button.navbar-toggler
       {:type "button"
        :data-bs-toggle "collapse"
        :data-bs-target "#navbarNavAltMarkup"
        :aria-controls "navbarNavAltMarkup"
        :aria-expanded "false"}
       [:span.navbar-toggler-icon]]
      [:div#navbarNavAltMarkup.collapse.navbar-collapse
       [:div.navbar-nav
        [:a.nav-link
         {:href "/slope"}
         "Slope one"]
        [:a.nav-link
         {:href "/lsh"}
         "LSH"]
        [:a.nav-link
         {:href "/svd"}
         "SVD"]]]]]))

(rf/reg-event-fx
 :app/initialize
 (fn [_ _]
   {:db {:session/loading? true
         :slope/user-ratings []
         :slope/recommendations-loaded? false
         :slope/recommendations []}}))

(rf/reg-sub
 :router/current-route
 (fn [db]
   (:router/current-route db)))

(rf/reg-event-db
 :router/navigated
 (fn [db [_ new-match]]
   (assoc db :router/current-route new-match)))

(def router
  (rtf/router
   (app-routes)
   {:data {:coercion reitit-spec/coercion}}))

(defn init-routes! []
  (rtfe/start!
   router
   (fn [new-match]
     (when new-match
       (let [{controllers :controllers}
             @(rf/subscribe [:router/current-route])
             new-match-with-controllers
             (assoc new-match
                    :controllers (rtfc/apply-controllers controllers new-match))]
         (rf/dispatch [:router/navigated new-match-with-controllers]))))
  {:use-fragment false}))

(defn page [{{:keys [view name]} :data
             path               :path
             :as                match}]
  [:div.container
   (if view
     [view match]
     [:div "No view specified for route: " name "(" path ")"])])

(defn app []
  (let [current-route @(rf/subscribe [:router/current-route])]
    [:div.app
     [navbar]
     [page current-route]]))

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (init-routes!)
  (d/render [#'app] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:app/initialize])
  (mount-root))

(comment
  (+ 3 4)
  )
