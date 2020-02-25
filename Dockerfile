FROM clojure:openjdk-11-tools-deps

COPY /lib/deps.edn /action/lib/deps.edn
RUN cd /action/lib; clojure -Spath

COPY /lib /action/lib

ENTRYPOINT ["/action/lib/entrypoint.sh"]
