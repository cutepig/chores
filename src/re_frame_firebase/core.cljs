(ns re-frame-firebase.core
  (:require [reagent.ratom :as ratom]
            [re-frame.core :as rf]
            [cljsjs.firebase]))

(defn reg-firebase [firebase-app]
  ;; TODO: Reference counting for firebase refs. Currently we just leave them hanging.
  ;; But we do get rid of subscriptions created with `ref.on()`.
  ;; TODO: In addition to ref we should also store the generated atom which we can
  ;; then pull from cache.
  (let [refs (atom {})
        fb-db (.database firebase-app)
        fb-auth (.auth firebase-app)]
    ;; Database subscription
    (rf/reg-sub-raw
      ::db
      ;; TODO: Make more of this action configurable, is it `.on` or is it `.once`,
      ;; Do we subscribe to `value` or one of the child-events
      ;; TODO: Should `mapper` be just a `read-path` making reaction `(get-in @db-atom read-path)`?
      ;; TODO: Drop the `mapper` and `read-ev` totally, we write and read from ::db
      ;; Just supply a handler for errors
      ;; Also `path` as kv
      (fn [db-atom [_ path mapper read-ev]]
        (let [-ref (or (get @refs path) (.ref fb-db path))
              read-fn #(rf/dispatch (conj read-ev (js->clj (.val %) :keywordize-keys true)))
              ;; TODO: Error-ev? where to hook
              unref (.on -ref "value" read-fn)]
          (swap! refs #(assoc % path -ref))
          (ratom/make-reaction
            (fn [] (mapper @db-atom))
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
      (fn [[path data done-ev error-ev]]
        (println ::db-fx path data done-ev error-ev)
        (-> (.ref fb-db path)
            (.set (clj->js data))
            (.then #(rf/dispatch done-ev)
                   #(rf/dispatch (conj error-ev %))))))

    ;; Auth service
    ;; TODO: run auth info (js->cljs % :keywordize-keys trye)
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
      (fn [cofx [_ path data done-ev error-ev]]
        (println ::db-event-fx path data done-ev error-ev)
        (assoc cofx ::db [path data done-ev error-ev])))

    ;; Auth event
    (rf/reg-event-fx
      ::auth
      (fn [cofx [_ type params]]
        (assoc cofx ::auth [type params])))

    ;; Signup event
    ;; TODO: Make :signup request to ::auth once we migrate ::auth and ::signup services
    (rf/reg-event-fx
      ::signup
      (fn [cofx [_ email password done-ev error-ev]]
        ;; TODO: update with conj to vector
        (assoc cofx ::signup [email password done-ev error-ev])))

    (rf/reg-event-fx
      ::logout
      (fn [cofx [_ done-ev error-ev]]
        ;; TODO: update with conj to vector
        (assoc cofx ::logout [done-ev error-ev])))))

(defn initialize-firebase [config]
  (try
    (js/firebase.initializeApp (clj->js config))
    (catch js/Object e
      (js/console.warn e)
      (.app js/firebase))))
