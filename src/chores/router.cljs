(ns chores.router
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [domkm.silk :as silk]
            [re-frame-history.core :as history]))

;; TODO: Wrap this so that we pluck the query params from [::silk/url :query]
(defmulti reg-route-fn (fn [route] (::silk/name route)))
;; TODO: Allow user to define this (you can just redefine this right?)
(defmethod reg-route-fn :default [route]
  (js/console.warn "Default route handler called with" route))

(defn reg-route [key route-fn]
  (defmethod reg-route-fn key [& params]
    (js/console.info "reg-route-fn" key "calling" route-fn "with" params)
    (apply route-fn params)))

(rf/reg-event-db ::location
  (fn [db [_ location]]
    (assoc db ::location location)))

(rf/reg-sub ::route
  (fn [[_ routes] _]
    (rf/subscribe [::history/location #(get % ::location) [::location]]))
  (fn [location [_ routes]]
    ; @see https://github.com/ReactTraining/history#listening
    (when (some? location)
      (silk/arrive routes (.-pathname location)))))

(defn router [{:keys [routes]}]
  (let [route @(rf/subscribe [::route routes])
        node (or (reg-route-fn route) [:div])]
    (into node [:div.router
                 [:h2 "Router here!"]
                 [:h3 (str route)]])))


