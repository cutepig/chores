(ns re-frame-firebase.core
  (:require [reagent.ratom :as ratom]
            [re-frame.core :as rf]
            [cljsjs.firebase]))

(defn reg-firebase [firebase]
  ;; TODO: Reference counting for firebase refs. Currently we just leave them hanging.
  ;; But we do get rid of subscriptions created with `ref.on()`.
  (let [refs (atom {})
        fb-db (.database firebase)
        fb-auth (.auth firebase)]
    ;; Database subscription
    (rf/reg-sub-raw
      ::db
      ;; TODO: Make more of this action configurable, is it `.on` or is it `.once`,
      ;; Do we subscribe to `value` or one of the child-events
      ;; TODO: Should `mapper` be just a `read-path` making reaction `(get-in @db-atom read-path)`?
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
    (rf/reg-sub-raw
      ::auth
      (fn [db-atom [_ mapper read-ev]]
        (let [read-fn #(rf/dispatch (conj read-ev (js->clj % :keywordize-keys true)))
              unref (.onAuthStateChanged fb-auth read-fn)]
          (read-fn (.-currentUser fb-auth))
          (ratom/make-reaction
            (fn [] (mapper @db-atom))
            :on-dispose unref))))

    ;; Service
    (rf/reg-fx
      ::firebase
      ;; TODO: Make the action configurable with different actions:
      ;; `set`, `update`, `remove`, `push`
      (fn [ops]
        (for [[path data done-ev error-ev] ops]
          (-> (.ref fb-db path)
              (.set data)
              (.then #(rf/dispatch done-ev %) #(rf/dispatch error-ev %))))))

    ;; I lost the trail of thoughts so I don't know what I was aiming for here.
    ;; Probably something awesome but what is it?

    ;; Event
    (rf/reg-event-fx
      ::firebase
      (fn [cofx [_ path data done-ev error-ev]]
        (comment "Here we get or create the ref, ")))))

(defn initialize-firebase [config]
  (try
    (js/firebase.initializeApp (clj->js config))
    (catch js/Object e
      (js/console.warn e)
      (.app js/firebase))))
