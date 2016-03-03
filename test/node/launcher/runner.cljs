(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-tests doo-all-tests]]
            replumb.core-test
            replumb.repl-test
            replumb.common-test
            replumb.load-test
            replumb.options-test
            replumb.require-test
            replumb.source-test
            #_replumb.cache-node-test))

(enable-console-print!)

(doo-tests 'replumb.core-test
           'replumb.repl-test
           'replumb.common-test
           'replumb.load-test
           'replumb.options-test
           'replumb.require-test
           'replumb.source-test)

;; (doo-tests 'replumb.weirdo-test)
