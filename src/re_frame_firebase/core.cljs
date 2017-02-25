(ns re-frame-firebase.core
  (:require [reagent.ratom :as ratom]
            [re-frame.core :as rf]
            [cljsjs.firebase]))

(defn reg-firebase [firebase]
  ;; TODO: Reference counting for firebase refs. Currently we just leave them hanging.
  ;; But we do get rid of subscriptions created with `ref.on()`.
  ;; TODO: In addition to ref we should also store the generated atom which we can
  ;; then pull from cache.
  (let [refs (atom {})
        fb-db (.database firebase)
        fb-auth (.auth firebase)]
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
        (println "sub" ::db path)
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
      (fn [ops]
        (for [[path data done-ev error-ev] ops]
          (-> (.ref fb-db path)
              (.set data)
              (.then #(rf/dispatch (conj done-ev (js->clj % :keywordize-keys true)))
                     #(rf/dispatch (conj error-ev %)))))))

    ;; Auth service
    ;; TODO: run auth info (js->cljs % :keywordize-keys trye)
    (rf/reg-fx
      ::auth
      ;; TODO: Support all types of logins as well as signup
      (fn [[email password done-ev error-ev]]
        (-> fb-auth
            (.signInWithEmailAndPassword email password)
            ;; NOTE: No point in converting `User` instance to clj
            (.then #(rf/dispatch (conj done-ev %))
                   #(rf/dispatch (conj error-ev %))))))

    ;; TODO: Hide logins and signups behind single ::auth
    (rf/reg-fx
      ::signup
      (fn [[email password done-ev error-ev]]
        (println :ref-fx-signup email password)
        (-> fb-auth
            (.createUserWithEmailAndPassword email password)
            ;; NOTE: No point in converting `User` instance to clj
            (.then #(rf/dispatch (conj done-ev %))
                   #(rf/dispatch (conj error-ev %))))))

    ;; TODO: Hide logins and signups behind single ::auth
    (rf/reg-fx
      ::logout
      (fn [[done-ev error-ev]]
        (println :ref-fx-logout done-ev error-ev)
        (-> (.signOut fb-auth)
            ;; NOTE: No point in converting `User` instance to clj
            (.then #(rf/dispatch (conj done-ev %))
                   #(rf/dispatch (conj error-ev %))))))

    ;; DB event
    (rf/reg-event-fx
      ::db
      (fn [cofx [_ path data done-ev error-ev]]
        ;; TODO: update with conj to vector
        (assoc cofx ::db [[path data done-ev error-ev]])))

    ;; Auth event
    (rf/reg-event-fx
      ::auth
      (fn [cofx [_ email password done-ev error-ev]]
        (println :reg-event-fx-auth cofx)
        ;; TODO: update with conj to vector
        (assoc cofx ::auth [email password done-ev error-ev])))

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
