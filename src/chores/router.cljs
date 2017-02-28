(ns chores.router
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [domkm.silk :as silk]
            [chores.fx.history :as history]))

(defn make-click-handler []
  (fn [ev]
    (let [target (.-target ev)]
      (when (and (= "A" (.-tagName target))
              (= (.-origin target) (.-origin js/location)))
            (do (.preventDefault ev)
              (println ::click-handler ev target)
              ;; FIXME: Convert `target` to location object
              (rf/dispatch [::history/push target.pathname nil]))))))

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
    (when (some? location)
      ; @see https://github.com/ReactTraining/history#listening
      ; TODO: Include search params
      (silk/arrive routes (.-pathname location)))))

(defn router [{:keys [routes]}]
  ;; TODO: Use `create-class` and register the handler to the root dom node
  (r/with-let [click-handler (make-click-handler)
               _ (.addEventListener js/document "click" click-handler true)]
    (let [route @(rf/subscribe [::route routes])
          children (r/children (r/current-component))
          node (or (reg-route-fn route) [:div])]
      (into node children))
    (finally
      (.removeEventListener js/document "click" click-handler true))))


