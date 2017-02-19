(ns chores.ui.user
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]))

(rf/reg-event-db ::set-user
  [rf/debug]
  (fn [db [_ user-id user]]
    (assoc-in db [::users user-id] user)))

(rf/reg-event-db ::set-user-error
  [rf/debug]
  (fn [db _] db))

(rf/reg-event-db ::set-groups
  [rf/debug]
  (fn [db [_ groups]]
    (assoc-in db [::groups] groups)))

(rf/reg-event-db ::set-user-error
  [rf/debug]
  (fn [db _] db))

(defn fb-sub-users []
  [::firebase/firebase ["/users"]])

;; TODO: Convert to proper subs
(defn fb-sub-user [user-id]
  [::firebase/firebase
   (str "/users/" user-id)
   (fn [db]
     (let [r (get-in db [::users user-id])]
       (js/console.log "fb-sub-user" user-id r)
       r))
   [::set-user user-id]
   [::set-user-error user-id]])

(defn fb-sub-groups []
  [::firebase/firebase
   "/groups"
   #(get-in % [::groups])
   [::set-groups]
   [::set-groups-error]])

(defn user-groups [{:keys [:user-id]}]
  (let [user @(rf/subscribe (fb-sub-user user-id))]
    (println :user-groups user)
    [:div.user-groups
     ;; TODO: User name to firebase:users/$user-id
     [:h2 "User groups"]
     [:ul
      (for [[group-id name] (:groups user)]
        ^{:key group-id} [:li name])]]))


