(ns re-frame-history.core
  (:require [reagent.ratom :as ratom]
            [re-frame.core :as rf]))

(defn reg-history [history]
  (rf/reg-sub-raw
    ::history
    (fn [db-atom [_ mapper-fn read-ev]]
      (println ::history mapper-fn read-ev)
      (let [read-fn #(rf/dispatch
                       (conj read-ev
                             (js->clj %1 :keywordize-keys true)
                             (js->clj %2 :keywordize-keys true)))
            unlisten (.listen history read-fn)]
        (read-fn (.-location history) nil)
        (ratom/make-reaction
          (fn [_] (mapper-fn @db-atom))
          :on-dispose unlisten)))))


