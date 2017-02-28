(ns chores.ui.task
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [chores.fx.firebase :as firebase]))

(rf/reg-event-db ::set-tasks
  [rf/debug]
  (fn [db [_ tasks]] (assoc db ::tasks tasks)))

(rf/reg-sub ::get-tasks
  (fn [[_ group-id] _]
    (println ::get-tasks group-id)
    (rf/subscribe [::firebase/db [::groups group-id ::tasks]]))
  (fn [tasks _] tasks))

(defn task-card [{:keys [task]}]
  [:div.task-card
   [:h3 (:name task)]
   [:h4 (str (:value task) " €")]
   [:p (:description task)]
   [:button {:on-click (fn [ev]
                         (.pushState js/history #js {} nil (str "/tasks/" (:id task))))}

    "Add"]])

(defn tasks-list [{:keys [group-id]}]
  (let [tasks @(rf/subscribe [::get-tasks group-id])]
    (println :tasks-list tasks)
    [:div.tasks-list
     [:h2 "Pick your task"]
     [:ul
      (for [[task-id task] tasks]
        ^{:key task-id} [:li [task-card {:task task}]])]]))

