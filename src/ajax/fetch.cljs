(ns ajax.fetch
  (:require ajax.protocols))

(defrecord FetchRequest []
  ajax.protocols/AjaxRequest
  ; Not supported on fetch, no-op.
  (-abort [this]))

(defrecord FetchResponse [status status-text body]
  ajax.protocols/AjaxResponse
  (-status [this]
    status)
  (-status-text [this]
    status-text)
  (-body [this]
    body)
  (-get-response-header [this header]  nil)
  (-was-aborted [this] false))

(defrecord FetchApi []
  ajax.protocols/AjaxImpl
  (-js-ajax-request
    [this {:keys [uri method body headers response-format]} handler]
    (->
      (js/fetch 
        uri 
        (clj->js {:method method
                  :headers headers
                  :body body}))
      (.then 
        (fn [res] 
          (-> (.text res) 
              (.then (fn [body]
                       (handler (->FetchResponse 
                                  (.-status res) 
                                  (.-statusText res) 
                                  body)))))))
      (.catch 
        (fn [err] 
          (handler (->FetchResponse -2 (.-message err) nil)))))
    (->FetchRequest)))

