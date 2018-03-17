(ns account.db
  "datomic数据库"
  (:require [datomic.api :as d]))

(def account-schema
  "数据库账户数据定义"
  [#:db{:ident       :account/username
        :doc         "用户名"
        :cardinality :db.cardinality/one
        :valueType   :db.type/string
        :unique      :db.unique/identity}
   #:db{:ident       :account/password
        :doc         "密码"
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}])

(defn gen-account
  "组装账户数据"
  [{:keys [username password]}]
  [{:account/username username
    :account/password password}])

(defn find-account
  "查询账户数据"
  [db username]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?username
         :where
         [?e :account/username ?username]]
       db username))

(defn update-data
  "更新数据库数据"
  [conn data]
  @(d/transact conn data))

(defn conn->db
  "从连接获取数据"
  [conn]
  (d/db conn))

(defn mk-conn
  "初始化数据库连接"
  [uri db-schema]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    @(d/transact conn db-schema)
    conn))

(comment
  (def conn (mk-conn "datomic:mem:acc-repl" account-schema))
  (d/transact conn (gen-account {:username "foo" :password "123456"}))
  (find-account (d/db conn) "foo")
  (d/delete-database "datomic:mem:acc-repl"))

; 如果哪天觉得记录的字段不够用了,需要扩展?
(comment
  (def account-schema
    "数据库账户数据定义"
    [#:db{:ident :account/username :doc "用户名" :cardinality :db.cardinality/one :valueType :db.type/string :unique :db.unique/identity}
     #:db{:ident :account/password :doc "密码" :cardinality :db.cardinality/one :valueType :db.type/string}
     #:db{:ident :account/telephone :doc "电话号码" :cardinality :db.cardinality/one :valueType :db.type/string}])

  (defn gen-account
    "组装账户数据"
    [{:keys [username password telephone]}]
    [{:account/username username
      :account/password password
      :account/telephone telephone}])

  (def conn (mk-conn "datomic:mem:acc-repl" account-schema))
  (d/transact conn (gen-account {:username "bar"
                                 :password "123456"
                                 :telephone "1333333333"}))
  (clojure.pprint/pprint
    (find-account (d/db conn) "foo"))
  (clojure.pprint/pprint
    (find-account (d/db conn) "bar")))

; 业务发展得太快,我需要绑定第三方账号?
(comment
  (def third-party-schema
    "数据库第三方账户数据定义"
    [#:db{:ident :account/third-party :doc "第三方账号" :cardinality :db.cardinality/many :valueType :db.type/ref :isComponent true}
     #:db{:ident :third/channel :doc "渠道" :cardinality :db.cardinality/one :valueType :db.type/string}
     #:db{:ident :third/username :doc "第三方渠道的账号" :cardinality :db.cardinality/one :valueType :db.type/string}
     #:db{:ident :third/nickname :doc "第三方渠道的账号昵称" :cardinality :db.cardinality/one :valueType :db.type/string}])

  (defn gen-third-party
    "组装绑定第三方账户数据"
    [username {channel :channel third-username :username nickname :nickname}]
    [{:account/username username
      :account/third-party {:third/channel channel
                            :third/username third-username
                            :third/nickname nickname}}])

  (def conn (mk-conn "datomic:mem:acc-repl" (concat account-schema third-party-schema)))
  (d/transact conn (gen-third-party "bar" {:channel "qq"
                                           :username "888888"
                                           :nickname "大师兄"}))
  (d/transact conn (gen-third-party "bar" {:channel "weixin"
                                           :username "我不知道"
                                           :nickname "你猜?"}))
  (clojure.pprint/pprint
    (find-account (d/db conn) "foo"))
  (clojure.pprint/pprint
    (find-account (d/db conn) "bar"))
  )
