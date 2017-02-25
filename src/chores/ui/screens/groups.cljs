(ns chores.ui.screens.groups
  (:require [re-frame.core :as rf]
            [re-frame-history.core :as history]
            [chores.router :as router]
            [chores.db.user :as user]
            [chores.ui.core :as ui]
            [chores.ui.layouts.main :refer [main-layout]]))

(rf/reg-sub ::groups
  (fn [_ _]
    (rf/subscribe [::user/user]))
  (fn [user _]
    (:groups user)))

;; TODO: Go directly to group if there is only 1 group
(defn groups-screen [_]
  (ui/with-auth
    (let [groups @(rf/subscribe [::groups])]
      (if (= 1 (count groups))
        ;; TODO: `[::route/route <route key>]` using `silk/depart`
        (rf/dispatch [::history/push (str "/groups/" (name (ffirst groups)))]))
      [main-layout
       [:div.groups
        [:h1 "Groups!"]
        [:ul
         (for [[group-id group-name] groups]
           ^{:key group-id}
           [:li
             [:a {:href (str "/groups/" (name group-id))} group-name]])]]])))

(router/reg-route ::groups groups-screen)

