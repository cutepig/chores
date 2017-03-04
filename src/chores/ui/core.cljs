(ns chores.ui.core
  (:require [re-frame.core :as rf]
            [chores.fx.router :as router]
            [chores.db.user :as user]))

(defn with-auth [content]
  (let [auth @(rf/subscribe [::user/auth])]
    (if (some? auth)
      content
      (rf/dispatch [::router/push "/"]))))

