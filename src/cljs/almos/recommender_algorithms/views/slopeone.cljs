(ns almos.recommender-algorithms.views.slopeone
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [almos.recommender-algorithms.events :as events]
   [almos.recommender-algorithms.subs :as subs]))

(def slope-one-controllers
  [{:start (fn [_] (rf/dispatch [:slope/get-user-ratings]))}])

(defn person-chooser [user-list]
  (let [chosen-person (r/atom (first @user-list))
        recommendations (rf/subscribe [:slope/recommendations])
        recommendations-loaded? (rf/subscribe [:slope/recommendations-loaded?])]
    (fn []
      [:div.container
       [:label {:for "people"}
        "Choose a person"]
       [:select {:on-change (fn [x]
                              (reset! chosen-person (let [pid (.. x -target -value)]
                                                      (first (filter (fn [x] (= (:user x) (js/parseInt pid))) @user-list))))
                              (rf/dispatch [:slope/reset-recommendations]))
                 :value (:user @chosen-person)}
        (map (fn [x] [:option {:value (:user x)} (:user x)]) @user-list)]
       [:div
        "Movies rated by him/her are: "
        [:ul
         (map (fn [x] [:li (str "Title: " (:title x) ", rating: " (:rating x))]) (:ratings @chosen-person))]]
       [:div
        [:button.primary
         {:on-click #(rf/dispatch [:slope/get-recommendations (:user @chosen-person)])}
         "Get person recommendations!"]
        (when @recommendations-loaded?
          [:div
           "The recommendations are:"
           [:ul
            (map (fn [x] [:li x]) @recommendations)]])]])))

(def slope-one-description
  "Slope one is a family of algorithms used for collaborative filtering, which is the simplest form of non-trivial item based filtering based on ratings, which uses the average difference between pairs of items as a baseline.")

(defn introducing-text [description]
  [:p description])

(defn slope-one [_]
  (let [user-list (rf/subscribe [:slope/user-ratings])]
    (fn []
      [:div
       [:h2 "Slope one recommender"]
       [:br]
       [introducing-text slope-one-description]
       (if @(rf/subscribe [:slope/user-ratings-loading])
         [:div "User ratings are loading..."]
         [person-chooser user-list])])))
