(ns almos.recommender-algorithms.routes
  (:require
   [almos.recommender-algorithms.views.home :as home]
   [almos.recommender-algorithms.views.lsh :as lsh]
   [almos.recommender-algorithms.views.slopeone :as slopeone]
   [almos.recommender-algorithms.views.svd :as svd]))

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
     :view #'lsh/lsh}]
   ["/svd"
    {:name ::svd
     :controllers svd/svd-controllers
     :view #'svd/svd}]]
  )
