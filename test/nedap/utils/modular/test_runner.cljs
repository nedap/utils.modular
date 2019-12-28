(ns nedap.utils.modular.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [nedap.utils.test.api :refer-macros [run-tests]]
   [unit.nedap.utils.modular.api.add-method]
   [unit.nedap.utils.modular.api.dependent]
   [unit.nedap.utils.modular.api.implement]
   [unit.nedap.utils.modular.api.omit-this]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests 'unit.nedap.utils.modular.api.add-method
             'unit.nedap.utils.modular.api.dependent
             'unit.nedap.utils.modular.api.implement
             'unit.nedap.utils.modular.api.omit-this))

(set! *main-cli-fn* -main)
