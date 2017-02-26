(ns chores.ui.panels.tasks
  (:require [re-frame.core :as rf]
            [chores.db.user :as user]
            [chores.db.task :as task]))

(defn on-add-deed [group-id user-id task-id]
  (let [deed-id (str (random-uuid))]
    (rf/dispatch [::task/add-deed group-id user-id task-id deed-id])))

(defn task-panel [{:keys [group-id user-id task-id task]}]
  [:div.task
   [:h3 (:name task)]
   [:h4 (str (:value task) " €")]
   [:p (:description task)]
   [:button {:on-click #(on-add-deed group-id user-id task-id)} "+"]])

(defn tasks-panel [{:keys [group-id]}]
  (let [tasks @(rf/subscribe [::task/tasks group-id])
        user @(rf/subscribe [::user/user])]
    [:ul.tasks
     (for [[task-id task] tasks]
       ^{:key task-id}
       [:li
        [task-panel {:group-id group-id :user-id (:id user) :task-id (name task-id) :task task}]])]))


