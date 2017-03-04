(ns chores.db.group
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [chores.fx.firebase :as firebase]))

(rf/reg-sub ::group
  (fn [[_ group-id] _]
    (if (some? group-id)
      (rf/subscribe [::firebase/db [::groups group-id]])
      (r/atom nil)))
  (fn [group _] group))

(rf/reg-sub ::users
  (fn [[_ group-id] _]
    (rf/subscribe [::group group-id]))
  (fn [group _]
    (:members group)))
