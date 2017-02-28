(ns chores.db.user
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]
            [chores.db.task :as task]))

(rf/reg-event-db ::auth
  [rf/debug]
  (fn [db [_ auth]]
    (assoc db ::auth auth)))

(rf/reg-event-db ::auth-error
  [rf/debug]
  (fn [db [_ auth]] db))

(rf/reg-event-db ::user
  [rf/debug]
  (fn [db [_ user]]
    (assoc db ::user user)))

(rf/reg-event-fx ::login
  [rf/debug]
  (fn [fx [_ type params]]
    (assoc fx :dispatch [::firebase/auth type
                         (merge params {:done-ev [::auth] :error-ev [::auth-error]})])))

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
