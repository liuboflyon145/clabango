(ns clabango.tags)

(defn load-template [template]
  (-> (Thread/currentThread)
      (.getContextClassLoader)
      (.getResource template)
      slurp))

;; template-tag must return a 2-vector of [string context]
;; where string will be parsed and context will be the new
;; context passed along to the rest of the pipeline
;; (including parsing of string)

;; the reason template-tag needs to return context is some
;; tags need to keep track of state and the context is the
;; logical place to do that (rather than a ref or atom)

(defmulti template-tag (fn [tag-name & _] tag-name))

(defmethod template-tag "include" [_ nodes context]
  (let [[node] nodes
        [template] (:args node)
        template (second (re-find #"\"(.*)\"" template))]
    [(load-template template)
     context]))

(defmethod template-tag "block" [_ nodes context]
  [""
   (assoc context :foo 42)])

(defmethod template-tag "extends" [_ nodes context]
  (let [[s context] (template-tag "include" nodes context)]
    [s
     (assoc context :extended true)]))

(defmethod template-tag "with-foo-as-42" [_ nodes context]
  [(rest (butlast nodes)) (assoc context :foo 42)])