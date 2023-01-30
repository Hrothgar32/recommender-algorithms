(ns almos.recommender-algorithms.person-chooser
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn person-chooser [user-list algorithm]
  (let [chosen-person (r/atom (first @user-list))
        recommendations (rf/subscribe [:backend/recommendations algorithm])
        recommendations-loaded? (rf/subscribe [:backend/recommendations-loaded? algorithm])]
    (fn []
      [:div.container
       [:label {:for "people"}
        "Choose a person"]
       [:select {:on-change (fn [x]
                              (reset! chosen-person (let [pid (.. x -target -value)]
                                                      (first (filter (fn [x] (= (:user x) (js/parseInt pid))) @user-list))))
                              (rf/dispatch [:backend/reset-recommendations algorithm]))
                 :value (:user @chosen-person)}
        (map (fn [x] [:option {:value (:user x)} (:user x)]) @user-list)]
       [:div
        "Movies rated by him/her are: "
        [:ul
         (map (fn [x] [:li (str "Title: " (:title x) ", rating: " (:rating x))]) (:ratings @chosen-person))]]
       [:div
        [:button.primary
         {:on-click #(rf/dispatch [:backend/get-recommendations algorithm (:user @chosen-person)])}
         "Get person recommendations!"]
        (when @recommendations-loaded?
          [:div
           "The recommendations are:"
           [:ul
            (map (fn [x] [:li x]) @recommendations)]])]])))
