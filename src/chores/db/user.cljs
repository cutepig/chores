(ns chores.db.user
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [chores.fx.firebase :as firebase]
            [chores.db.task :as task]
            [chores.ui.notifications :as notifications]))

(rf/reg-event-fx ::auth
  [rf/debug]
  (fn [fx [_ auth]]
    (println ::auth auth)
    (-> fx
      (assoc-in [:db ::auth] auth)
      (assoc :dispatch (if (and (nil? (get-in fx [::db auth])) (some? auth))
                         ;; TODO: "Logged in as $username"
                         [::notifications/info "Logged in"]
                         (if (and (some? (get-in fx [::db auth])) (nil? auth))
                           [::notifications/info "Logged out"]))))))

(rf/reg-event-fx ::auth-error
  [rf/debug]
  (fn [fx [_ e]]
    (assoc fx :dispatch [::notifications/error (.-message e)])))

(rf/reg-event-db ::user
  [rf/debug]
  (fn [db [_ user]]
    (assoc db ::user user)))

(rf/reg-event-fx ::login
  [rf/debug]
  (fn [fx [_ type params]]
    (assoc fx :dispatch [::firebase/auth type
                         (merge params {:done-ev [::auth] :error-ev [::auth-error]})])))

(rf/reg-event-fx ::logout
  [rf/debug]
  (fn [fx _]
    (assoc fx :dispatch [::firebase/logout [::auth] [::auth-error]])))

(rf/reg-sub ::auth
  (fn [_ _]
    (rf/subscribe [::firebase/auth]))
  (fn [auth _]
    auth))

(rf/reg-sub ::user
  (fn [_ _]
    (rf/subscribe [::auth]))
  (fn [auth]
    (if (some? auth)
      @(rf/subscribe [::firebase/db [::users (.-uid auth)]])
      nil)))

(rf/reg-sub ::deeds
  (fn [[_ group-id] _]
    [(rf/subscribe [::task/deeds group-id])
     (rf/subscribe [::task/tasks group-id])])
  (fn [[deeds tasks] [_ _ user-id]]
    (->> (vals deeds)
         (filter #(= (:memberId %) user-id))
         (map #(assoc % :task (get tasks (keyword (:taskId %))))))))
