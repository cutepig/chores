(ns chores.ui.panels.user
  (:require [re-frame.core :as rf]
            [chores.db.user :as user]
            [chores.db.group :as group]))

(defn users-panel [{:keys [group-id]}]
  (let [users @(rf/subscribe [::group/users group-id])]
    [:div.users
     [:h4 "Users"]
     [:ul
      (for [[user-id user] users]
        ^{:key user-id}
        [:li [:a {:href (str "/g/" (name group-id) "/u/" (name user-id))} (name user-id)]])]]))

(defn user-earnings-panel [{:keys [group-id user-id]}]
  (let [deeds @(rf/subscribe [::user/deeds group-id user-id])
        approved (->> deeds (filter :approved) (map #(get-in % [:task :value])) (reduce + 0))
        total (->> deeds (map #(get-in % [:task :value])) (reduce + 0))]
    [:div.user-earnings
     (str "Earned " approved " € / " total " €")]))


