(ns replumb.weirdo-test
  (:require [clojure.string :as s]
            [cljs.test :refer-macros [deftest testing is async]]
            [cljs.nodejs :as nodejs]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result? extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.ast :as ast]
            [replumb.load :as load]
            [replumb.test-env :as e]
            [replumb.test-helpers :as h :refer-macros [read-eval-call-test]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Weird test #1

;; AR - Uncommenting either this or the following test, breaks the third
;; Was https://github.com/ScalaConsultants/replumb/issues/66
;; (h/read-eval-call-test e/*target-opts* #_(assoc h/*target-opts* :verbose true)
;;   ["(require '[foo.bar.baz :refer [a]])"
;;    "a"]
;;   (let [out (unwrap-result @_res_)]
;;     (is (success? @_res_) "(require '[foo.bar.baz :refer [a]]) and a should succeed")
;;     (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [a]]) and a should be a valid result")
;;     (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [a]]) and a should not change namespace")
;;     (is (= "\"whatever\"" out) "(require '[foo.bar.baz :refer [a]]) and a should return \"whatever\""))
;;   (_reset!_ '[clojure.string goog.string goog.string.StringBuffer  foo.bar.baz]))
;;
;; (h/read-eval-call-test e/*target-opts*
;;   ["(require '[foo.bar.baz :refer [const-a]])"
;;    "const-a"]
;;   (let [out (unwrap-result @_res_)]
;;     (is (success? @_res_) "(require '[foo.bar.baz :refer [const-a]]) and const-a should succeed")
;;     (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should be a valid result")
;;     (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [const-a]]) and const-a should not change namespace")
;;     (is (= "1024" out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should return 1024"))
;;   (_reset!_ '[foo.bar.baz]))

;; AR The following does not work if one of the above is uncommented
;; (h/read-eval-call-test e/*target-opts*
;;   ["(require 'clojure.string)"]
;;   (let [out (unwrap-result @_res_)]
;;     (is (success? @_res_) "(require 'clojure.string) should succeed")
;;     (is (valid-eval-result? out) "(require 'clojure.string) should be a valid result")
;;     (is (= 'cljs.user (repl/current-ns)) "(require 'clojure.string) should not change namespace")
;;     (is (= "nil" out) "(require 'clojure.string) should return \"nil\""))
;;   (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Weird test #2 -
;; AR - If you comment these out the other two succeed
;; but if you uncomment this and...
;; (h/read-eval-call-test e/*target-opts*
;;   ["(ns my.namespace (:import foo.bar.baz [MyRecord]))"
;;    "(apply str ((juxt :first :second) (foo.bar.baz.MyRecord. \"ABC\" \"DEF\")))"]
;;   (let [out (unwrap-result @_res_)]
;;     (is (success? @_res_) "(ns my.namespace (:import ... )) and (apply str ...) should succeed")
;;     (is (valid-eval-result? out) "(ns my.namespace (:import ... )) and (apply str ...) should be a valid result.")
;;     (is (re-find #"ABCDEF" out) "The result should be ABCDEF"))
;;   (_reset!_ '[my.namespace foo.bar.baz]))

;; ...this is uncommented it break
;; If you comment this and the previous is the, the following succeed
;; (h/read-eval-call-test e/*target-opts*
;;   ["(ns my.namespace (:require-macros [foo.bar.baz :as f]))"
;;    "(f/mul-baz 20 20)"]
;;   (let [error (unwrap-result @_res_)]
;;     (is (not (success? @_res_)) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should not succeed")
;;     (is (valid-eval-error? error) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should be an instance of js/Error")
;;     (is (re-find #"ERROR" (extract-message error)) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should have correct error message"))
;;   (_reset!_ '[my.namespace foo.bar.baz]))

;; (h/read-eval-call-test e/*target-opts*
;;   ["(ns my.namespace (:require-macros [foo.bar.baz :refer [mul-baz]]))"
;;    "(mul-baz 3 3)"]
;;   (let [out (unwrap-result @_res_)]
;;     (is (success? @_res_) "(ns my.namespace (:require-macros ... :refer ...)) and (mul-baz 3 3) should succeed")
;;     (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...:refer...)) and (mul-baz 3 3) should be a valid result.")
;;     (is (= "9" out) "(mul-baz 3 3) should be 9"))
;;   (_reset!_ '[my.namespace foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "nil" out) (str _msg_ "should return \"nil\"")))
  (_reset!_ '[foo.bar.baz]))
