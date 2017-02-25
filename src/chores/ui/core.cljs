(ns chores.ui.core
  (:require [re-frame.core :as rf]
            [re-frame-history.core :as history]
            [chores.db.user :as user]))

(defn with-auth [content]
  (let [auth @(rf/subscribe [::user/auth])]
    (if (some? auth)
      content
      (rf/dispatch [::history/push "/"]))))

