(ns chores.fx.router
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [re-frame.core :as rf]
            [domkm.silk :as silk]
            [pushy.core :as pushy]))

;; TODO: Wrap this so that we pluck the query params from [::silk/url :query]
(defmulti reg-route-fn (fn [route] (::silk/name route)))
;; TODO: Allow user to define this (you can just redefine this right?)
(defmethod reg-route-fn :default [route]
  (js/console.warn "Default route handler called with" route))

;; Call matched route component with route parameters
(defn reg-route [key route-fn]
  (defmethod reg-route-fn key [& params]
    (js/console.info "reg-route-fn" key "calling" route-fn "with" params)
    (apply route-fn params)))

(rf/reg-event-fx ::push
  [rf/debug]
  (fn [fx params]
    (assoc fx ::pushy params)))

(rf/reg-event-fx ::replace
  [rf/debug]
  (fn [fx params]
    (assoc fx ::pushy params)))

(rf/reg-event-db ::route
  (fn [db [_ match]]
    (assoc db ::route match)))

(rf/reg-sub ::route
  (fn [db _]
    (::route db)))

(defn reg-router [routes]
  (let [history (pushy/pushy #(rf/dispatch [::route %]) (partial silk/arrive routes))]
    (rf/reg-sub-raw
      ::route
      (fn [db-atom _]
        (pushy/start! history)
        ;; TODO: Do we need initial routing?
        ;; (get-token history)
        (ratom/make-reaction
          (fn [] (get @db-atom ::route))
          :on-dispose #(pushy/stop! history))))

    (rf/reg-fx
      ::pushy
      (fn [[op & params]]
        (condp = op
          ::push (apply pushy/set-token! history params)
          ::replace (apply pushy/replace-token! history params))))))

(defn router [extra-params]
  (let [route @(rf/subscribe [::route])
        children (r/children (r/current-component))
        node (or (reg-route-fn (merge extra-params route)) [:div])]
    (into node children)))


