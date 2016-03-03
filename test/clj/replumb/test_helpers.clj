(ns replumb.test-helpers
  (:require [clojure.string :as s]))

(def ^:dynamic *ns-max-id* 1000)

(defmacro read-eval-call-test
  "This macro mainly tests read-eval-call in an async way.

  It is complex because the author did not want to change too much and
  *tried* to reuse the existing code (apart from some find&replace).

  First of all sources:

  - It must be a vector and it will include the expressions to evaluate
  in replumb.repl/read-eval-call. They will be executed one by one but
  only the result of the last one will be inserted in _res_, see later.

  - If a :before is found in first position and/or :after is found in
  the last but one position of the sources vector, the next form will be
  interpreted as a side effecting 0-arity function to execute as
  fixture, before or after replumb.repl/read-eval-call respectively.

  All the subsequent forms (test) and replumb.repl/read-eval-call are
  executed asynchronously in a cljs.test/async block. The deftest name
  is automatically generated from the sources and (rand-int
  *ns-max-id*) is appended at the end, meaning that no more then
  *ns-max-id* tests with exactly the same sources can be included in
  the same namespace.

  The anaphoras are:
  - _res_ - an atom that will contain the result map
  - _all_res_ - an atom that will contain all the intermediate result maps
  - _reset!_ (fn [& args]) - reset the atom and passes whatever arg to
  replumb.repl/reset-env!
  - _msg_ - will contain a concatenation of the strings to evaluate,
  useful for \"is\" messages, it will have newline (if multi line) or
  space at the end of it"
  [opts sources & forms]
  (let [strings# (->> sources
                      (filter string?)
                      vec)
        var-name# (->> strings#
                       (clojure.string/join " ")
                       ((fn [s] (clojure.string/replace s #"[\s\.\/]" "-")))
                       (filter #(re-find #"[\w\*\+!\-_\?]" (str %)))
                       (take 46)
                       clojure.string/join
                       clojure.string/lower-case
                       ((fn [s] (str s "-" (rand-int *ns-max-id*)))) ;; 50 chars (if (<= *ns-max-id* 1000))
                       symbol)
        msg# (let [multiline? (some #(.contains % "\n") strings#)
                   separator (if multiline? \newline \space)]
               (str (clojure.string/join separator strings#) separator))
        before-fn# (list 'fn '[]
                         (when (= :before (first sources)) (second sources)))
        after-fn# (list 'fn '[]
                        (when (and (> (count sources) 2)
                                   (= :after (nth sources (- (count sources) 2))))
                          (nth sources (dec (count sources)))))]
    `(cljs.test/deftest ~var-name#
       (cljs.test/async ~'done
         (let [~'_res_ (atom ::empty)
               ~'_all_res_ (atom ::empty)
               ~'_msg_ ~msg#
               ~'_reset!_ #(do (reset! ~'_res_ ::empty)
                               (apply replumb.repl/reset-env! ~opts %&))]
           (~before-fn#)
           (-> (reduce #(.then %1 (partial replumb.test-helpers/read-eval-call-promise ~opts %2))
                       (goog.Promise.resolve []) ;; seeding an empty vector
                       ~strings#)
               (.then #(do (reset! ~'_all_res_ %)
                           (reset! ~'_res_ (peek %)))
                      #(throw (ex-info % {:test-var ~var-name#
                                          :source  ~strings#}) %))
               (.thenAlways #(do ~@forms
                                 (~after-fn#)
                                 (~'done)))))))))

(comment
  (require-macros '[macro-debug :refer [debug debug-1 debug-all debug->js debug-1->js debug-all->js]])
  (conj (or results []) %)
  (def _res_ (atom ::empty))
  (def _all_res_ (atom ::empty))
  (def strings ["(require 'clojure.string)"
                "(doc clojure.string/trim)"])
  (def p (-> (reduce #(.then (replumb.test-helpers/read-eval-call-promise e/*target-opts* %2)
                             (fn [prev] (conj %1 prev)))
                     (goog.Promise.resolve []) ;; seeding an empty vector
                     strings)
             (.then #(do (println "here") (reset! _all_res_ %)
                         (reset! _res_ (peek %)))
                    #_#(throw (ex-info % {:test-var ~var-name#
                                          :source  ~strings#}) %))))

  )
