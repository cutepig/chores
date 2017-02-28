(ns chores.fx.history
  (:require [reagent.ratom :as ratom]
            [re-frame.core :as rf]))

(rf/reg-event-fx ::push
  (fn [fx [_ location state]]
    (assoc fx ::location [::push location state])))

(rf/reg-event-fx ::replace
  (fn [fx [_ location state]]
    (assoc fx ::location [::replace location state])))

(defn reg-history [history]
  (set! js/__history__ history)

  (rf/reg-sub-raw
    ::location
    (fn [db-atom [_ mapper-fn read-ev]]
      (println ::history read-ev)
      ;; TODO: Map location and state objects properly between cljs and js
      (let [read-fn #(rf/dispatch (conj read-ev %1 %2))
            unlisten (.listen history read-fn)]
        (read-fn (.-location history) nil)
        (ratom/make-reaction
          (fn [_] (mapper-fn @db-atom))
          :on-dispose unlisten))))

  (rf/reg-fx
    ::location
    (fn [[op pathname state]]
      (condp = op
        ;; TODO: Support state
        ::push (.push history pathname state)
        ::replace (.replace history pathname state)))))


