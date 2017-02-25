(ns chores.db.group
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]))

(rf/reg-event-db ::group
  [rf/debug]
  (fn [db [_ group-id group]]
    (assoc-in db [::groups group-id] group)))

(rf/reg-sub ::group
  (fn [[_ group-id] _]
    (if (some? group-id)
      (rf/subscribe [::firebase/db
                     (str "/groups/" group-id)
                     #(get-in % [::groups group-id])
                     [::group group-id]])
      (r/atom nil)))
  (fn [group _] group))

