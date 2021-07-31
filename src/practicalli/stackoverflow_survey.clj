;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Practicalli Tablecloth Playground and Stackoverflow survey example
;;
;; See the `tablecloth-basics` namespace for example use of Tablecloth functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(ns practicalli.stackoverflow-survey
  (:gen-class)
  (:require [tablecloth.api :as tables]
            [tech.v3.datatype.functional :as datatype]))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
