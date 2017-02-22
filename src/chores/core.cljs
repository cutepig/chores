(ns chores.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljsjs.firebase]
            [re-frame-firebase.core :as firebase]
            [re-frame-history.core :as history]
            [domkm.silk :as silk]
            [camel-snake-kebab.core]
            [chores.ui.user :as ui-user]
            [chores.ui.task :as ui-task]
            [chores.router :as router]))

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
                [::user [["groups" :group-id "users" :user-id]]]]))

(router/reg-route ::index
  (fn index-page [_]
    [:div.index-page
     [:h1 "Hello!"]]))

(router/reg-route ::groups
  (fn groups-page [_]
    [:div.groups-page
     [:h1 "Groups!"]]))

(router/reg-route ::group
  (fn group-page [_]
    [:div.group-page
     [:h1 "One single group!"]]))

(router/reg-route ::user
  (fn user-page [_]
    [:div.user-page
     [:h1 "User!"]]))

(router/reg-route :default
  (fn not-found-page [_]
    [:div.not-found-page
     [:h1 "Not found!"]]))

(defn hello-world []
  [:div.hello-world
   [:h1 "Hello world!"]
   ; Link tests
   [:a {:href "/"} "Hello!"]
   [:a {:href "/groups"} "Groups"]
   [:a {:href "/groups/123"} "Group 123"]
   [:a {:href "/groups/123/users/34"} "User 34"]
   [ui-user/user-panel]
   [ui-task/tasks-list {:group-id "d1dcedb2-e7be-401c-a71c-a5008d225916"}]
   [router/router {:routes routes}]])

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
