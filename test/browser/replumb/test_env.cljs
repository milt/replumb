(ns replumb.test-env
  (:require [replumb.core :as core]
            [replumb.browser.io :as io]))

(def ^:dynamic *target-opts*  (assoc (core/options :browser
                                                   ["js/compiled/out"]
                                                   io/fetch-file!)
                                     :warning-as-error true))

(def ^:dynamic *read-file-fn* io/fetch-file!)
(def ^:dynamic *write-file-fn* nil)
(def ^:dynamic *delete-file-fn* nil)
