(ns chores.ui.core
  (:require [re-frame.core :as rf]
            [chores.fx.history :as history]
            [chores.db.user :as user]))

(defn with-auth [content]
  (let [auth @(rf/subscribe [::user/auth])]
    (if (some? auth)
      content
      (rf/dispatch [::history/push "/"]))))

