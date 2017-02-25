(ns chores.ui.screens.groups
  (:require [re-frame.core :as rf]
            [chores.router :as router]
            [chores.db.user :as user]))

(rf/reg-sub ::groups
  (fn [_ _]
    (rf/subscribe [::user/user]))
  (fn [user _]
    (:groups user)))

;; TODO: Go directly to group if there is only 1 group
(defn groups-screen [_]
  (let [groups @(rf/subscribe [::groups])]
    (println ::groups-screen groups)
    [:div.groups
     [:h1 "Groups!"]
     [:ul
      (for [[group-id group-name] groups]
        ^{:key group-id}
        [:li
          [:a {:href (str "/groups/" group-id)} group-name]])]]))

(router/reg-route ::groups groups-screen)

