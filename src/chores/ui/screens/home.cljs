(ns chores.ui.screens.home
  (:require [re-frame.core :as rf]
            [chores.ui.panels.login :as login]
            [chores.fx.router :as router]
            [chores.db.user :as user]
            [chores.ui.layouts.main :refer [main-layout]]))

(defn home-screen []
  (let [user @(rf/subscribe [::user/user])]
    ;; TODO: `[::route/route <route key>]` using `silk/depart`
    (if (some? user) (rf/dispatch [::router/push "/groups"]))
    [main-layout
     [:div.home
      [:div.home-login
       [login/login-panel]
      [:div.home-signup
       [login/signup-panel]]]]]))

(router/reg-route ::index home-screen)
