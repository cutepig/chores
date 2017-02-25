(ns chores.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljsjs.firebase]
            [re-frame-firebase.core :as firebase]
            [re-frame-history.core :as history]
            [domkm.silk :as silk]
            [camel-snake-kebab.core]
            [chores.router :as router]
            [chores.ui.screens.home :as home]
            [chores.ui.screens.groups :as groups]))

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
  (silk/routes [[::home/index [[]]]
                [::groups/groups [["groups"]]]
                [::group [["groups" :group-id]]]
                [::user [["groups" :group-id "users" :user-id]]]]))

(router/reg-route ::group
  (fn group-page [{:keys [group-id]}]
    [:div.group-page
     [:h1 (str "One single group! " group-id)]]))

(router/reg-route ::user
  (fn user-page [{:keys [group-id user-id]}]
    [:div.user-page
     [:h1 (str "User! " group-id ":" user-id)]]))

(router/reg-route :default
  (fn not-found-page [_]
    [:div.not-found-page
     [:h1 "Not found!"]]))

(defn top-panel []
  [:div.top-panel
   [:h1 "Hello world!"]
   ; Link tests
   [:a {:href "/"} "Hello!"]
   [:a {:href "/groups"} "Groups"]
   [:a {:href "/groups/123"} "Group 123"]
   [:a {:href "/groups/123/users/34"} "User 34"]
   [:button {:on-click #(rf/dispatch [::history/push "/foo"])} "Foo!"]])

(defn chores-screen []
  [:div.chores
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
  (r/render-component [chores-screen] (. js/document (getElementById "app"))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ; (rf/dispatch [:initialize-db])
)
