(ns account.main
  "服务入口"
  (:require [clojure.edn :as edn]
            [account.db :as d]
            [account.http :as http]
            [account.register :as reg]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn load-config
  "加载配置文件"
  [config-file]
  (-> (slurp config-file)
      (edn/read-string)))

(defn start-server
  "启动服务"
  [cfg]
  (let [{:keys [web-opts datomic-url]} cfg
        conn (d/mk-conn datomic-url d/account-schema)
        context {:conn conn}
        handler (http/handler {:exist (reg/mk-exist context)
                               :register (reg/mk-register context)})]
    (println "start server. options : " web-opts)
    (run-jetty handler web-opts)))

(defn -main
  "服务程序入口"
  [config-file]
  (start-server (load-config config-file)))
