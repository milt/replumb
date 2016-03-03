(ns replumb.test-env
  (:require [cljs.test :include-macros true]
            [replumb.core :as core]
            [replumb.nodejs.io :as io])
  (:require-macros replumb.test-helpers))

(def ^:dynamic *target-opts* (assoc
                              (core/options :nodejs
                                            ["dev-resources/private/test/node/compiled/out"
                                             "dev-resources/private/test/src/cljs"
                                             "dev-resources/private/test/src/clj"
                                             "dev-resources/private/test/src/cljc"
                                             "dev-resources/private/test/src/js"]
                                            io/read-file!)
                              :warning-as-error true))
(def ^:dynamic *read-file-fn* io/read-file!)
(def ^:dynamic *write-file-fn* io/write-file!)
(def ^:dynamic *delete-file-fn* io/delete-file!)
