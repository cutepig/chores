(ns chores.ui.panels.tasks
  (:require [re-frame.core :as rf]
            [chores.db.task :as task]))

(defn tasks-panel [{:keys [group-id]}]
  (let [tasks @(rf/subscribe [::task/tasks group-id])]
    [:ul.tasks
     (for [[task-id task] tasks]
       ^{:key task-id}
       [:li
        [:h3 (:name task)]
        [:h4 (str (:value task) " €")]
        [:p (:description task)]])]))

