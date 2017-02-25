(ns chores.ui.screens.home
  (:require [re-frame.core :as rf]
            [chores.ui.panels.login :as login]
            [chores.router :as router]))

;; TODO: Go directly to groups if user is already logged in
(defn home-screen []
  [:div.home
    [:h1 "Chores"]
    [:div.home-login
     [login/login-panel]]])

(router/reg-route ::index home-screen)
