(ns chores.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljsjs.firebase]
            [re-frame-firebase.core :as firebase]
            [re-frame-history.core :as history]
            [domkm.silk :as silk]
            [camel-snake-kebab.core]
            [chores.ui.user :as ui-user]
            [chores.ui.task :as ui-task]))

;; TODO: Use `camel-snake-kebab to convert between "memberId" to :member-id
;; etc..

(enable-console-print!)

(println "This text is printed from src/chores/core.cljs. Go ahead and edit it and see reloading in action.")

;; FIXME: Not liking this, setting everything in one place. Distribute! Distribute!
(rf/reg-event-db :initialize-db (fn [db _] db))

;; TODO: Move to utils
(defn select [params]
  @(rf/subscribe [(first params) (rest params)]))

(def routes
  (silk/routes [[::index [[]]]
                [::groups [["groups"]]]
                [::group [["groups" :group-id]]]
                [::user [["groups" :group-id "users" :user-id]]]
                [::user [["bar" :group-id "users" :user-id]]]]))

(defn hello-world []
  [:div.hello-world
   [:h1 "Hello world!"]
   [ui-user/user-panel]
   [ui-task/tasks-list {:group-id "d1dcedb2-e7be-401c-a71c-a5008d225916"}]])

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
  (history/reg-history (.createBrowserHistory js/History))
  (r/render-component [hello-world] (. js/document (getElementById "app"))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ; (rf/dispatch [:initialize-db])
)
