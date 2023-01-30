(ns almos.recommender-algorithms.routes
  (:require
   [almos.recommender-algorithms.views.home :as home]
   [almos.recommender-algorithms.views.lsh :as lsh]
   [almos.recommender-algorithms.views.slopeone :as slopeone]))

(defn app-routes []
  [""
   ["/"
    {:name ::home
     :view #'home/home}]
   ["/slope"
    {:name ::slope
     :controllers slopeone/slope-one-controllers
     :view #'slopeone/slope-one}]
   ["/lsh"
    {:name ::lsh
     :controllers lsh/lsh-controllers
     :view #'lsh/lsh}]]
  )
