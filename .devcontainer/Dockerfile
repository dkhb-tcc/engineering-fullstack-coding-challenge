FROM mcr.microsoft.com/devcontainers/javascript-node:1-20-bookworm

# Remove stale Yarn apt sources if present, then install tools needed for the interview repo.
RUN rm -f /etc/apt/sources.list.d/yarn.list /etc/apt/sources.list.d/yarnpkg.list || true &&     apt-get update &&     apt-get install -y --no-install-recommends curl unzip git ca-certificates python3 python3-pip python3-venv &&     rm -rf /var/lib/apt/lists/*

# Install Gradle explicitly so the dev container can run `gradle ...` without a wrapper.
ARG GRADLE_VERSION=8.10.2
RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip &&     mkdir -p /opt/gradle &&     unzip -q /tmp/gradle.zip -d /opt/gradle &&     ln -s "/opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle" /usr/local/bin/gradle &&     rm -f /tmp/gradle.zip
