(ns chores.ui.layouts.main
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [chores.fx.firebase :as firebase]
            [chores.fx.history :as history]
            [chores.db.user :as user]))

(rf/reg-event-fx ::logout
  [rf/debug]
  (fn logout [fx _]
    (assoc fx :dispatch-n [[::firebase/logout]
                           [::history/push "/"]])))

(defn main-layout []
  (let [auth @(rf/subscribe [::user/auth])
        children (r/children (r/current-component))]
    (js/console.info ::main-layout auth)
    [:div.main
     [:header
      [:h1 "Chores"]
      (when (some? auth)
        [:h2 (or (.-displayName auth) (.-email auth))])
      (when (some? auth)
        [:button {:on-click #(rf/dispatch [::logout])} "Logout"])]

     (into [:main] children)

     [:footer
      [:p "© 2017 " [:i "Powered by moonlight"]]]]))

