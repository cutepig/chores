(ns chores.ui.screens.group
  (:require [re-frame.core :as rf]
            [chores.fx.router :as router]
            [chores.db.group :as group]
            [chores.db.user :as user]
            [chores.ui.core :as ui]
            [chores.ui.panels.tasks :as tasks]
            [chores.ui.panels.user :refer [users-panel user-earnings-panel]]
            [chores.ui.layouts.main :refer [main-layout]]))

(defn group-screen [{:keys [group-id]}]
  (ui/with-auth
    (let [group @(rf/subscribe [::group/group group-id])
          user @(rf/subscribe [::user/user])]
      [main-layout
       [:div.group
        [:h2 (:name group)]
        [users-panel {:group-id group-id}]
        [user-earnings-panel {:group-id group-id :user-id (:id user)}]
        [tasks/tasks-panel {:group-id group-id :user-id (:id user)}]]])))

(router/reg-route ::me group-screen)
(router/reg-route ::user group-screen)
