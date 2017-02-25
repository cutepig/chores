(ns chores.ui.screens.group
  (:require [re-frame.core :as rf]
            [re-frame-history.core :as history]
            [chores.router :as router]
            [chores.db.group :as group]
            [chores.ui.core :as ui]
            [chores.ui.panels.tasks :as tasks]
            [chores.ui.layouts.main :refer [main-layout]]))

(defn group-screen [{:keys [group-id]}]
  (ui/with-auth
    (let [group @(rf/subscribe [::group/group group-id])]
      [main-layout
       [:div.group
        [:h2 (:name group)]
        [tasks/tasks-panel {:group-id group-id}]]])))

(router/reg-route ::group group-screen)
