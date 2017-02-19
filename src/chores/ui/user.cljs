(ns chores.ui.user
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]))

(rf/reg-event-db ::set-users
  [rf/debug]
  (fn [db [_ users]]
    (assoc db :users users)))

(rf/reg-event-db ::set-user
  [rf/debug]
  (fn [db [_ user-id user]]
    (assoc-in db [::users user-id] user)))

(rf/reg-event-db ::set-groups
  [rf/debug]
  (fn [db [_ groups]]
    (assoc-in db [::groups] groups)))

(rf/reg-event-db ::set-auth
  [rf/debug]
  (fn [db [_ auth]]
    (assoc-in db [::auth] auth)))

(rf/reg-event-db ::set-auth-error
  [rf/debug]
  (fn [db _] db))

(rf/reg-sub ::firebase-users
  (fn [_ _]
    (rf/subscribe [::firebase/db "/users" #(get % :users) [::set-users]])))

(rf/reg-sub ::firebase-user
  (fn [[_ user-id] x]
    (rf/subscribe [::firebase/db
                   (str "/users/" user-id)
                   #(get-in % [::users user-id])
                   [::set-user user-id]]))
  (fn [user _] user))

(rf/reg-sub ::firebase-groups
  (fn [_ _]
    (rf/subscribe [::firebase/db
                   "/groups"
                   #(get-in % [::groups])
                   [::set-groups]]))
  (fn [groups _] groups))

(defn user-groups [{:keys [user-id]}]
  (let [user @(rf/subscribe [::firebase-user user-id])]
    (println :user-groups user)
    [:div.user-groups
     ;; TODO: User name to firebase:users/$user-id
     [:h2 "User groups"]
     [:ul
      (for [[group-id name] (:groups user)]
        ^{:key group-id} [:li name])]]))

(defn login [ev]
  (println "login" ev)
  (.preventDefault ev)
  (let [form-data (js/FormData. (.-currentTarget ev))
        email (.get form-data "email")
        password (.get form-data "password")]
    (rf/dispatch [::firebase/auth email password [::set-auth] [::set-auth-error]])))

(defn signup [ev]
  (println "signup" ev)
  (.preventDefault ev)
  (let [form-data (js/FormData. (.-currentTarget ev))
        email (.get form-data "email")
        password (.get form-data "password")
        password2 (.get form-data "password2")]
    (if (= password password2)
      (rf/dispatch [::firebase/signup email password [::set-auth] [::set-auth-error]]))))

(defn user-login-panel []
  [:div.user-login-panel
   [:form {:on-submit login}
    [:h3 "Login"]
    [:label
     "E-mail"
     [:input {:name "email" :type :email}]]
    [:label
     "Password"
     [:input {:name "password" :type :password}]]
    [:button {:type :submit} "Login"]]
   [:form {:on-submit signup}
    [:h3 "Sign up"]
    [:label
     "E-mail"
     [:input {:name "email" :type :email}]]
    [:label
     "Password"
     [:input {:name "password" :type :password}]]
    [:label
     "Password again"
     [:input {:name "password2" :type :password}]]
    [:button {:type :submit} "Signup"]]])

(defn user-info-panel [{:keys [user]}]
  [:div.user-info-panel
   [:pre (js/JSON.stringify user nil 2)]
   [:button {:on-click #(rf/dispatch [::firebase/logout
                                      [::set-auth]
                                      [::set-auth-error]])}
    "Logout"]])

(defn user-panel []
  (let [user @(rf/subscribe [::firebase/auth
                             #(get-in % [::auth])
                             [::set-auth]])]
    [:div.user-panel
     [:h2 "Current user"]
     (if (nil? user)
       [user-login-panel]
       [user-info-panel {:user user}])]))



