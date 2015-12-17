(ns replumb.load
  (:require [clojure.string :as string]
            [replumb.common :as common]))

(defn fake-load-fn!
  "This load function just calls:
  (cb {:lang   :js
       :source \"\"})"
  [_ cb]
  (cb {:lang   :js
       :source ""}))

(defn no-resource-load-fn!
  "Mimics \"Resource not found\" as it just calls: (cb nil)"
  [_ cb]
  (cb nil))

(defn file-path->lang
  "Converts a file path to a :lang keyword by inspecting the file extension."
  [file-path]
  (if (string/ends-with? file-path ".js")
    :js
    :clj))

(defn read-files-and-callback!
  "Loop on the file-paths using a supplied read-file-fn (fn [file-path
  src-cb] ...), calling back cb upon first successful read, otherwise
  calling back with nil."
  [verbose? file-paths read-file-fn load-fn-cb]
  ;; AR - Can't make this function tail recursive as it is now
  (if-let [file-path (first file-paths)]
    (do (when verbose?
          (common/debug-prn "Reading" file-path "..."))
        (read-file-fn file-path (fn [source]
                                  (if source
                                    (load-fn-cb {:lang (file-path->lang file-path)
                                                 :source source
                                                 :file file-path})
                                    (do (when verbose?
                                          (common/debug-prn "No source found..."))
                                        (read-files-and-callback! verbose? (rest file-paths) read-file-fn load-fn-cb))))))
    (load-fn-cb nil)))

(defn file-paths-to-try
  "Produces a sequence of file paths to try reading, in the
  order they should be tried."
  [src-paths macros file-path]
  (let [extensions (if macros
                     [".clj" ".cljc"]
                     [".cljs" ".cljc" ".js"])]
    (for [extension extensions
          src-path src-paths]
      ;; AR - will there be a need for https://nodejs.org/docs/latest/api/path.html ?
      (str (common/normalize-path src-path) file-path extension))))

(defn file-paths-to-try-from-ns-symbol
  "Given the symbol of a namespace produces all possibile file paths
  in which given ns could be found."
  [src-paths ns-sym]
  (let [without-extension (string/replace (string/replace (name ns-sym) #"\." "/") #"-" "_")]
    (file-paths-to-try src-paths false without-extension)))

(defn goog-file-paths-to-try
  "Produces a sequence of file paths to try reading crafted for goog
  libraries, in the order they should be tried."
  [src-paths goog-path]
  (for [src-path src-paths]
    (str (common/normalize-path src-path) goog-path ".js")))

(defn skip-load?
  [{:keys [name macros]}]
  (or
   (= name 'cljs.core)
   (= name 'cljs.analyzer)
   (and (= name 'cljs.pprint) macros)
   (and (= name 'cljs.test) macros)
   (and (= name 'clojure.template) macros)))
