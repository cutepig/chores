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

(defn router [{:keys [routes]}]
  (let [location @(rf/subscribe [::history/location #(get % ::location) [::location]])]
    [:div.router
       [:h2 "Router here!"]
       ; @see https://github.com/ReactTraining/history#listening
       [:h3 (.-pathname location)]]))


