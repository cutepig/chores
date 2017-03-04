(ns chores.db.task
  (:require [re-frame.core :as rf]
            [chores.fx.firebase :as firebase]
            [chores.db.group :as group]
            [chores.ui.notifications :as notifications]))

;; TODO
(rf/reg-event-fx ::add-deed-done
  [rf/debug]
  (fn [fx _]
    (assoc fx :dispatch [::notifications/info "Deed added"])))

(rf/reg-event-fx ::add-deed-error
  [rf/debug]
  (fn [fx e]
    (assoc fx :dispatch [::notifications/error (.-message e)])))

(rf/reg-event-fx ::add-deed
  [rf/debug]
  (fn [fx [_ group-id user-id task-id deed-id]]
    (assoc fx :dispatch [::firebase/db [::groups group-id ::deeds deed-id]
                                       {:memberId user-id :taskId task-id}
                                       [::add-deed-done]
                                       [::add-deed-error]])))

(rf/reg-sub ::deeds
  (fn [[_ group-id] _]
    (rf/subscribe [::group/group group-id]))
  (fn [group _]
    (:deeds group)))

(rf/reg-sub ::tasks
  (fn [[_ group-id] _]
    (rf/subscribe [::group/group group-id]))
  (fn [group _]
    (:tasks group)))
