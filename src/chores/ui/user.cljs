(ns chores.ui.user
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame-firebase.core :as firebase]
            [re-frame-history.core :as history]))

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

;; FIXME: Should be indexed by group-id?
(rf/reg-event-db ::set-deeds
  [rf/debug]
  (fn [db [_ deeds]] (assoc db ::deeds deeds)))

(rf/reg-sub ::get-users
  (fn [_ _]
    (rf/subscribe [::firebase/db "/users" #(get % :users) [::set-users]]))
  (fn [users _] users))

(rf/reg-sub ::get-user
  (fn [[_ user-id] _]
    ;; FIXME: Don't call when user-id is nil
    (rf/subscribe [::firebase/db
                   (str "/users/" user-id)
                   #(get-in % [::users user-id])
                   [::set-user user-id]]))
  (fn [user _] user))

(rf/reg-sub ::get-deeds
  (fn [[_ group-id] _]
    ;; FIXME: Don't call when group-id is nil
    (rf/subscribe [::firebase/db
                   (str "/groups/" group-id "/deeds")
                   #(get % ::deeds)
                   [::set-deeds]]))
  (fn [deeds _] deeds))

(rf/reg-sub ::get-deeds-by-user
  (fn [[_ group-id user-id] _]
    ;; FIXME: Don't call when group-id  or user-id is nil
    (rf/subscribe [::get-deeds group-id]))
  (fn [deeds [_ _ user-id]]
    (filter #(= (:memberId %) user-id) (vals deeds))))

(rf/reg-sub ::get-groups
  (fn [_ _]
    (rf/subscribe [::firebase/db
                   "/groups"
                   #(get-in % [::groups])
                   [::set-groups]]))
  (fn [groups _] groups))

(defn user-deeds [{:keys [group-id user-id]}]
  (let [deeds @(rf/subscribe [::get-deeds-by-user group-id user-id])]
    (println ::user-deeds deeds)
    [:div.user-deeds
     [:h2 "Deeds"]]))

(defn user-groups [{:keys [user-id]}]
  (let [user @(rf/subscribe [::get-user user-id])]
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

(defn login-panel []
  [:div.login-panel
   [:form.login-panel-login {:on-submit login}
    [:h3 "Login"]
    [:label
     "E-mail"
     [:input {:name "email" :type :email}]]
    [:label
     "Password"
     [:input {:name "password" :type :password}]]
    [:button {:type :submit} "Login"]]
   [:div.login-panel-signup {:on-submit signup}
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

(defn user-info-panel [{:keys [auth-user user]}]
  [:div.user-info-panel
   [:pre (js/JSON.stringify auth-user nil 2)]
   [:pre (js/JSON.stringify (clj->js user) nil 2)]
   [user-groups {:user-id (:id user)}]
   [user-deeds {:user-id (:id user) :group-id "d1dcedb2-e7be-401c-a71c-a5008d225916"}]
   [:button {:on-click #(rf/dispatch [::firebase/logout
                                      [::set-auth]
                                      [::set-auth-error]])}
    "Logout"]])

(rf/reg-event-db ::location
  [rf/debug]
  (fn [db [_ location]]
    (println ::location location)
    (assoc db ::location location)))

(defn user-panel []
  (let [auth-user @(rf/subscribe [::firebase/auth
                                  #(get-in % [::auth])
                                  [::set-auth]])
        ;; NOTE: Will N subscriptions cause N redispatched actions to go out?
        ;; Specially when using `::get-user` that will redispatch with `::set-user`
        user @(rf/subscribe [::get-user (if (nil? auth-user) nil (.-uid auth-user))])]

    [:div.user-panel
     [:div {:on-click #(rf/dispatch [::history/push "/groups"])} "Link test"]
     [:h2 "Current user"]
     (if (nil? auth-user)
       [login-panel]
       [user-info-panel {:auth-user auth-user :user user}])]))



