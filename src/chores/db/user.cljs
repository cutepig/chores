(ns chores.db.user
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]))

(rf/reg-event-db ::auth
  [rf/debug]
  (fn [db [_ auth]]
    (assoc db ::auth auth)))

(rf/reg-event-db ::user
  [rf/debug]
  (fn [db [_ user]]
    (assoc db ::user user)))

(rf/reg-sub ::auth
  (fn [_ _]
    (rf/subscribe [::firebase/auth #(get % ::auth) [::auth]]))
  (fn [auth _]
    (println ::auth auth)
    auth))

(rf/reg-sub ::user
  (fn [_ _]
    (rf/subscribe [::auth]))
  (fn [auth]
    (if (some? auth)
      @(rf/subscribe [::firebase/db (str "/users/" (.-uid auth)) #(get % ::user) [::user]])
      nil)))


