(ns chores.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(enable-console-print!)

(println "This text is printed from src/chores/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(rf/reg-event-db :initialise-db
  (fn [_ _]
    (println "Initializing db")
    {:users {#uuid "432fe1b1-d664-4ecc-9f10-f141637d37e1"
                {:id #uuid "432fe1b1-d664-4ecc-9f10-f141637d37e1"
                 :name "John Doe"}}
     :tasks {#uuid "6f098720-a532-48f0-bac1-563360ef1d66"
                {:id #uuid "6f098720-a532-48f0-bac1-563360ef1d66"
                 :user-id #uuid "432fe1b1-d664-4ecc-9f10-f141637d37e1"}
             #uuid "1b496ac1-a3f6-4418-b90f-a43035e34daa"
                {:id #uuid "1b496ac1-a3f6-4418-b90f-a43035e34daa"
                 :user-id #uuid "432fe1b1-d664-4ecc-9f10-f141637d37e1"}}}))

(defn select [params]
  @(rf/subscribe [(first params) (rest params)]))

(rf/reg-sub :get-user
  (fn [db [_ [user-id]]]
    (get-in db [:users (uuid user-id)])))

(rf/reg-sub :get-tasks (fn [db _] (:tasks db)))

(rf/reg-sub :get-tasks-by-user-id
  (fn [_ _] (rf/subscribe [:get-tasks]))
  (fn [tasks [_ [user-id]]]
    (let [-user-id (uuid user-id)]
      (filter #(= (:user-id %) -user-id) (vals tasks)))))

(defn user-view [{:keys [user-id]}]
  (let [user (select [:get-user user-id])
        tasks (select [:get-tasks-by-user-id user-id])]
    (println :user-view user tasks)
    [:div.user
     [:h2 (:name user)]
     [:ul
      (for [task tasks]
        ^{:key (:id task)} [:li (:id task)])]]))

(defn hello-world []
  [:div.hello-world
   [:h1 "Hello world!"]
   [user-view {:user-id "432fe1b1-d664-4ecc-9f10-f141637d37e1"}]])

(r/render-component [hello-world]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (rf/dispatch [:initialise-db])
)
