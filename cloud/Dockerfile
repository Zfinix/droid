FROM debian:bookworm-slim

ARG ASTER_VERSION=0.7.1
ARG TARGETARCH

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl git tini \
 && rm -rf /var/lib/apt/lists/*

RUN case "$TARGETARCH" in \
      arm64) triple=aarch64-unknown-linux-musl ;; \
      *) triple=x86_64-unknown-linux-musl ;; \
    esac \
 && name="aster-$ASTER_VERSION-$triple" \
 && base="https://github.com/Zfinix/aster/releases/download/cli-v$ASTER_VERSION" \
 && cd /tmp \
 && curl -fsSLO "$base/$name.tar.gz" \
 && echo "$(curl -fsSL "$base/$name.tar.gz.sha256" | cut -d' ' -f1)  $name.tar.gz" | sha256sum -c - \
 && tar xzf "$name.tar.gz" \
 && install -m 755 "$name/aster" /usr/local/bin/aster \
 && rm -rf /tmp/*

COPY AGENTS.md /opt/aster/AGENTS.md
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

ENV HOME=/data

ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/entrypoint.sh"]
