(ns chores.db.task
  (:require [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]
            [chores.db.group :as group]))

(rf/reg-sub ::tasks
  (fn [[_ group-id] _]
    (println ::tasks-group-id group-id)
    (rf/subscribe [::group/group group-id]))
  (fn [group _]
    (println ::tasks group)
    (:tasks group)))




