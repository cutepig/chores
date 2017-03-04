(ns chores.fx.firebase
  (:require [clojure.string :as s]
            [reagent.ratom :as ratom]
            [re-frame.core :as rf]
            [cljsjs.firebase]))

(rf/reg-event-db ::db-write
  [rf/debug]
  (fn [db [_ path-kv value]]
    (assoc-in db path-kv value)))

(defn kv->path [kv]
  (->> kv
       (map #(if (keyword? %) (name %) %))
       (s/join "/")
       (str "/")))

(defn reg-firebase [firebase-app]
  (let [fb-db (.database firebase-app)
        fb-auth (.auth firebase-app)]

    ;; Database subscription
    (rf/reg-sub-raw
      ::db
      (fn [db-atom [_ path-kv]]
        (let [-ref (.ref fb-db (kv->path path-kv))
              read-fn #(rf/dispatch [::db-write path-kv (js->clj (.val %) :keywordize-keys true)])
              unref (.on -ref "value" read-fn)]
          (ratom/make-reaction
            (fn [] (get-in @db-atom path-kv))
            :on-dispose #(.off -ref "value" read-fn)))))

    ;; Authentication subscription
    (rf/reg-sub-raw
      ::auth
      (fn [db-atom _]
        ;; NOTE: No point in converting `User` instance to clj
        (let [read-fn #(rf/dispatch [::db-write [::auth] %])
              unref (.onAuthStateChanged fb-auth read-fn)]
          ;; NOTE: Not doing initial dispatch for current user
          (ratom/make-reaction
            (fn [] (get @db-atom ::auth))
            :on-dispose unref))))

    ;; DB service
    (rf/reg-fx
      ::db
      ;; TODO: Make the action configurable with different actions:
      ;; `set`, `update`, `remove`, `push`
      (fn [[path-kv data done-ev error-ev]]
        (-> (.ref fb-db (kv->path path-kv))
            (.set (clj->js data))
            (.then (if (some? done-ev) #(rf/dispatch done-ev) identity)
                   (if (some? error-ev) #(rf/dispatch (conj error-ev %)) identity)))))

    ;; Auth service
    (rf/reg-fx
      ::auth
      (fn [[type {:keys [done-ev error-ev] :as params}]]
        (condp = type
          :email (-> fb-auth
                     (.signInWithEmailAndPassword (:email params) (:password params))
                     ;; NOTE: No point in converting `User` instance to clj
                     (.then #(rf/dispatch (conj done-ev %))
                            #(rf/dispatch (conj error-ev %))))
          :signup (-> fb-auth
                      (.createUserWithEmailAndPassword (:email params) (:password params))
                      ;; NOTE: No point in converting `User` instance to clj
                      (.then #(rf/dispatch (conj done-ev %))
                             #(rf/dispatch (conj error-ev %))))
          :logout (-> (.signOut fb-auth)
                      (.then #(rf/dispatch (conj done-ev %))
                             #(rf/dispatch (conj error-ev %))))
          :google (let [provider (new js/firebase.auth.GoogleAuthProvider)]
                    (.addScope provider "profile")
                    (.addScope provider "email")
                    (-> fb-auth
                        (.signInWithPopup provider)
                        ;; FIXME: Should we utilize `(.-credential %)` somehow?
                        ;; @see https://firebase.google.com/docs/reference/js/firebase.auth#.UserCredential
                        (.then #(rf/dispatch (conj done-ev (.-user %)))
                               #(rf/dispatch (conj error-ev %))))))))

    ;; DB event
    (rf/reg-event-fx
      ::db
      [rf/debug]
      (fn [cofx [_ path data done-ev error-ev]]
        (assoc cofx ::db [path data done-ev error-ev])))

    ;; Auth event
    (rf/reg-event-fx
      ::auth
      [rf/debug]
      (fn [cofx [_ type params]]
        (assoc cofx ::auth [type params])))

    ;; Signup event
    (rf/reg-event-fx
      ::signup
      [rf/debug]
      (fn [cofx [_ email password done-ev error-ev]]
        (assoc cofx ::auth [:signup {:email email
                                     :password password
                                     :done-ev done-ev
                                     :error-ev error-ev}])))

    (rf/reg-event-fx
      ::logout
      [rf/debug]
      (fn [cofx [_ done-ev error-ev]]
        (assoc cofx ::auth [:logout {:done-ev done-ev error-ev error-ev}])))))

(defn initialize-firebase [config]
  (try
    (js/firebase.initializeApp (clj->js config))
    (catch js/Object e
      (js/console.warn e)
      (.app js/firebase))))
