FROM clojure:openjdk-17-tools-deps
WORKDIR /primes
COPY / ./
ENTRYPOINT ["./run.sh"]
