(ns chores.ui.user
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [chores.fx.firebase :as firebase]
            [re-frame-history.core :as history]))

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
(rf/reg-sub ::get-users
  (fn [_ _]
    (rf/subscribe [::firebase/db [::users]]))
  (fn [users _] users))

(rf/reg-sub ::get-user
  (fn [[_ user-id] _]
    (if (some? user-id)
      (rf/subscribe [::firebase/db [::users user-id]])
      (r/atom nil)))
  (fn [user _] user))

(rf/reg-sub ::get-deeds
  (fn [[_ group-id] _]
    (if (some? group-id)
      (rf/subscribe [::firebase/db [::groups group-id ::deeds]])
      (r/atom nil)))
  (fn [deeds _] deeds))

(rf/reg-sub ::get-deeds-by-user
  (fn [[_ group-id user-id] _]
    (if (and (some? group-id) (some? user-id))
      (rf/subscribe [::get-deeds group-id])
      (r/atom nil)))
  (fn [deeds [_ _ user-id]]
    (filter #(= (:memberId %) user-id) (vals deeds))))

(rf/reg-sub ::get-groups
  (fn [_ _]
    (rf/subscribe [::firebase/db [::groups]]))
  (fn [groups _] groups))

(defn user-deeds [{:keys [group-id user-id]}]
  (let [deeds @(rf/subscribe [::get-deeds-by-user group-id user-id])]
    [:div.user-deeds
     [:h2 "Deeds"]]))

(defn user-groups [{:keys [user-id]}]
  (let [user @(rf/subscribe [::get-user user-id])]
    [:div.user-groups
     ;; TODO: User name to firebase:users/$user-id
     [:h2 "User groups"]
     [:ul
      (for [[group-id name] (:groups user)]
        ^{:key group-id} [:li name])]]))

(defn login [ev]
  (.preventDefault ev)
  (let [form-data (js/FormData. (.-currentTarget ev))
        email (.get form-data "email")
        password (.get form-data "password")]
    (rf/dispatch [::firebase/auth email password [::set-auth] [::set-auth-error]])))

(defn signup [ev]
  (.preventDefault ev)
  (let [form-data (js/FormData. (.-currentTarget ev))
        email (.get form-data "email")
        password (.get form-data "password")
        password2 (.get form-data "password2")]
    (if (= password password2)
      (rf/dispatch [::firebase/signup email password [::set-auth] [::set-auth-error]]))))


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
    (assoc db ::location location)))

(defn user-panel []
  (let [auth-user @(rf/subscribe [::firebase/auth])
        user @(rf/subscribe [::get-user (if (nil? auth-user) nil (.-uid auth-user))])]
    [:div.user-panel
     [:h2 "Current user"]
     (if (nil? auth-user)
       [user-info-panel {:auth-user auth-user :user user}])]))



