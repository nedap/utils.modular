(ns nedap.utils.modular.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [nedap.utils.test.api :refer-macros [run-tests]]
   [unit.nedap.utils.modular.api.implement]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'unit.nedap.utils.modular.api.implement))

(set! *main-cli-fn* -main)
