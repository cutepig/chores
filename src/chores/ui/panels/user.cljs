(ns chores.ui.panels.user
  (:require [re-frame.core :as rf]
            [chores.db.user :as user]))

(defn user-earnings-panel [{:keys [group-id user-id]}]
  (let [deeds @(rf/subscribe [::user/deeds group-id user-id])
        approved (->> deeds (filter :approved) (map #(get-in % [:task :value])) (reduce + 0))
        total (->> deeds (map #(get-in % [:task :value])) (reduce + 0))]
    [:div.user-earnings
     (str "Earned " approved " € / " total " €")]))


