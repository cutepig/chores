(ns chores.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljsjs.firebase]
            [re-frame-firebase.core :as firebase]
            [camel-snake-kebab.core]
            [chores.ui.user :as ui-user]))

;; TODO: Use `camel-snake-kebab to convert between "memberId" to :member-id
;; etc..

(enable-console-print!)

(println "This text is printed from src/chores/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

;; FIXME: Not liking this, setting everything in one place. Distribute! Distribute!
(rf/reg-event-db :initialize-db
  (fn [_ _]
    {:users {"432fe1b1-d664-4ecc-9f10-f141637d37e1"
                {:id "432fe1b1-d664-4ecc-9f10-f141637d37e1"
                 :name "John Doe"}
             "8a29db11-c98f-4cbf-b49b-59f0c0cbf5ba"
                {:id "8a29db11-c98f-4cbf-b49b-59f0c0cbf5ba"
                 :name "Jane Doe"}}
     ;; TODO: `tasks` aren't related to users, we either use an
     ;; intermediary `:tasks-by-user` db or connect via `[:users user-id :tasks]`
     :tasks {"6f098720-a532-48f0-bac1-563360ef1d66"
                {:id "6f098720-a532-48f0-bac1-563360ef1d66"
                 :name "Imuroi"
                 :price 3}
             "1b496ac1-a3f6-4418-b90f-a43035e34daa"
                {:id "1b496ac1-a3f6-4418-b90f-a43035e34daa"
                 :name "Tiskaa"
                 :price 3}}
     :deeds [{:task-id "1b496ac1-a3f6-4418-b90f-a43035e34daa"
              :user-id "432fe1b1-d664-4ecc-9f10-f141637d37e1"
              :approved true}
             {:task-id "6f098720-a532-48f0-bac1-563360ef1d66"
              :user-id "432fe1b1-d664-4ecc-9f10-f141637d37e1"}
             {:task-id "6f098720-a532-48f0-bac1-563360ef1d66"
              :user-id "8a29db11-c98f-4cbf-b49b-59f0c0cbf5ba"}]}))

(defn select [params]
  @(rf/subscribe [(first params) (rest params)]))

(rf/reg-sub :get-user
  (fn [db [_ [user-id]]]
    (get-in db [:users user-id])))

(rf/reg-sub :get-tasks (fn [db _] (:tasks db)))

(rf/reg-sub :get-deeds (fn [db _] (:deeds db)))

(rf/reg-sub :get-deeds-by-user-id
  (fn [db [_ [user-id]]]
    (let [tasks (:tasks db)]
      (->> (:deeds db)
           (map #(assoc % :task (get tasks (:task-id %))))))))

(rf/reg-event-db :add-deed
  (fn [db [_ user-id task-id]]
    (update db :deeds #(conj % {:user-id user-id :task-id task-id}))))

(defn user-view [{:keys [user-id]}]
  (let [deeds (select [:get-deeds-by-user-id user-id])
        user (select [:get-user user-id])]
    [:div.user
     [:h2 (:name user)]
     [:ul
      (for [deed deeds]
        [:li (get-in deed [:task :name])])]]))

(defn tasks-view []
  (let [tasks (select [:get-tasks])]
    (println :tasks-view tasks)
    [:div.tasks
     [:h2 "Tasks"]
     [:ul
       (for [task (vals tasks)]
         ^{:key (:id task)}
         [:li
          (str (:name task) " - " (:price task) "€")
          [:button {:on-click #(rf/dispatch
                                 [:add-deed
                                  "432fe1b1-d664-4ecc-9f10-f141637d37e1"
                                  (:id task)])}
           "Add deed"]])]]))

(defn hello-world []
  [:div.hello-world
   [:h1 "Hello world!"]
   [user-view {:user-id "432fe1b1-d664-4ecc-9f10-f141637d37e1"}]
   [tasks-view]
   [ui-user/user-groups {:user-id "ZWqmz3Ma7CQbg7orX1ny9NIzrx23"}]])

(def firebase-config {:apiKey "AIzaSyDg0XgimVokGyOIQREFSSUow441WFx5O1w"
                      ;; TODO: Is this `localhost` in local development?
                      :authDomain "moneyfunnysonny.firebaseapp.com"
                      :databaseURL "https://moneyfunnysonny.firebaseio.com/"
                      ; :storageBucket "<BUCKET>.appspot.com"
                      ; :messagingSenderId "<SENDER_ID>"
                     })

(do
  (rf/dispatch [:initialize-db])
  (firebase/reg-firebase (firebase/initialize-firebase firebase-config))
  (r/render-component [hello-world] (. js/document (getElementById "app"))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ; (rf/dispatch [:initialize-db])
)
