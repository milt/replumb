(ns replumb.test-helpers
  (:require replumb.repl goog.Promise))

(defn read-eval-call-promise
  "Return a promise that resolves with the result of accumulating the
  actual call to replumb.repl/read-eval-call on the source with results."
  [opts source results]
  {:pre [(vector? results)]}
  (goog.Promise.
   (fn [resolve, _]
     (replumb.repl/read-eval-call
      opts
      (partial replumb.repl/validated-call-back!
               opts
               #(resolve (conj results %)))
      source))))
