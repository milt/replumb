(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-tests doo-all-tests]]
            replumb.core-test
            replumb.repl-test
            replumb.common-test
            replumb.load-test
            replumb.options-test
            replumb.macro-test
            replumb.require-test
            replumb.source-test
            #_replumb.cache-node-test)) ;; AR/TB TODO port it to the test

(enable-console-print!)

(doo-tests 'replumb.core-test
           'replumb.repl-test
           'replumb.common-test
           'replumb.load-test
           'replumb.options-test
           'replumb.macro-test
           'replumb.require-test
           'replumb.source-test)

;; (doo-tests 'replumb.weirdo-test)
