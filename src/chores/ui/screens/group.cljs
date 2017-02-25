(ns chores.ui.screens.group
  (:require [re-frame.core :as rf]
            [re-frame-history.core :as history]
            [chores.router :as router]
            [chores.db.group :as group]
            [chores.ui.panels.tasks :as tasks]))

(defn group-screen [{:keys [group-id]}]
  (println ::group-screen group-id)
  (let [group @(rf/subscribe [::group/group group-id])]
    [:div.group
     [:h2 (:name group)]
     [tasks/tasks-panel {:group-id group-id}]]))

(router/reg-route ::group group-screen)
