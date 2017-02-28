(ns re-frame-firebase.core
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

    ;;
    ;; Database subscription
    (rf/reg-sub-raw
      ::db
      (fn [db-atom [_ path-kv]]
        (let [path (kv->path path-kv)
              -ref (.ref fb-db path)
              read-fn #(rf/dispatch [::db-write path-kv (js->clj (.val %) :keywordize-keys true)])
              unref (.on -ref "value" read-fn)]
          (ratom/make-reaction
            (fn [] (get-in @db-atom path-kv))
            :on-dispose #(.off -ref "value" read-fn)))))

    ;; Authentication subscription
    ;; TODO: Drop the `mapper` and `read-ev` totally, we write and read from ::db
    ;; Just supply a handler for errors
    (rf/reg-sub-raw
      ::auth
      (fn [db-atom [_ mapper read-ev]]
        ;; NOTE: No point in converting `User` instance to clj
        (let [read-fn #(rf/dispatch (conj read-ev %))
              unref (.onAuthStateChanged fb-auth read-fn)]
          ;; NOTE: Not doing initial dispatch for current user
          (ratom/make-reaction
            (fn [] (mapper @db-atom))
            :on-dispose unref))))

    ;; DB service
    (rf/reg-fx
      ::db
      ;; TODO: Make the action configurable with different actions:
      ;; `set`, `update`, `remove`, `push`
      (fn [[path-kv data done-ev error-ev]]
        (println ::db-fx path-kv data (kv->path path-kv))
        (-> (.ref fb-db (kv->path path-kv))
            (.set (clj->js data))
            (.then (if (some? done-ev) #(rf/dispatch done-ev) identity)
                   (if (some? error-ev) #(rf/dispatch (conj error-ev %)) identity)))))

    ;; Auth service
    (rf/reg-fx
      ::auth
      ;; TODO: Support all types of logins as well as signup
      (fn [[type {:keys [done-ev error-ev] :as params}]]
        (println ::auth-fx type done-ev error-ev)
        (condp = type
          :email (-> fb-auth
                     (.signInWithEmailAndPassword (:email params) (:password params))
                     ;; NOTE: No point in converting `User` instance to clj
                     (.then #(rf/dispatch (conj done-ev %))
                            #(rf/dispatch (conj error-ev %))))
          :google (let [provider (new js/firebase.auth.GoogleAuthProvider)]
                    (.addScope provider "profile")
                    (.addScope provider "email")
                    (println ::google provider)
                    (-> fb-auth
                        (.signInWithPopup provider)
                        (.then #(rf/dispatch (conj done-ev (.-result %)))
                               #(rf/dispatch (conj error-ev %))))))))

    ;; TODO: Hide logins and signups behind single ::auth
    (rf/reg-fx
      ::signup
      (fn [[email password done-ev error-ev]]
        (-> fb-auth
            (.createUserWithEmailAndPassword email password)
            ;; NOTE: No point in converting `User` instance to clj
            (.then #(rf/dispatch (conj done-ev %))
                   #(rf/dispatch (conj error-ev %))))))

    ;; TODO: Hide logins and signups behind single ::auth
    (rf/reg-fx
      ::logout
      (fn [[done-ev error-ev]]
        (-> (.signOut fb-auth)
            ;; NOTE: No point in converting `User` instance to clj
            (.then #(rf/dispatch (conj done-ev %))
                   #(rf/dispatch (conj error-ev %))))))

    ;; DB event
    (rf/reg-event-fx
      ::db
      [rf/debug]
      (fn [cofx [_ path data done-ev error-ev]]
        (println ::db-event-fx path data done-ev error-ev)
        (assoc cofx ::db [path data done-ev error-ev])))

    ;; Auth event
    (rf/reg-event-fx
      ::auth
      [rf/debug]
      (fn [cofx [_ type params]]
        (println ::auth type params)
        (assoc cofx ::auth [type params])))

    ;; Signup event
    ;; TODO: Make :signup request to ::auth once we migrate ::auth and ::signup services
    (rf/reg-event-fx
      ::signup
      [rf/debug]
      (fn [cofx [_ email password done-ev error-ev]]
        ;; TODO: update with conj to vector
        (assoc cofx ::signup [email password done-ev error-ev])))

    (rf/reg-event-fx
      ::logout
      [rf/debug]
      (fn [cofx [_ done-ev error-ev]]
        ;; TODO: update with conj to vector
        (assoc cofx ::logout [done-ev error-ev])))))

(defn initialize-firebase [config]
  (try
    (js/firebase.initializeApp (clj->js config))
    (catch js/Object e
      (js/console.warn e)
      (.app js/firebase))))
