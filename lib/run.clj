(require '[hato.client :as hc]
         '[clojure.data.json :as json]
         '[clojure.pprint :as pp]
         '[autochrome.page :as autochrome]
         '[clojure.java.shell :as sh]
         '[clojure.string :as string])
(import '(java.io File)
        '(java.time Instant))

(def diff-upload-endpoint
  "https://us-central1-autochrome-service.cloudfunctions.net/api/upload")

(def headers
  {"content-type" "application/json"
   "accept" "application/vnd.github.antiope-preview+json"
   "authorization" (str "Bearer " (System/getenv "GITHUB_TOKEN"))
   "user-agent" "clojure-autochrome-diff"})

(defn prs-for-branch [repo branch-name]
    (->> (hc/get (str "https://api.github.com/repos/" repo "/pulls?head=" branch-name)
                 {:headers headers})
         :body
         json/read-str
         #_(map (fn [p]
                {:id (get p "id")
                 :base (get-in p ["base" "sha"])
                 :head (get-in p ["head" "sha"])}))))

(defn save-diff! [diff]
  (let [f (.getpath (File/createTempFile "autochrome-" ".html"))]
    (spit f diff)
    ;; no multipart stuff in clj-http-lite :/
    (:out (sh/sh "curl" "-F" (str  "data=@" f) diff-upload-endpoint))))

(defn create-check [repo head-sha]
  (-> (hc/post (str  "https://api.github.com/repos/" repo "/check-runs")
               {:headers headers
                :body (json/write-str
                        {:name "autochrome diff"
                         :head_sha head-sha
                         :status "in_progress"
                         :started_at (.toString (Instant/now))})})
      :body
      json/read-str
      (get "id")))

#_(defn conclude-check [repo check-id]
  (-> (hc/patch (str  "https://api.github.com/repos/" repo "/check-runs/" check-id)
                {:headers headers
                 :body (json/write-str
                         {:name "autochrome diff"
                          :status "completed"
                          :completed_at (.toString (Instant/now))
                          :conclusion "neutral"
                          :output {:title "Structural Diff"
                                   :summary "Test summary"
                                   :annotations [{:path ".firebaserc" ;"README.md"
                                                  :start_line 1
                                                  :end_line 1
                                                  :annotation_level "notice"
                                                  :message (str  "View the structural diff for this PR here: " "https://storage.cloud.google.com/autochrome-service.appspot.com/diffs/YFezJQnU0qp860E08lK-I.html")}]}})})))

(defn add-comment [repo pr-id]
  (-> (hc/post (str "https://api.github.com/repos/" repo "/issues/" pr-id "/comments")
               {:headers headers
                :body (json/write-str
                        {:body (str  "View the structural diff for this PR here: " "https://storage.cloud.google.com/autochrome-service.appspot.com/diffs/YFezJQnU0qp860E08lK-I.html")})})
      :body
      json/read-str
      #_(get "id")))

(def autochrome-user-id 61368450)

(defn get-comments [repo pr-id]
  (->> (hc/get (str "https://api.github.com/repos/" repo "/issues/" pr-id "/comments")
              {:headers headers})
      :body
      json/read-str))

(defn delete-own-comments [comments]
  (doseq [{:strs [url]} (filter #(= autochrome-user-id (get-in % ["user" "id"])) comments)]
    (hc/delete url {:headers headers})))


(println "ARGS" *command-line-args*)
(prn (:out (sh/sh "ls")))
(prn (:out (sh/sh "pwd")))
(prn (System/getenv "GITHUB_TOKEN"))
(let [[p] (prs-for-branch (System/getenv "GITHUB_REPOSITORY") "autochrome-action")]
  (autochrome/local-diff (:base p) (:head p)))

(comment
  (hc/delete "https://api.github.com/repos/martinklepsch/autochrome-action/issues/comments/590096341"
             {:headers headers})
  
  (.toString (Instant/now))
  (def repo "martinklepsch/autochrome-action")

  (def check (create-check repo (:head p)))
  (conclude-check repo check)
  (prn *e)
  (prn check)

  (add-comment repo 1)
  (-> (get-comments repo 1)
      (delete-own-comments))

  (def p
    (first
      (prs-for-branch repo "autochrome-action")))

  (def f (.getPath (File/createTempFile "autochrome-" ".html")))
  (spit f "testing")

  ;; https://storage.cloud.google.com/autochrome-service.appspot.com/diffs/YFezJQnU0qp860E08lK-I.html
  ;; https://storage.cloud.google.com/autochrome-service.appspot.com//diffs/YFezJQnU0qp860E08lK-I.html

  (autochrome/local-diff "71f74d9" "2708fcc")

  )
