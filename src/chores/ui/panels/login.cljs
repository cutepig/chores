(ns chores.ui.panels.login
  (:require [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]
            [chores.db.user :as user]))

;; TODO: Display maybe a notification or smth
(rf/reg-event-db ::login
  [rf/debug]
  (fn [db [_ user]]
    (assoc db ::user user)))

(rf/reg-event-fx ::signup
  [rf/debug]
  (fn [fx [_ user]]
    (if (some? user)
      (assoc fx :dispatch [::firebase/db [::users (.-uid user)] {:id (.-uid user)}])
      fx)))

;; TODO
(rf/reg-event-db ::login-error
  [rf/debug]
  (fn [db _] db))

;; TODO
(rf/reg-event-db ::signup-error
  [rf/debug]
  (fn [db _] db))

(defn on-login [ev]
  (.preventDefault ev)
  (let [form-data (js/FormData. (.-currentTarget ev))
        email (.get form-data "email")
        password (.get form-data "password")]
    (rf/dispatch [::user/login :email {:email email :password password}])))

(defn on-signup [ev]
  (.preventDefault ev)
  (let [form-data (js/FormData. (.-currentTarget ev))
        email (.get form-data "email")
        password (.get form-data "password")
        password2 (.get form-data "password2")]
    (if (= password password2)
      (rf/dispatch [::firebase/signup email password [::signup] [::signup-error]]))))

(defn signup-panel []
  [:div.signup
   [:form {:on-submit on-signup}
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

(defn login-panel []
  [:div.login
   [:form {:on-submit on-login}
    [:h3 "Login"]
    [:label
     "E-mail"
     [:input {:name "email" :type :email}]]
    [:label
     "Password"
     [:input {:name "password" :type :password}]]
    [:button {:type :submit} "Login"]]
   [:button {:on-click #(rf/dispatch [::user/login :google])}
            "Login with Google"]])

