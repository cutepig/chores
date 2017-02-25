(ns chores.ui.screens.home
  (:require [re-frame.core :as rf]
            [re-frame-history.core :as history]
            [chores.ui.panels.login :as login]
            [chores.router :as router]
            [chores.db.user :as user]
            [chores.ui.layouts.main :refer [main-layout]]))

(defn home-screen []
  (let [user @(rf/subscribe [::user/user])]
    ;; TODO: `[::route/route <route key>]` using `silk/depart`
    (if (some? user) (rf/dispatch [::history/push "/groups"]))
    [main-layout
     [:div.home
      [:div.home-login
       [login/login-panel]
       [:button {:on-click #(rf/dispatch [::user/login :google])}
                "Login with Google"]
      [:div.home-signup
       [login/signup-panel]]]]]))

(router/reg-route ::index home-screen)
