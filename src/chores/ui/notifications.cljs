(ns chores.ui.notifications
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn add-notification [db type text actions]
  (update db ::notifications #(conj (into [] %) [type text actions])))

(rf/reg-event-db ::add
  [rf/debug]
  (fn [db [_ text & actions]]
    (add-notification db nil text actions)))

(rf/reg-event-db ::info
  [rf/debug]
  (fn [db [_ text & actions]]
    (add-notification db :info text actions)))

(rf/reg-event-db ::success
  [rf/debug]
  (fn [db [_ text & actions]]
    (add-notification db :info text actions)))

(rf/reg-event-db ::warn
  [rf/debug]
  (fn [db [_ text & actions]]
    (add-notification db :warning text actions)))

(rf/reg-event-db ::error
  [rf/debug]
  (fn [db [_ text & actions]]
    (add-notification db :danger text actions)))

(defn remove-notification [notifications idx]
  (into [] (concat (subvec notifications 0 idx) (subvec notifications (inc idx)))))

(rf/reg-event-db ::remove
  [rf/debug]
  (fn [db [_ idx]]
    (update db ::notifications #(remove-notification % idx))))

(rf/reg-sub ::notifications
  (fn [db _]
    (::notifications db)))

(defn type->cls [type]
  (condp = type
    nil nil
    :info "is-info"
    :success "is-success"
    :warning "is-warning"
    :danger "is-danger"))

(defn notifications-panel []
  (let [notifications @(rf/subscribe [::notifications])]
    [:div.notifications
      [:ul
        (for [idx (reverse (range (count notifications)))
              :let [[type text & actions] (nth notifications idx)]]
          (into
            ^{:key idx}
            [:li.notification {:class (type->cls type)}
              [:button.delete {:on-click #(rf/dispatch [::remove idx])}]
              [:p text]]
            actions))]]))

